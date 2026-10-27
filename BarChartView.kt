package eng.nookipet.lab7.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

/**
 * Lightweight bar chart drawn on Canvas. No external dependencies.
 *
 * Features:
 *   - gradient bars (primary → secondary)
 *   - labels under each bar
 *   - numeric value above bars (only when there are 8 or fewer, otherwise
 *     the labels would overlap)
 *   - grow animation on setData()
 *
 * Usage:
 *   barChart.setColors(primary, secondary, labelColor, valueColor)
 *   barChart.setData(listOf(BarData("Mon", 3f), BarData("Tue", 5f)))
 */
class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** One bar. `label` may be empty — then no label under the bar. */
    data class BarData(val label: String, val value: Float)

    private var bars: List<BarData> = emptyList()

    private var primaryColor = Color.parseColor("#4CC9A0")
    private var secondaryColor = Color.parseColor("#5AA9E6")
    private var labelColor = Color.parseColor("#8592A0")
    private var valueColor = Color.parseColor("#14212B")

    private val density = resources.displayMetrics.density
    private val scaledDensity = resources.displayMetrics.scaledDensity

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 10f * scaledDensity
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 10f * scaledDensity
        isFakeBoldText = true
    }

    private var progress = 0f

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 700L
        interpolator = DecelerateInterpolator()
        addUpdateListener {
            progress = it.animatedValue as Float
            invalidate()
        }
    }

    fun setColors(primary: Int, secondary: Int, label: Int, value: Int) {
        this.primaryColor = primary
        this.secondaryColor = secondary
        this.labelColor = label
        this.valueColor = value
        invalidate()
    }

    fun setData(data: List<BarData>) {
        this.bars = data
        progress = 0f
        animator.cancel()
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (bars.isEmpty()) {
            labelPaint.color = labelColor
            labelPaint.textSize = 12f * scaledDensity
            canvas.drawText(
                "No data for this period",
                width / 2f,
                height / 2f,
                labelPaint
            )
            labelPaint.textSize = 10f * scaledDensity
            return
        }

        val padH = 6f * density
        val padTop = 22f * density         // space for value labels above bars
        val padBottom = 22f * density      // space for bar labels

        val chartWidth = width - 2 * padH
        val chartHeight = height - padTop - padBottom

        val maxValue = bars.maxOf { it.value }.coerceAtLeast(1f)

        val slotWidth = chartWidth / bars.size
        val barWidth = slotWidth * 0.55f
        val cornerRadius = barWidth * 0.35f

        bars.forEachIndexed { i, bar ->
            val cx = padH + slotWidth * i + slotWidth / 2f
            val barHeight = (bar.value / maxValue) * chartHeight * progress
            val top = padTop + chartHeight - barHeight
            val bottom = padTop + chartHeight

            if (barHeight > 0f) {
                barPaint.shader = LinearGradient(
                    0f, top, 0f, bottom,
                    primaryColor, secondaryColor,
                    Shader.TileMode.CLAMP
                )
                val rect = RectF(cx - barWidth / 2f, top, cx + barWidth / 2f, bottom)
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint)
            }

            if (bar.label.isNotBlank()) {
                labelPaint.color = labelColor
                canvas.drawText(bar.label, cx, height - 6f * density, labelPaint)
            }

            // Show numeric value above the bar only when bars are few enough to fit.
            if (bars.size <= 8 && bar.value > 0f) {
                valuePaint.color = valueColor
                val txt = if (bar.value == bar.value.toInt().toFloat())
                    bar.value.toInt().toString()
                else
                    "%.1f".format(bar.value)
                canvas.drawText(txt, cx, top - 6f * density, valuePaint)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }
}