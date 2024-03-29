package ua.turskyi.travelling.features.flags.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ua.turskyi.travelling.R
import ua.turskyi.travelling.databinding.ActivityFlagsBinding
import ua.turskyi.travelling.features.flags.callbacks.FlagsActivityView
import ua.turskyi.travelling.features.flags.callbacks.OnChangeFlagFragmentListener
import ua.turskyi.travelling.features.flags.view.adapter.FlagsAdapter
import ua.turskyi.travelling.features.flags.view.adapter.ZoomOutPageTransformer
import ua.turskyi.travelling.utils.extensions.openInfoDialog
import ua.turskyi.travelling.utils.extensions.toast

class FlagsActivity : AppCompatActivity(R.layout.activity_flags), OnChangeFlagFragmentListener,
    FlagsActivityView {

    companion object {
        const val EXTRA_POSITION = "ua.turskyi.travelling.POSITION"
        const val EXTRA_ITEM_COUNT = "ua.turskyi.travelling.ITEM_COUNT"
    }

    private var getBundle: Bundle? = null
    private lateinit var binding: ActivityFlagsBinding
    private lateinit var flagsAdapter: FlagsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getBundle = this@FlagsActivity.intent.extras
        if (getBundle != null) {
            initView()
            initListeners()
            initObserver()
        } else {
            toast(R.string.msg_not_found)
            finish()
        }

    }

    override fun onChangeToolbarTitle(title: String?) {
        binding.tvToolbarTitle.text = title
    }

    override fun onCreatePanelMenu(featureId: Int, menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_info, menu)
        return super.onCreatePanelMenu(featureId, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        openInfoDialog(R.string.txt_info_flags)
        return true
    }

    override fun getItemCount(): Int = if (getBundle != null) {
        getBundle!!.getInt(EXTRA_ITEM_COUNT)
    } else {
        0
    }

    override fun setLoaderVisibility(currentVisibility: Int) {
        binding.pb.visibility = currentVisibility
    }

    private fun initView() {
        getBundle = this@FlagsActivity.intent.extras
        binding = ActivityFlagsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBlack)
        initAdapter()
    }

    private fun initAdapter() {
        /* flagsAdapter cannot by implemented in koin modules
   since "view pager 2" required exact context */
        flagsAdapter = FlagsAdapter(this)
        binding.pager.apply {
            adapter = flagsAdapter
            offscreenPageLimit = 4
            setPageTransformer(ZoomOutPageTransformer())
            val startPosition: Int? = getBundle?.getInt(EXTRA_POSITION)
            if (startPosition != null) {
                post { setCurrentItem(startPosition, true) }
            }
        }
    }

    private fun initListeners() = binding.toolbar.setNavigationOnClickListener {
        @Suppress("DEPRECATION")
        onBackPressed()
    }

    private fun initObserver() = lifecycle.addObserver(flagsAdapter)
}