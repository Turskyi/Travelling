package ua.turskyi.travelling.features.travellers.view

import android.animation.ValueAnimator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.databinding.ActivityTravellersBinding
import ua.turskyi.travelling.entities.Traveller
import ua.turskyi.travelling.features.allcountries.view.adapter.EmptyListObserver
import ua.turskyi.travelling.features.traveller.view.TravellerActivity
import ua.turskyi.travelling.features.travellers.TravellersActivityViewModel
import ua.turskyi.travelling.features.travellers.view.adapter.TravellersAdapter
import ua.turskyi.travelling.utils.Event
import ua.turskyi.travelling.utils.extensions.hideKeyboard
import ua.turskyi.travelling.utils.extensions.openActivityWithObject
import ua.turskyi.travelling.utils.extensions.showKeyboard
import ua.turskyi.travelling.utils.extensions.toastLong
import ua.turskyi.travelling.widgets.ExpandableSearchBar

class TravellersActivity : AppCompatActivity(), VisibilityDialog.VisibilityListener {
    companion object {
        const val EXTRA_TRAVELLER = "ua.turskyi.travelling.TRAVELLER"
    }

    private val viewModel: TravellersActivityViewModel by inject()
    private val listAdapter: TravellersAdapter by inject()

    private lateinit var binding: ActivityTravellersBinding
    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            viewModel.searchQuery = s.toString()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListeners()
        initObservers()
    }

    override fun becomeVisible() {
        viewModel.onBecomingVisibleTriggered()
    }

    private fun initView() {
        binding = ActivityTravellersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.expandableSearchBar.isFocusableInTouchMode = true
        @Suppress("DEPRECATION")
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
        binding.rvTravellers.adapter = listAdapter
        val layoutManager = GridLayoutManager(this, 2)
        binding.rvTravellers.layoutManager = layoutManager
    }

    private fun initListeners() {
        binding.expandableSearchBar.addTextChangeListener(textWatcher)
        binding.expandableSearchBar.onSearchActionListener =
            object : ExpandableSearchBar.OnSearchActionListener {
                override fun onSearchStateChanged(enabled: Boolean) {
                    if (enabled) {
                        expandSearch()
                    } else {
                        collapseSearch()
                    }
                }

                override fun onSearchConfirmed(text: String?) {}
                override fun onButtonClicked(buttonCode: Int) {}
            }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        listAdapter.onTravellerClickListener = ::showTraveller
        binding.rvTravellers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                when {
                    dy > 0 -> binding.floatBtnVisibility.show()
                    dy < 0 -> binding.floatBtnVisibility.hide()
                }
            }
        })
        binding.floatBtnVisibility.apply {
            setOnClickListener {
                val isUserVisible: Boolean = tag is Int && (tag as Int) == R.drawable.btn_eye_ripple
                if (isUserVisible) {
                    viewModel.onVisibilityFabClicked()
                } else {
                    val infoDialog: VisibilityDialog = VisibilityDialog.newInstance(
                        getString(R.string.txt_info_all_travellers)
                    )
                    infoDialog.show(
                        supportFragmentManager,
                        resources.getString(R.string.tag_visibility_dialog),
                    )
                }
            }
        }
    }

    private fun initObservers() {
        val observer = EmptyListObserver(binding.rvTravellers, binding.tvNoResults)
        listAdapter.registerAdapterDataObserver(observer)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pagedTravellers.collectLatest { pagingData ->
                    listAdapter.submitData(pagingData)
                }
            }
        }
        viewModel.topTravellersPercentLiveData.observe(this) { topPercent: Int ->
            updateTitle(topPercent)
        }
        viewModel.visibilityLoader.observe(this) { currentVisibility: Int ->
            binding.pb.visibility = currentVisibility
        }
        viewModel.visibilityUser.observe(this) { currentVisibility: Int ->
            if (currentVisibility == VISIBLE) {
                binding.floatBtnVisibility.setImageResource(R.drawable.btn_eye_ripple)
                binding.floatBtnVisibility.tag = R.drawable.btn_eye_ripple
            } else {
                binding.floatBtnVisibility.setImageResource(R.drawable.btn_hide_ripple)
                binding.floatBtnVisibility.tag = R.drawable.btn_hide_ripple
            }
        }
        viewModel.errorMessage.observe(this) { event: Event<String> ->
            event.getMessageIfNotHandled()?.let { message: String ->
                toastLong(message)
            }
        }
    }

    private fun showTraveller(traveller: Traveller) {
        hideKeyboard()
        openActivityWithObject(TravellerActivity::class.java, EXTRA_TRAVELLER, traveller)
    }

    private fun updateTitle(percent: Int) {
        if (percent > 0) {
            binding.tvTopPercentage.text =
                getString(R.string.title_activity_travellers, percent)
        } else {
            binding.tvTopPercentage.text = getString(R.string.title_most)
        }
    }

    private fun collapseSearch() {
        binding.rvTravellers.animate()
            .translationY((-1 * resources.getDimensionPixelSize(R.dimen.offset_20)).toFloat())
        binding.expandableSearchBar.isSelected = false
        hideKeyboard()
        binding.toolbar.animate().alpha(1f).duration = 200
        ValueAnimator.ofInt(
            0,
            binding.toolbar.width
        ).apply {
            addUpdateListener {
                binding.toolbar.layoutParams.width = animatedValue as Int
                binding.toolbar.requestLayout()
                binding.toolbar.clearFocus()
            }
            duration = 400
        }.start()
    }

    private fun expandSearch() {
        binding.rvTravellers.animate().translationY(0f)
        binding.expandableSearchBar.isSelected = true
        binding.toolbar.animate().alpha(0f).duration = 200
        ValueAnimator.ofInt(
            0,
            binding.toolbar.width
        ).apply {
            addUpdateListener {
                binding.toolbar.layoutParams.width = animatedValue as Int
                binding.toolbar.requestLayout()
            }
            doOnEnd {
                binding.toolbar.requestFocus()
            }
            duration = 400
        }.start()
        showKeyboard()
    }
}
