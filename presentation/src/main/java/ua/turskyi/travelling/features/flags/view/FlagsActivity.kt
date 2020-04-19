package ua.turskyi.travelling.features.flags.view

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.android.synthetic.main.activity_selfie.*
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.flags.view.adapter.ZoomOutPageTransformer
import ua.turskyi.travelling.features.flags.view.fragment.FlagFragment
import ua.turskyi.travelling.features.flags.viewmodel.FlagsActivityViewModel
import ua.turskyi.travelling.features.home.view.ui.HomeActivity.Companion.LOG_ID

class FlagsActivity: AppCompatActivity( R.layout.activity_selfie) {

    companion object{
        const val POSITION = "position"
    }

    private val viewModel: FlagsActivityViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListeners()
    }

    private fun initView() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBlack)
        viewModel.getVisitedCountriesFromDB()
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        pager.adapter = pagerAdapter
        pager.offscreenPageLimit = 4
        pager.setPageTransformer(ZoomOutPageTransformer())
        val getBundle: Bundle? = this.intent.extras
        Log.d(LOG_ID, "${getBundle?.getInt(POSITION)}")
        val startPosition = getBundle?.getInt(POSITION)
        startPosition?.let { pager.post { pager.setCurrentItem(it, true) } }
        postponeEnterTransition()
    }

    private fun initListeners() = toolbar.setNavigationOnClickListener { onBackPressed() }

    private inner class ScreenSlidePagerAdapter(activity: AppCompatActivity) :
        FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = viewModel.visitedCount
        override fun createFragment(position: Int):  Fragment = FlagFragment(position)
    }
}