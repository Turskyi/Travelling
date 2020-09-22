package ua.turskyi.travelling.features.home.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_bottom_sheet.*
import splitties.toast.toast
import ua.turskyi.travelling.R
import ua.turskyi.travelling.extensions.isFacebookInstalled
import ua.turskyi.travelling.extensions.shareImageViaChooser
import ua.turskyi.travelling.extensions.shareViaFacebook

class ShareListBottomSheetDialog : BottomSheetDialogFragment() {

    override fun getTheme() = R.style.BottomSheetMenuTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.layout_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        ivFacebook.setOnClickListener {
            if (requireContext().isFacebookInstalled()) shareViaFacebook()
            else toast(R.string.toast_no_facebook_app)
            dismiss()
        }
        ivOther.setOnClickListener {
            requireContext().shareImageViaChooser()
            dismiss()
        }
    }
}