package ua.turskyi.travelling.features.flags.view.fragment

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener
import kotlinx.android.synthetic.main.fragment_flag.*
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.flags.viewmodel.FlagsActivityViewModel

class FlagFragment(private val position: Int) : Fragment(R.layout.fragment_flag) {

    /* This interface implemented by the Activity */
    interface OnFlagFragmentListener {
        fun onChangeToolbarTitle(title: String?)
    }

    private val viewModel: FlagsActivityViewModel by inject()

    var mListener: OnFlagFragmentListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFlagFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException(
                "$context must implement OnFlagFragmentListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onResume() {
        super.onResume()
        initObservers()
    }

    private fun initObservers() {
        viewModel.getVisitedCountriesFromDB()
        viewModel.visitedCountries.observe(viewLifecycleOwner, Observer {
            mListener?.onChangeToolbarTitle(it[position].name)
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
                        wvFlag.loadData(
                            "<html><head><style type='text/css'>" +
                                    "body{margin:auto auto;text-align:center;} img{width:100%25;}" +
                                    " </style></head><body><img src='${it[position].flag}'/>" +
                                    "</body></html>", "text/html", "UTF-8"
                        )
                    }

                    override fun onResourceReady() {
                        imageViewEnlarged?.let { ivFlag ->
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