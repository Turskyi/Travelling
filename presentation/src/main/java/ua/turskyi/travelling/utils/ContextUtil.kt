package ua.turskyi.travelling.utils

import android.content.Context
import android.content.res.TypedArray
import ua.turskyi.travelling.R

class ContextUtil private constructor() {
    companion object {
        private var context: Context? = null

        fun init(context: Context) {
            Companion.context = context.applicationContext
        }

        /**
         * ApplicationContext
         * @return ApplicationContext
         */
        fun getContext(): Context? {
            if (context != null) return context
            throw NullPointerException("u should init first")
        }

        fun getToolbarHeight(context: Context): Int {
            val styledAttributes: TypedArray =
                context.theme.obtainStyledAttributes(intArrayOf(R.attr.actionBarSize))
            val toolbarHeight = styledAttributes.getDimension(0, 0f).toInt()
            styledAttributes.recycle()
            return toolbarHeight
        }
    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}
