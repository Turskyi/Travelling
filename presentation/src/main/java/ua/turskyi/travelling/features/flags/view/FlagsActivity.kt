package ua.turskyi.travelling.features.flags.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_flags.*
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.flags.callback.OnFlagFragmentListener
import ua.turskyi.travelling.features.flags.view.adapter.ScreenSlidePagerAdapter
import ua.turskyi.travelling.features.flags.view.adapter.ZoomOutPageTransformer
import ua.turskyi.travelling.common.view.InfoDialog

class FlagsActivity: AppCompatActivity(R.layout.activity_flags), OnFlagFragmentListener {

    companion object{
        const val POSITION = "position"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListeners()
    }

    override fun onChangeToolbarTitle(title: String?) {
        tvToolbarTitle.text = title
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_info, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        openInfoDialog()
        return true
    }

    private fun openInfoDialog() {
        val infoDialog = InfoDialog.newInstance(getString(R.string.txt_info_flags),false)
        infoDialog.show(supportFragmentManager, "info dialog")
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