package ua.turskyi.travelling.features.traveller.view

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.databinding.ActivityTravellerBinding
import ua.turskyi.travelling.entities.Traveller
import ua.turskyi.travelling.entities.VisitedCountryNode
import ua.turskyi.travelling.features.flags.view.FlagsActivity
import ua.turskyi.travelling.features.traveller.TravellerActivityViewModel
import ua.turskyi.travelling.features.travellers.view.TravellersActivity.Companion.EXTRA_TRAVELLER
import ua.turskyi.travelling.utils.decoration.SectionAverageGapItemDecoration
import ua.turskyi.travelling.utils.extensions.getScreenWidth
import ua.turskyi.travelling.utils.extensions.openActivityWithArgs
import ua.turskyi.travelling.utils.extensions.parcelable
import ua.turskyi.travelling.utils.extensions.toast
import ua.turskyi.travelling.utils.extensions.toastLong

class TravellerActivity : AppCompatActivity(), TravellerActivityView {
    private val viewModel: TravellerActivityViewModel by inject()
    private val listAdapter: VisitedCountriesAdapter by inject()

    private lateinit var binding: ActivityTravellerBinding
    private var traveller: Traveller? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        traveller = intent.parcelable(EXTRA_TRAVELLER)
        if (traveller != null) {
            initView()
            initObservers()
            initListeners()
        } else {
            toast(getString(R.string.msg_user_not_found))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun initTitle() {
        showTitleWithOnlyCountries()
    }

    override fun showTitleWithOnlyCountries() {
        viewModel.visitedCountriesWithCitiesNode.observe(this) { visitedCountryNodes: List<VisitedCountryNode> ->
            binding.includeAppBar.toolbarLayout.title = resources.getString(
                R.string.name_and_counter,
                traveller!!.name,
                resources.getQuantityString(
                    R.plurals.travellerCounter,
                    visitedCountryNodes.size,
                    visitedCountryNodes.size
                )
            )
        }
    }

    private fun initView() {
        binding = ActivityTravellerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.includeAppBar.toolbar)
        binding.includeAppBar.toolbarLayout.title = traveller!!.name

        val linearLayoutManager = LinearLayoutManager(this)
        binding.rvVisitedCountries.apply {
            adapter = listAdapter
            layoutManager = linearLayoutManager
            addItemDecoration(
                SectionAverageGapItemDecoration(
                    resources.getDimensionPixelOffset(R.dimen.offset_10),
                    resources.getDimensionPixelOffset(R.dimen.offset_10),
                    resources.getDimensionPixelOffset(R.dimen.offset_20),
                    resources.getDimensionPixelOffset(R.dimen.offset_16),
                )
            )
        }
        initGravityForTitle()
        binding.includeAppBar.cvAvatar.visibility = VISIBLE
        Glide.with(this).load(traveller!!.avatar).into(binding.includeAppBar.ivAvatar)
        viewModel.showListOfVisitedCountriesById(traveller!!.id)
        window.decorView.setBackgroundColor(Color.BLACK)
    }

    private fun initListeners() {
        listAdapter.apply {
            onFlagClickListener = { country: VisitedCountryNode ->
                if (SystemClock.elapsedRealtime() - viewModel.mLastClickTime > resources.getInteger(
                        R.integer.click_interval
                    )
                ) {
                    val position = viewModel.visitedCountriesWithCitiesNode.value?.indexOf(country)
                        ?.takeIf { it != -1 } ?: getItemPosition(country)
                    val itemCount =
                        viewModel.visitedCountriesWithCitiesNode.value?.size ?: traveller!!.counter
                    openActivityWithArgs(FlagsActivity::class.java) {
                        putParcelable(FlagsActivity.EXTRA_USER, traveller)
                        putInt(FlagsActivity.EXTRA_POSITION, position)
                        putInt(FlagsActivity.EXTRA_ITEM_COUNT, itemCount)
                    }
                }
                viewModel.mLastClickTime = SystemClock.elapsedRealtime()
            }
        }
    }

    private fun initObservers() {
        viewModel.visitedCountriesWithCitiesNode.observe(this) { visitedCountries ->
            initTitle()
            updateAdapterWith(visitedCountries)
        }
        viewModel.visibilityLoader.observe(this) { currentVisibility ->
            binding.pb.visibility = currentVisibility
        }

        viewModel.errorMessage.observe(this) { event ->
            event.getMessageIfNotHandled()?.let { message ->
                toastLong(message)
            }
        }
    }

    private fun initGravityForTitle() {
        if (getScreenWidth() < 1082) {
            binding.includeAppBar.toolbarLayout.expandedTitleGravity = Gravity.BOTTOM
        }
    }

    private fun updateAdapterWith(visitedCountryNodes: List<VisitedCountryNode>) {
        viewModel.cityCount = 0
        for (countryNode in visitedCountryNodes) {
            countryNode.isExpanded = false
            viewModel.cityCount += countryNode.childNode.size
        }
        listAdapter.setList(visitedCountryNodes)
        initTitle()
    }
}
