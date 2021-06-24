package ua.turskyi.travelling.features.home.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ua.turskyi.travelling.R
import ua.turskyi.travelling.databinding.FragmentShareListBottomSheetBinding
import ua.turskyi.travelling.utils.extensions.isFacebookInstalled
import ua.turskyi.travelling.utils.extensions.shareImageViaChooser
import ua.turskyi.travelling.utils.extensions.shareViaFacebook
import ua.turskyi.travelling.utils.extensions.toast

class ShareListBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentShareListBottomSheetBinding? = null
    private val binding get() = _binding!!
    override fun getTheme() = R.style.BottomSheetMenuTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShareListBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initListeners() {
        val toolbarLayout = activity?.findViewById<View>(
            R.id.toolbar_layout)
        binding.ivFacebook.setOnClickListener {
            if (requireContext().isFacebookInstalled()) {
                toolbarLayout?.shareViaFacebook(this)
            }
            else toast(R.string.msg_no_facebook_app)
            dismiss()
        }
        binding.ivOther.setOnClickListener {
            toolbarLayout?.shareImageViaChooser()
            dismiss()
        }
    }
}