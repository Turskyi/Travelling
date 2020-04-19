package ua.turskyi.travelling.features.flags.view.fragment

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener
import kotlinx.android.synthetic.main.fragment_selfie.*
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.flags.viewmodel.FlagsActivityViewModel

class FlagFragment(private val position: Int) : Fragment(R.layout.fragment_selfie) {

    private val viewModel: FlagsActivityViewModel by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
    }

    private fun initObservers() {
        viewModel.getVisitedCountriesFromDB()
        viewModel.visitedCountries.observe(viewLifecycleOwner, Observer {
            /**
             * @Description Opens the pictureUri in full size
             *  */
            val uri: Uri = Uri.parse(it[position].flag)

            GlideToVectorYou
                .init()
                .with(activity)
                .withListener(object : GlideToVectorYouListener {
                    override fun onLoadFailed() = showFlagInWebView()
                    private fun showFlagInWebView() {
                        imageViewEnlarged.visibility = View.GONE
                        wvFlag.webViewClient = WebViewClient()
                        wvFlag.visibility = View.VISIBLE
                        wvFlag.setBackgroundColor(Color.TRANSPARENT)
                        wvFlag.setInitialScale(8)
                        wvFlag.loadUrl(it[position].flag)
                    }

                    override fun onResourceReady() {
                        imageViewEnlarged?.let {ivFlag ->
                            ivFlag.visibility = View.VISIBLE
                            wvFlag.visibility = View.GONE
                        }
                    }
                })
                .setPlaceHolder(R.drawable.anim_loading, R.drawable.ic_broken_image)
                .load(uri, imageViewEnlarged)
        })
    }
}