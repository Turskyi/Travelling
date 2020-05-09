package ua.turskyi.travelling.features.flags.view

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_flag.*
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.flags.callback.OnFlagFragmentListener
import ua.turskyi.travelling.features.flags.view.adapter.ScreenSlidePagerAdapter
import ua.turskyi.travelling.features.flags.view.adapter.ZoomOutPageTransformer

class FlagsActivity: AppCompatActivity( R.layout.activity_flag), OnFlagFragmentListener {

    companion object{
        const val POSITION = "position"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListeners()
    }

    override fun onChangeToolbarTitle(title: String?) {
        Log.d("POSITION===>", "$title")
        tvToolbarTitle.text = title
    }

    private fun initView() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBlack)
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        pager.adapter = pagerAdapter
        pager.offscreenPageLimit = 4
        pager.setPageTransformer(ZoomOutPageTransformer())
        val getBundle: Bundle? = this.intent.extras
        val startPosition = getBundle?.getInt(POSITION)
        startPosition?.let { pager.post { pager.setCurrentItem(it, true) } }
        postponeEnterTransition()
    }

    private fun initListeners() = toolbar.setNavigationOnClickListener { onBackPressed() }
}