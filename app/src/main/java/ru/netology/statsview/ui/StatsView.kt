package ru.netology.statsview.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import ru.netology.statsview.R
import ru.netology.statsview.utils.AndroidUtils
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

class StatsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(
    context,
    attributeSet,
    defStyleAttr,
    defStyleRes
) {

    private var textSize = AndroidUtils.dp(context, 20).toFloat()
    private var lineWidth = AndroidUtils.dp(context, 5)
    private var colors = emptyList<Int>()

    private var radius = 0F
    private var center = PointF(0F, 0F)

    private var oval = RectF()

    private var progress = 0F
    private var valueAnimator: ValueAnimator? = null
    private val animDuration = 2500

    init {
        context.withStyledAttributes(attributeSet, R.styleable.StatsView) {
            textSize = getDimension(R.styleable.StatsView_textSize, textSize)
            lineWidth = getDimension(R.styleable.StatsView_lineWidth, lineWidth.toFloat()).toInt()
            colors = listOf(
                getColor(R.styleable.StatsView_color1, generateRamdomColor()),
                getColor(R.styleable.StatsView_color2, generateRamdomColor()),
                getColor(R.styleable.StatsView_color3, generateRamdomColor()),
                getColor(R.styleable.StatsView_color4, generateRamdomColor()),
            )

        }
    }

    var data: List<Float> = emptyList()
        set(value) {
            field = value
            update()
        }

    private val paint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        strokeWidth = lineWidth.toFloat()
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(
        Paint.ANTI_ALIAS_FLAG
    ).apply {
        textSize = this@StatsView.textSize
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = min(w, h) / 2F - lineWidth / 2
        center = PointF(w / 2F, h / 2F)
        oval = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )

    }

    override fun onDraw(canvas: Canvas) {
        if (data.isEmpty()) {
            return
        }

        var startAngle = -90F

        for ((index, datum) in data.withIndex()) {
            val angle = 360F * datum * sumCutom(data.sum())
            paint.color = colors.getOrNull(index) ?: generateRamdomColor()
            canvas.drawArc(
                oval,
                startAngle,
                angle * progress,
                false,
                paint
            )
            startAngle += angle
            if (progress == 1F) {
                canvas.drawPoint(center.x, center.y - radius, paint)
            }
        }

        canvas.drawText(
            "%.2f%%".format(data.sum() * 100 * sumCutom(data.sum())),
            center.x,
            center.y + textPaint.textSize / 4,
            textPaint
        )

        val lastDot = 1F
        paint.color = colors.getOrElse(0) { generateRamdomColor() }
        canvas.drawArc(oval, startAngle, lastDot, false, paint)

    }

    private fun generateRamdomColor() = Random.nextInt(0xFF000000.toInt(), 0xFFFFFFFF.toInt())

    private fun sumCutom(sum: Float): Float =
        if (sum < 1) 1F else sum.pow(-1)

    private fun update() {
        valueAnimator?.let {
            it.removeAllListeners()
            it.cancel()
        }
        progress = 0F

        valueAnimator = ValueAnimator.ofFloat(0F, 1F).apply {
            addUpdateListener { anim ->
                progress = anim.animatedValue as Float
                invalidate()
            }
            duration = animDuration.toLong()
            interpolator = LinearInterpolator()
        }.also {
            it.start()
        }
    }
}