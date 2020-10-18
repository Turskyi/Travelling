package ua.turskyi.travelling.features.flags.view.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color.TRANSPARENT
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
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
import ua.turskyi.travelling.extensions.observeOnce
import ua.turskyi.travelling.features.flags.callback.OnFlagFragmentListener
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.POSITION
import ua.turskyi.travelling.features.flags.viewmodel.FlagsActivityViewModel
import ua.turskyi.travelling.models.Country

class FlagFragment : Fragment(R.layout.fragment_flag) {

    private val viewModel: FlagsActivityViewModel by inject()

    var mListener: OnFlagFragmentListener? = null

   private lateinit var photoPickerResultLauncher: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        photoPickerResultLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photoChooserIntent: Intent? = result.data
                val position = this.arguments?.getInt(POSITION)
                ivEnlarged.visibility = VISIBLE
                wvFlag.visibility = GONE
                val selectedImageUri = photoChooserIntent?.data
                if (selectedImageUri.toString().contains("com.android.providers.media")) {
                    val imageId =
                        selectedImageUri?.lastPathSegment?.takeLastWhile { it.isDigit() }?.toInt()
                    val visitedCountriesObserverForLocalPhotos =
                        Observer<List<Country>> { visitedCountries ->
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
                        }
                    viewModel.visitedCountries.observeOnce(
                        viewLifecycleOwner,
                        visitedCountriesObserverForLocalPhotos
                    )
                } else {
                    val visitedCountriesObserverForCloudPhotos =
                        Observer<List<Country>> { visitedCountries ->
                            position?.let {
                                viewModel.updateSelfie(
                                    visitedCountries[position].id,
                                    selectedImageUri.toString()
                                )
                            }
                        }
                    viewModel.visitedCountries.observeOnce(
                        viewLifecycleOwner,
                        visitedCountriesObserverForCloudPhotos
                    )
                }
            } else {
                splitties.toast.toast(getString(R.string.flag_message_did_not_choose))
            }
        }
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
        initListeners()
        initObservers()
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

    private fun addSelfieLongClickListener(): OnLongClickListener = OnLongClickListener {
        initPhotoPicker()
            return@OnLongClickListener true
        }

    private fun initPhotoPicker() {
        val action: String = Intent.ACTION_OPEN_DOCUMENT
        val intent = Intent(action)
        intent.type = "image/jpeg"
        val intentChooser =  Intent.createChooser(intent, getString(R.string.flag_chooser_title_complete_using))
        photoPickerResultLauncher.launch(intentChooser)
    }

    private fun initListeners() {
        wvFlag.isLongClickable = true
        ivEnlarged.setOnLongClickListener(addSelfieLongClickListener())
        wvFlag.setOnLongClickListener(addSelfieLongClickListener())
    }

    private fun initObservers() {
        val visitedCountriesObserver = Observer<List<Country>> { countries ->
            val position = this.arguments?.getInt(POSITION)
            position?.let {
                mListener?.onChangeToolbarTitle(countries[position].name)
                if (countries[position].selfie.isNullOrEmpty()) {
                    showTheFlag(countries, position)
                } else {
                    showSelfie(countries, position)
                    ivEnlarged.setOnClickListener(showFlagClickListener(countries, position))
                }
            }
        }
        viewModel.visitedCountries.observe(viewLifecycleOwner, visitedCountriesObserver)
    }

    private fun showFlagClickListener(countries: List<Country>, position: Int?):
            OnClickListener = OnClickListener {
        showTheFlag(countries, position)
        /* change clickListener */
        ivEnlarged.setOnClickListener(showSelfieClickListener(countries, position))
        wvFlag.setOnTouchListener(onWebViewClickListener(countries, position))
    }

    private fun onWebViewClickListener(
        countries: List<Country>,
        position: Int?
    ): OnTouchListener {
        return object : OnTouchListener {
            val FINGER_RELEASED = 0
            val FINGER_TOUCHED = 1
            val FINGER_DRAGGING = 2
            val FINGER_UNDEFINED = 3
            private var fingerState = FINGER_RELEASED

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        fingerState =
                            if (fingerState == FINGER_RELEASED) FINGER_TOUCHED else FINGER_UNDEFINED
                    }
                    MotionEvent.ACTION_UP -> when {
                        fingerState != FINGER_DRAGGING -> {
                            fingerState = FINGER_RELEASED
                            /* perform click */
                            showSelfie(countries, position)
                            /* return first clickListener */
                            ivEnlarged.setOnClickListener(
                                showFlagClickListener(
                                    countries,
                                    position
                                )
                            )
                        }
                        else -> fingerState =
                            if (fingerState == FINGER_DRAGGING) FINGER_RELEASED else {
                                FINGER_UNDEFINED
                            }
                    }
                    else -> fingerState = if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                        if (fingerState == FINGER_TOUCHED || fingerState == FINGER_DRAGGING) {
                            FINGER_DRAGGING
                        } else FINGER_UNDEFINED
                    } else FINGER_UNDEFINED
                }
                return false
            }
        }
    }

    private fun showSelfieClickListener(countries: List<Country>, position: Int?):
            OnClickListener = OnClickListener {
        showSelfie(countries, position)
        /* return first clickListener */
        ivEnlarged.setOnClickListener(showFlagClickListener(countries, position))
    }

    private fun showSelfie(
        countries: List<Country>,
        position: Int?
    ) {
        ivEnlarged.visibility = VISIBLE
        wvFlag.visibility = GONE
        position?.let {
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

    private fun showTheFlag(
        countries: List<Country>,
        position: Int?
    ) {
        /**
         * @Description Opens the pictureUri in full size
         *  */
        position?.let {
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
        }
    }
}