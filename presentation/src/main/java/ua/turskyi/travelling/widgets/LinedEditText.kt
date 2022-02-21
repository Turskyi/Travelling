package ua.turskyi.travelling.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import ua.turskyi.travelling.R

open class LinedEditText(context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatEditText(context, attrs) {
    private val mPaint: Paint = Paint()

    init {
        mPaint.style = Paint.Style.STROKE
        mPaint.color = -0x80000000
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) mPaint.color =
            context.getColor(R.color.colorLightGrey)
    }

    override fun onDraw(canvas: Canvas) {
        val nextCount: Int = lineCount
        val right: Int = right
        val paddingTop: Int = paddingTop
        val paddingBottom: Int = paddingBottom
        val paddingLeft: Int = paddingLeft
        val paddingRight: Int = paddingRight
        val height: Int = height
        val lineHeight: Int = lineHeight
        val startCount: Int = (height - paddingTop - paddingBottom) / lineHeight

        for (firstCount: Int in 0 until startCount) {
            val baseline: Int = lineHeight * (firstCount + 1) + paddingTop
            canvas.drawLine(
                paddingLeft.toFloat(),
                baseline.toFloat(), (right - paddingRight).toFloat(), baseline.toFloat(), mPaint
            )
        }

        for (secondCount: Int in startCount until nextCount) {
            val baseline = lineHeight * (secondCount + 1) + paddingTop
            canvas.drawLine(
                paddingLeft.toFloat(),
                baseline.toFloat(), (right - paddingRight).toFloat(), baseline.toFloat(), mPaint
            )
        }
        super.onDraw(canvas)
    }
}