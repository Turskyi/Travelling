package ua.turskyi.travelling.widgets.multilinecollapsingtoolbar

import android.content.Context
import android.content.res.TypedArray
import ua.turskyi.travelling.R

internal object ThemeUtils {
    private val APPCOMPAT_CHECK_ATTRS = intArrayOf(R.attr.colorPrimary)

    fun checkAppCompatTheme(context: Context) {
        val typedArray: TypedArray = context.obtainStyledAttributes(APPCOMPAT_CHECK_ATTRS)
        val failed: Boolean = !typedArray.hasValue(0)
        typedArray.recycle()
        require(!failed) {
            ("You need to use a Theme.AppCompat theme "
                    + "(or descendant) with the design library.")
        }
    }
}
