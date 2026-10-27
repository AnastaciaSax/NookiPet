package eng.nookipet.lab7.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import eng.nookipet.lab7.ui.theme.PetPalette
import eng.nookipet.lab7.ui.theme.PetPaletteCatalog
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Atmospheric background with 3 floating soft orbs.
 * Uses RadialGradient instead of real blur — same visual effect,
 * works on all Android versions (real blur requires API 31+).
 *
 * Attach to any layout as a first child matching parent bounds:
 *   <eng.nookipet.lab7.ui.widget.OrbBackgroundView
 *       android:layout_width="match_parent"
 *       android:layout_height="match_parent"/>
 *
 * Then call setPalette() to switch colors when pet changes.
 */
class OrbBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private data class Orb(
        var baseX: Float = 0f,
        var baseY: Float = 0f,
        var radius: Float = 0f,
        var color: Int = Color.TRANSPARENT,
        var phaseOffset: Float = 0f,
        var amplitude: Float = 0f,
        var speed: Float = 1f
    )

    private val orbs = listOf(
        Orb(phaseOffset = 0.0f, amplitude = 0.06f, speed = 1.0f),
        Orb(phaseOffset = 0.33f, amplitude = 0.05f, speed = 0.8f),
        Orb(phaseOffset = 0.66f, amplitude = 0.07f, speed = 1.2f)
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** Animator drives phase from 0 to 1, loops forever. */
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 24_000L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { invalidate() }
    }

    init {
        // Apply default palette so it looks right even before setPalette() is called.
        setPalette(PetPaletteCatalog.MINT)
    }

    fun setPalette(palette: PetPalette) {
        orbs[0].color = palette.orbA
        orbs[1].color = palette.orbB
        orbs[2].color = palette.orbC
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = minOf(w, h).toFloat()
        // Positions — top-left, right-middle, bottom-center (matches mockup)
        orbs[0].apply { baseX = w * 0.10f; baseY = h * 0.08f; radius = size * 0.70f }
        orbs[1].apply { baseX = w * 0.95f; baseY = h * 0.42f; radius = size * 0.65f }
        orbs[2].apply { baseX = w * 0.30f; baseY = h * 0.95f; radius = size * 0.60f }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val phase = animator.animatedFraction
        val w = width.toFloat()
        val h = height.toFloat()

        orbs.forEach { orb ->
            val angle = (phase * orb.speed + orb.phaseOffset) * 2f * PI.toFloat()
            val dx = sin(angle) * w * orb.amplitude
            val dy = cos(angle) * h * orb.amplitude
            val cx = orb.baseX + dx
            val cy = orb.baseY + dy

            paint.shader = RadialGradient(
                cx, cy, orb.radius,
                intArrayOf(orb.color, Color.TRANSPARENT),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, orb.radius, paint)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!animator.isStarted) animator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }
}