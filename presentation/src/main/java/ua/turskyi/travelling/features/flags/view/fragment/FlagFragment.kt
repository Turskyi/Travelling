package ua.turskyi.travelling.features.flags.view.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color.TRANSPARENT
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.databinding.FragmentFlagBinding
import ua.turskyi.travelling.features.flags.callbacks.FlagsActivityView
import ua.turskyi.travelling.features.flags.callbacks.OnChangeFlagFragmentListener
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import ua.turskyi.travelling.features.flags.viewmodel.FlagsFragmentViewModel
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.utils.extensions.observeOnce
import ua.turskyi.travelling.utils.extensions.toast
import ua.turskyi.travelling.utils.extensions.toastLong

class FlagFragment : Fragment() {

    private var _binding: FragmentFlagBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlagsFragmentViewModel by inject()

    private var mChangeFlagListener: OnChangeFlagFragmentListener? = null
    private var flagsActivityViewListener: FlagsActivityView? = null

    private lateinit var photoPickerResultLauncher: ActivityResultLauncher<Intent>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initResultLauncher()
        if (context is OnChangeFlagFragmentListener) {
            mChangeFlagListener = context
        } else {
            throw RuntimeException(getString(R.string.msg_exception_flag_listener, context))
        }
        try {
            flagsActivityViewListener = context as FlagsActivityView?
        } catch (castException: ClassCastException) {
            // in this case the activity does not implement the listener.
            castException.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        mChangeFlagListener = null
        flagsActivityViewListener = null
    }

    override fun onResume() {
        super.onResume()
        initListeners()
        initObservers()
    }

    @SuppressLint("InvalidFragmentVersionForActivityResult")
    private fun initResultLauncher() {
        photoPickerResultLauncher = registerForActivityResult(
            StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photoChooserIntent: Intent? = result.data
                val position = this.arguments?.getInt(EXTRA_POSITION)
                binding.ivEnlargedFlag.visibility = VISIBLE
                binding.wvFlag.visibility = GONE
                val selectedImageUri = photoChooserIntent?.data
                if (selectedImageUri.toString().contains(getString(R.string.media_providers))) {
                    val imageId =
                        selectedImageUri?.lastPathSegment?.takeLastWhile { character -> character.isDigit() }
                            ?.toInt()
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
                    val visitedCountriesObserverForCloudPhotos: Observer<List<Country>> =
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
                toast(R.string.msg_did_not_choose)
            }
        }
    }


    private fun getContentUriFromUri(id: Int, imageId: Int, name: String, flag: String): Country {
        val columns: Array<String> = arrayOf(MediaStore.Images.Media._ID)

        val orderBy: String = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            MediaStore.Images.Media.DATE_TAKEN /* This cursor will hold the result of the query
                and put all data in Cursor by sorting in descending order */
        } else /* This cursor will hold the result of the query
                and put all data in Cursor by sorting in descending order */ {
            MediaStore.Images.Media._ID
        }

        val cursor: Cursor? = requireContext().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            columns, null, null, "$orderBy DESC"
        )
        cursor?.moveToFirst()
        val uriImage: Uri = Uri.withAppendedPath(
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
        intent.type = getString(R.string.image_and_jpeg_type)
        val intentChooser =
            Intent.createChooser(intent, getString(R.string.flag_chooser_title_complete_using))
        photoPickerResultLauncher.launch(intentChooser)
    }

    private fun initListeners() {
        binding.wvFlag.isLongClickable = true
        binding.ivEnlargedFlag.setOnLongClickListener(addSelfieLongClickListener())
        binding.wvFlag.setOnLongClickListener(addSelfieLongClickListener())
    }

    private fun initObservers() {
        lifecycle.addObserver(viewModel)
        viewModel.errorMessage.observe(this) { event ->
            val message = event.getMessageIfNotHandled()
            if (message != null) {
                toastLong(message)
            }
        }
        viewModel.visibilityLoader.observe(this) { currentVisibility ->
            if (flagsActivityViewListener != null) {
                flagsActivityViewListener!!.setLoaderVisibility(currentVisibility)
            }
        }
        val visitedCountriesObserver: Observer<List<Country>> = Observer<List<Country>> { countries: List<Country> ->
            if (this.arguments != null) {
                val position: Int = this.arguments!!.getInt(EXTRA_POSITION)

                if (mChangeFlagListener != null) {
                    mChangeFlagListener!!.onChangeToolbarTitle(countries[position].name)
                }

                if (countries[position].selfie.isEmpty()) {
                    showTheFlag(countries, position)
                } else {
                    showSelfie(countries, position)
                    binding.ivEnlargedFlag.setOnClickListener(showFlagClickListener(countries, position))
                }
            }
        }
        viewModel.visitedCountries.observe(viewLifecycleOwner, visitedCountriesObserver)
    }

    private fun showFlagClickListener(countries: List<Country>, position: Int): OnClickListener {
        return OnClickListener {
            showTheFlag(countries, position)
            // change clickListener
            binding.ivEnlargedFlag.setOnClickListener(showSelfieClickListener(countries, position))
            val wvFlag: WebView = binding.wvFlag
            wvFlag.setOnTouchListener(onWebViewClickListener(countries, position))
        }
    }

    private fun onWebViewClickListener(countries: List<Country>, position: Int): OnTouchListener {
        return OnTouchListener { view: View, motionEvent: MotionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    // perform click
                    showSelfie(countries, position)
                    view.performClick()
                    // return first clickListener
                    binding.ivEnlargedFlag.setOnClickListener(showFlagClickListener(countries, position))
                }
            }
            false
        }
    }

    private fun showSelfieClickListener(countries: List<Country>, position: Int): OnClickListener {
        return OnClickListener {
            showSelfie(countries, position)
            // return first clickListener
            binding.ivEnlargedFlag.setOnClickListener(showFlagClickListener(countries, position))
        }
    }

    private fun showSelfie(countries: List<Country>, position: Int) {
        binding.ivEnlargedFlag.visibility = VISIBLE
        binding.wvFlag.visibility = GONE
        val uri: Uri = Uri.parse(countries[position].selfie)
        Glide.with(this)
            .load(uri)
            .thumbnail(ResourcesCompat.getFloat(resources, R.dimen.thumbnail))
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.anim_loading)
                    .error(R.drawable.ic_broken_image)
                    .priority(Priority.IMMEDIATE)
            )
            .into(binding.ivEnlargedFlag)
    }

    private fun showTheFlag(countries: List<Country>, position: Int) {
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
                    binding.apply {
                        ivEnlargedFlag.visibility = GONE
                        wvFlag.webViewClient = WebViewClient()
                        wvFlag.visibility = VISIBLE
                        wvFlag.setBackgroundColor(TRANSPARENT)
                        wvFlag.loadData(
                            getString(R.string.html_data_flag, countries[position].flag),
                            getString(R.string.mime_type_txt_html),
                            getString(R.string.encoding_utf_8)
                        )
                    }
                }

                override fun onResourceReady() {
                    binding.ivEnlargedFlag.visibility = VISIBLE
                    binding.wvFlag.visibility = GONE
                }
            })
            .setPlaceHolder(R.drawable.anim_loading, R.drawable.ic_broken_image)
            .load(uri, binding.ivEnlargedFlag)
    }
}