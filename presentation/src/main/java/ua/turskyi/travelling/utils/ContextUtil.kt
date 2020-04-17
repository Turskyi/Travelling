package ua.turskyi.travelling.utils

import android.content.Context

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
    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}
