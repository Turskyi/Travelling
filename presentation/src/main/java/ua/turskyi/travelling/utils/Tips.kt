package ua.turskyi.travelling.utils

import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast

object Tips {
    /**
     *  Toast
     * @param message
     */
    @JvmOverloads
    fun show(message: String, duration: Int = Toast.LENGTH_SHORT) {
        val toast = Toast(ContextUtil.getContext())
        toast.duration = duration
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.view = createTextToastView(message)
        toast.show()
    }

    /**
     *  Toast View
     * @param message
     * @return View
     */
    private fun createTextToastView(message: String): FrameLayout? {
        val roundRectangle = dp2px(6f).toFloat()
        val shape = RoundRectShape(
            floatArrayOf(
                roundRectangle,
                roundRectangle,
                roundRectangle,
                roundRectangle,
                roundRectangle,
                roundRectangle,
                roundRectangle,
                roundRectangle
            ), null, null
        )
        val drawable = ShapeDrawable(shape)
        drawable.paint.color = Color.argb(225, 240, 240, 240)
        drawable.paint.style = Paint.Style.FILL
        drawable.paint.isAntiAlias = true
        drawable.paint.flags = Paint.ANTI_ALIAS_FLAG

        val layout = ContextUtil.getContext()?.let { FrameLayout(it) }
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layout?.layoutParams = layoutParams
        layout?.setPadding(dp2px(16f), dp2px(12f), dp2px(16f), dp2px(12f))
        layout?.background = drawable
        val textView = TextView(ContextUtil.getContext())
        textView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        textView.textSize = 15f
        textView.text = message
        textView.setLineSpacing(dp2px(4f).toFloat(), 1f)
        textView.setTextColor(Color.BLACK)
        layout?.addView(textView)
        return layout
    }

    private fun dp2px(dpValue: Float): Int {
        val scale: Float? = ContextUtil.getContext()?.resources?.displayMetrics?.density
        return (dpValue * scale!! + 0.5f).toInt()
    }
}