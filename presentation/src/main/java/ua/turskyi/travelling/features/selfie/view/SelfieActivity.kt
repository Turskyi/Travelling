package ua.turskyi.travelling.features.selfie.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.android.synthetic.main.activity_selfie.*
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.selfie.view.adapter.ZoomOutPageTransformer
import ua.turskyi.travelling.features.selfie.view.fragment.SelfieFragment
import ua.turskyi.travelling.features.selfie.viewmodel.SelfieActivityViewModel

class SelfieActivity : AppCompatActivity( R.layout.activity_selfie) {

    private val viewModel: SelfieActivityViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListeners()
        initObservers()
    }

    private fun initObservers() {
       viewModel.fragmentPosition.observe(
           this, Observer { position ->
               pager.setCurrentItem(position, false)
           }
       )
    }

    private fun initView() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBlack)
        viewModel.getVisitedCountriesFromDB()
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        pager.adapter = pagerAdapter
        pager.offscreenPageLimit = 2
        postponeEnterTransition()
        pager.setPageTransformer(ZoomOutPageTransformer())
    }

    private fun initListeners() {
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onBackPressed() {
        if (pager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            pager.currentItem = pager.currentItem - 1
        }
    }
    private inner class ScreenSlidePagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = viewModel.visitedCount

        override fun createFragment(position: Int):  Fragment = SelfieFragment(position)
    }
}