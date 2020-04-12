package ua.turskyi.travelling.features.selfie.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_selfie.*
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.selfie.viewmodel.SelfieActivityViewModel

class SelfieActivity : AppCompatActivity( R.layout.activity_selfie) {

    private val viewModel: SelfieActivityViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListeners()
    }
    private fun initView() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorBlack)
    }

    private fun initListeners() {
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}