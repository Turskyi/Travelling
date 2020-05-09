package ua.turskyi.travelling.features.flags.view.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color.TRANSPARENT
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener
import kotlinx.android.synthetic.main.fragment_flag.*
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.features.flags.callback.OnFlagFragmentListener
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.POSITION
import ua.turskyi.travelling.features.flags.viewmodel.FlagsActivityViewModel
import ua.turskyi.travelling.models.Country

class FlagFragment : Fragment(R.layout.fragment_flag) {
    companion object {
        private const val RESULT_CODE_PHOTO_PICKER = 1
    }

    private val viewModel: FlagsActivityViewModel by inject()

    var mListener: OnFlagFragmentListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFlagFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnFlagFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onResume() {
        super.onResume()
        initListener()
        initObservers()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_CODE_PHOTO_PICKER && resultCode == Activity.RESULT_OK) {
            val position = this.arguments?.getInt(POSITION)
            ivEnlarged.visibility = VISIBLE
            wvFlag.visibility = GONE
            val selectedImageUri = data?.data
            Log.d(
                "Selfie==>",
                "before update selfie ${selectedImageUri.toString()}"
            )
            if (selectedImageUri.toString().contains("com.android.providers.media")) {
                Log.d(
                    "Selfie==>",
                    "local"
                )
                val imageId =
                    selectedImageUri?.lastPathSegment?.takeLastWhile { it.isDigit() }?.toInt()
                viewModel.visitedCountries.observe(
                    viewLifecycleOwner,
                    Observer { visitedCountries ->
                        val contentImg = imageId?.let { contentImgId ->
                            position?.let {
                                getContentUriFromUri(
                                    visitedCountries[position].id,
                                    contentImgId,
                                    visitedCountries[position].name,
                                    visitedCountries[position].flag
                                )
                            }
                        }
                        contentImg?.selfie?.let { uri ->
                            position?.let {
                                viewModel.updateSelfie(
                                    visitedCountries[position].id,
                                    uri
                                )
                            }
                        }
                    })
            } else {
                viewModel.visitedCountries.observe(
                    viewLifecycleOwner,
                    Observer { visitedCountries ->
                        position?.let {
                            viewModel.updateSelfie(
                                visitedCountries[position].id,
                                selectedImageUri.toString()
                            )
                        }
                    })
            }
        } else {
            splitties.toast.toast("did not choose anything")
        }
    }

    private fun getContentUriFromUri(id: Int, imageId: Int, name: String, flag: String): Country {
        val columns = arrayOf(MediaStore.Images.Media._ID)

        val orderBy =
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) MediaStore.Images.Media.DATE_TAKEN
            else MediaStore.Images.Media._ID

        /** This cursor will hold the result of the query
        and put all data in Cursor by sorting in descending order */
        val cursor = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            columns, null, null, "$orderBy DESC"
        )
        cursor?.moveToFirst()
        val uriImage = Uri.withAppendedPath(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "" + imageId
        )
        val galleryPicture = Country(id, name, flag, true, uriImage.toString())
        cursor?.close()
        return galleryPicture
    }

    private fun addSelfieLongClickListener(): View.OnLongClickListener =
        View.OnLongClickListener {
            pickThePhoto()
            return@OnLongClickListener true
        }

    private fun pickThePhoto() {
        val action: String = Intent.ACTION_OPEN_DOCUMENT
        val intent = Intent(action)
        intent.type = "image/jpeg"
        startActivityForResult(
            Intent.createChooser(intent, "Complete action using"),
            RESULT_CODE_PHOTO_PICKER
        )
    }

    private fun initListener() {
        wvFlag.isLongClickable = true
        ivEnlarged.setOnLongClickListener(addSelfieLongClickListener())
        wvFlag.setOnLongClickListener(addSelfieLongClickListener())
    }

    private fun initObservers() {
        viewModel.getVisitedCountriesFromDB()
        viewModel.visitedCountries.observe(viewLifecycleOwner, Observer { countries ->
            val position = this.arguments?.getInt(POSITION)
            position?.let {
                mListener?.onChangeToolbarTitle(countries[position].name)
            }
            position?.let {
                if (countries[position].selfie.isNullOrEmpty()) {
                    /**
                     * @Description Opens the pictureUri in full size
                     *  */
                    val uri: Uri = Uri.parse(countries[position].flag)
                    GlideToVectorYou
                        .init()
                        .with(activity)
                        .withListener(object : GlideToVectorYouListener {
                            override fun onLoadFailed() = showFlagInWebView()
                            private fun showFlagInWebView() {
                                ivEnlarged.visibility = GONE
                                wvFlag.webViewClient = WebViewClient()
                                wvFlag.visibility = VISIBLE
                                wvFlag.setBackgroundColor(TRANSPARENT)
                                wvFlag.loadData(
                                    "<html><head><style type='text/css'>" +
                                            "body{margin:auto auto;text-align:center;} img{width:100%25;}" +
                                            " </style></head><body><img src='${countries[position].flag}'/>" +
                                            "</body></html>", "text/html", "UTF-8"
                                )
                            }

                            override fun onResourceReady() {
                                ivEnlarged?.let { ivFlag ->
                                    ivFlag.visibility = VISIBLE
                                    wvFlag.visibility = GONE
                                }
                            }
                        })
                        .setPlaceHolder(R.drawable.anim_loading, R.drawable.ic_broken_image)
                        .load(uri, ivEnlarged)
                } else {
                    val uri: Uri = Uri.parse(countries[position].selfie)
                    Glide.with(this)
                        .load(uri)
                        .thumbnail(0.5F)
                        .apply(
                            RequestOptions()
                                .placeholder(R.drawable.anim_loading)
                                .error(R.drawable.ic_broken_image)
                                .priority(Priority.IMMEDIATE)
                        )
                        .into(ivEnlarged)
                }
            }
        })
    }
}