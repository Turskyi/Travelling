package ua.turskyi.travelling.features.flags.view.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Color.TRANSPARENT
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener
import org.koin.android.ext.android.inject
import ua.turskyi.travelling.R
import ua.turskyi.travelling.databinding.FragmentFlagBinding
import ua.turskyi.travelling.entities.Traveller
import ua.turskyi.travelling.features.flags.callbacks.FlagsActivityView
import ua.turskyi.travelling.features.flags.callbacks.OnChangeFlagFragmentListener
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import ua.turskyi.travelling.features.flags.view.FlagsActivity.Companion.EXTRA_USER
import ua.turskyi.travelling.features.flags.viewmodel.FlagsFragmentViewModel
import ua.turskyi.travelling.models.Country
import ua.turskyi.travelling.utils.Event
import ua.turskyi.travelling.utils.extensions.observeOnce
import ua.turskyi.travelling.utils.extensions.parcelable
import ua.turskyi.travelling.utils.extensions.showReportDialog
import ua.turskyi.travelling.utils.extensions.toast
import ua.turskyi.travelling.utils.extensions.toastLong
import ua.turskyi.travelling.utils.getDisplayName
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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
            toast(getString(R.string.msg_exception_flag_listener, context))
        }
        if (context is FlagsActivityView) {
            flagsActivityViewListener = context
        } else {
            toast(getString(R.string.msg_exception_flag_listener, context))
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
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photoChooserIntent: Intent? = result.data
                val position: Int? = this.arguments?.getInt(EXTRA_POSITION)
                binding.ivEnlargedFlag.visibility = VISIBLE
                binding.wvFlag.visibility = GONE
                val selectedImageUri: Uri? = photoChooserIntent?.data
                if (selectedImageUri != null && position != null) {
                    createInputStreamAndChangeImage(selectedImageUri, position)
                } else {
                    requireContext().showReportDialog()
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
        val traveller: Traveller? = arguments?.parcelable(EXTRA_USER)
            ?: activity?.intent?.extras?.parcelable(EXTRA_USER)
        if (traveller == null) {
            binding.ivEnlargedFlag.setOnLongClickListener(addSelfieLongClickListener())
            binding.wvFlag.setOnLongClickListener(addSelfieLongClickListener())
        }
    }

    private fun initObservers() {
        val traveller: Traveller? = arguments?.parcelable(EXTRA_USER)
            ?: activity?.intent?.extras?.parcelable(EXTRA_USER)
        viewModel.userId = traveller?.id
        lifecycle.addObserver(viewModel)
        viewModel.errorMessage.observe(this) { event: Event<String> ->
            val message = event.getMessageIfNotHandled()
            if (message != null) {
                toastLong(message)
            }
        }
        viewModel.visibilityLoader.observe(this) { currentVisibility: Int ->
            if (flagsActivityViewListener != null) {
                flagsActivityViewListener!!.setLoaderVisibility(currentVisibility)
            }
        }
        val visitedCountriesObserver: Observer<List<Country>> =
            Observer { countries: List<Country> ->
                val position: Int = this.requireArguments().getInt(EXTRA_POSITION)

                if (position in countries.indices) {
                    if (mChangeFlagListener != null) {
                        mChangeFlagListener!!.onChangeToolbarTitle(countries[position].getDisplayName(requireContext()))
                    }

                    if (!isSelfieAvailable(countries[position].filePath)) {
                        if (countries[position].filePath.isNotEmpty()) {
                            countries[position].filePath = ""
                            if (viewModel.userId == null) {
                                viewModel.updateSelfie(countries[position].id, "")
                            }
                        }
                        binding.ivEnlargedFlag.setOnClickListener(null)
                        binding.wvFlag.setOnTouchListener(null)
                        showTheFlag(countries, position)
                    } else {
                        showSelfie(countries, position)
                        binding.ivEnlargedFlag.setOnClickListener(
                            showFlagClickListener(
                                countries,
                                position
                            )
                        )
                    }
                }
            }
        viewModel.visitedCountries.observe(viewLifecycleOwner, visitedCountriesObserver)
    }

    private fun isSelfieAvailable(filePath: String): Boolean {
        return filePath.isNotBlank() && try {
            if (filePath.startsWith("content://")) {
                val uri = filePath.toUri()
                context?.contentResolver?.openInputStream(uri)?.use { true } ?: false
            } else if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                true
            } else {
                val path = if (filePath.startsWith("file://")) {
                    filePath.removePrefix("file://")
                } else {
                    filePath
                }
                val file = File(path)
                file.exists() && file.length() > 0
            }
        } catch (_: Exception) {
            false
        }
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
                    if (isSelfieAvailable(countries[position].filePath)) {
                        showSelfie(countries, position)
                        view.performClick()
                        // return first clickListener
                        binding.ivEnlargedFlag.setOnClickListener(
                            showFlagClickListener(
                                countries,
                                position
                            )
                        )
                    } else {
                        countries[position].filePath = ""
                        if (viewModel.userId == null) {
                            viewModel.updateSelfie(countries[position].id, "")
                        }
                        binding.ivEnlargedFlag.setOnClickListener(null)
                        binding.wvFlag.setOnTouchListener(null)
                    }
                }
            }
            false
        }
    }

    private fun showSelfieClickListener(countries: List<Country>, position: Int): OnClickListener {
        return OnClickListener {
            if (isSelfieAvailable(countries[position].filePath)) {
                showSelfie(countries, position)
                // return first clickListener
                binding.ivEnlargedFlag.setOnClickListener(showFlagClickListener(countries, position))
            } else {
                countries[position].filePath = ""
                if (viewModel.userId == null) {
                    viewModel.updateSelfie(countries[position].id, "")
                }
                binding.ivEnlargedFlag.setOnClickListener(null)
                binding.wvFlag.setOnTouchListener(null)
            }
        }
    }

    private fun showSelfie(countries: List<Country>, position: Int) {
        binding.ivEnlargedFlag.visibility = VISIBLE
        binding.wvFlag.visibility = GONE
        val thumbnailBuilder: RequestBuilder<Drawable> =
            com.bumptech.glide.Glide.with(binding.ivEnlargedFlag.context)
                .asDrawable()
                .sizeMultiplier(ResourcesCompat.getFloat(resources, R.dimen.thumbnail))
        com.bumptech.glide.Glide.with(this)
            .load(countries[position].filePath)
            .thumbnail(thumbnailBuilder)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.anim_loading)
                    .priority(Priority.IMMEDIATE)
            )
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    countries[position].filePath = ""
                    binding.ivEnlargedFlag.setOnClickListener(null)
                    binding.wvFlag.setOnTouchListener(null)
                    showTheFlag(countries, position)
                    if (viewModel.userId == null) {
                        viewModel.updateSelfie(countries[position].id, "")
                    }
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean = false
            })
            .into(binding.ivEnlargedFlag)
    }

    private fun showTheFlag(countries: List<Country>, position: Int) {
        /**
         * @Description Opens the pictureUri in full size
         *  */
        val uri: Uri = countries[position].flag.toUri()
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

    private fun createInputStreamAndChangeImage(selectedImageUri: Uri, position: Int) {
        val orderBy: String =
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                MediaStore.Images.Media.DATE_TAKEN
            } else {
                MediaStore.Images.Media._ID
            }
        val projectionColumns: Array<String> =
            arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        // Constructs a selection clause with a replaceable parameter
        val selectionClause = "var = ?"
        // Defines a mutable list to contain the selection arguments
        val selectionArgs: MutableList<String> = mutableListOf()
        requireContext().contentResolver.query(
            selectedImageUri,  // The content URI of the image table
            projectionColumns, // The columns to return for each row
            selectionClause, // Selection criteria
            selectionArgs.toTypedArray(), // Selection criteria
            "$orderBy DESC", // The sort order for the returned rows
        )?.use { cursor: Cursor ->
            val inputStream: InputStream? =
                requireContext().contentResolver.openInputStream(selectedImageUri)
            if (inputStream != null && cursor.moveToFirst()) {
                updateFlagImage(cursor, inputStream, position)
            }
        }
        binding.ivEnlargedFlag.visibility = VISIBLE
        binding.wvFlag.visibility = GONE
    }

    private fun updateFlagImage(cursor: Cursor, inputStream: InputStream, position: Int) {
        val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val rawName: String? = cursor.getString(nameIndex)
        val sanitizedName: String = (rawName ?: "temp_flag.jpg")
            .substringAfterLast(File.separatorChar)
            .substringAfterLast('/')
            .replace("..", "")
            .ifBlank { "temp_flag.jpg" }
        val file = File(requireContext().cacheDir, sanitizedName)
        val fileOutputStream: FileOutputStream = file.outputStream()
        fileOutputStream.use { inputStream.copyTo(it) }
        val visitedCountriesObserverForLocalPhotos: Observer<List<Country>> =
            Observer { visitedCountries: List<Country> ->
                viewModel.updateSelfie(
                    id = visitedCountries[position].id,
                    filePath = file.absolutePath,
                )
            }
        viewModel.visitedCountries.observeOnce(
            viewLifecycleOwner,
            visitedCountriesObserverForLocalPhotos
        )
    }
}