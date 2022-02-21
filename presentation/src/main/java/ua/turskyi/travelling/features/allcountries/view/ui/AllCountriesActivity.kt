package ua.turskyi.travelling.features.allcountries.view.ui

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.databinding.ActivityAllCountriesBinding
import ua.turskyi.travelling.features.allcountries.view.adapter.AllCountriesAdapter
import ua.turskyi.travelling.features.allcountries.view.adapter.EmptyListObserver
import ua.turskyi.travelling.features.allcountries.viewmodel.AllCountriesActivityViewModel
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.utils.extensions.openInfoDialog
import ua.turskyi.travelling.utils.extensions.toastLong
import ua.turskyi.travelling.utils.hideKeyboard
import ua.turskyi.travelling.utils.showKeyboard
import ua.turskyi.travelling.widgets.ExpandableSearchBar.OnSearchActionListener


class AllCountriesActivity : AppCompatActivity() {

    private val viewModel: AllCountriesActivityViewModel by inject()
    private val adapter: AllCountriesAdapter by inject()
    private lateinit var binding: ActivityAllCountriesBinding
    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            viewModel.searchQuery = "%$s%"
            adapter.submitList(viewModel.pagedList)
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

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }

    private fun initView() {
        binding = ActivityAllCountriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // init animated background
        binding.root.setBackgroundResource(R.drawable.gradient_list)
        val animationDrawable: AnimationDrawable = binding.root.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(resources.getInteger(R.integer.enter_fade_duration))
        animationDrawable.setExitFadeDuration(resources.getInteger(R.integer.exit_fade_duration))
        animationDrawable.start()

        binding.expandableSearchBar.isFocusableInTouchMode = true
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBlack)
        adapter.submitList(viewModel.pagedList)
        val layoutManager = LinearLayoutManager(this)
        binding.rvAllCountries.adapter = adapter
        binding.rvAllCountries.layoutManager = layoutManager
        val w: Window = window
        w.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    private fun initListeners() {
        binding.expandableSearchBar.addTextChangeListener(textWatcher)
        binding.expandableSearchBar.onSearchActionListener =
            object : OnSearchActionListener {
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
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        adapter.onCountryClickListener = ::addToVisited
        adapter.onCountryLongClickListener = ::sendToGoogleMapToShowGeographicalLocation
        binding.rvAllCountries.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                when {
                    dy > 0 -> binding.floatBtnInfo.show()
                    dy < 0 -> binding.floatBtnInfo.hide()
                }
            }
        })
        binding.floatBtnInfo.setOnClickListener { openInfoDialog(getString(R.string.txt_info_all_countries)) }
    }

    private fun initObservers() {
        val emptyListObserver = EmptyListObserver(binding.rvAllCountries, binding.tvNoResults)
        adapter.registerAdapterDataObserver(emptyListObserver)
        viewModel.notVisitedCountriesNumLiveData.observe(this) { notVisitedNum ->
            updateTitle(notVisitedNum)
        }
        viewModel.visibilityLoader.observe(this) { currentVisibility ->
            binding.pb.visibility = currentVisibility
        }
        viewModel.errorMessage.observe(this) { event ->
            val message: String? = event.getMessageIfNotHandled()
            if (message != null) {
                toastLong(message)
            }
        }
    }

    private fun sendToGoogleMapToShowGeographicalLocation(country: Country) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(getString(R.string.geo_location, country.name))
        )
        startActivity(intent)
    }

    private fun addToVisited(country: Country) {
        viewModel.markAsVisited(country) {
            hideKeyboard()
            val intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun updateTitle(num: Int) {
        binding.tvToolbarTitle.text = resources.getQuantityString(
            R.plurals.numberOfCountriesRemain,
            num,
            num,
        )
    }

    private fun collapseSearch() {

        binding.rvAllCountries.animate()
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
        binding.rvAllCountries.animate().translationY(0f)
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