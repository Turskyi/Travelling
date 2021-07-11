package ua.turskyi.travelling.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView
import java.util.*

class ClickableWebView : WebView {
    companion object {
        private const val MAX_CLICK_DURATION = 200
    }

    private var startClickTime: Long = 0

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startClickTime = Calendar.getInstance().timeInMillis
            }
            MotionEvent.ACTION_UP -> {
                val clickDuration: Long = Calendar.getInstance().timeInMillis - startClickTime
                if (clickDuration < MAX_CLICK_DURATION) {
                    /* For this particular app we want the main work to happen
                     * on ACTION_UP rather than ACTION_DOWN. So this is where
                     * we will call performClick(). */
                    super.performClick()
                }
            }
        }
        return true
    }

    /* Because we call this from onTouchEvent, this code will be executed for both
     * normal touch events and for when the system calls this using Accessibility */
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}