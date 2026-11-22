package ua.turskyi.travelling.utils.extensions

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun Fragment.toast(@StringRes msgResId: Int) = requireContext().toast(getString(msgResId))
fun Fragment.toast(msg: String) = requireContext().toast(msg)
fun Fragment.toastLong(@StringRes msgResId: Int) = requireContext().toastLong(getString(msgResId))
fun Fragment.toastLong(msg: String) = requireContext().toastLong(msg)
