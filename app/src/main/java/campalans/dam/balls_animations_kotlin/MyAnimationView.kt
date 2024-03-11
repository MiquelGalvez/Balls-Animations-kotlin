package campalans.dam.balls_animations_kotlin

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

class MyAnimationView(context: Context?) : View(context) {
    private val balls: ArrayList<ShapeHolder> = ArrayList()

    init {
        // Animate background color
        val colorAnim: ValueAnimator =
            ObjectAnimator.ofInt(this, "backgroundColor", -0x7f80, -0x7f7f01)
        colorAnim.duration = 3000
        colorAnim.setEvaluator(ArgbEvaluator())
        colorAnim.repeatCount = ValueAnimator.INFINITE
        colorAnim.repeatMode = ValueAnimator.REVERSE
        colorAnim.start()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN &&
            event.action != MotionEvent.ACTION_MOVE
        ) {
            return false
        }
        val newBall: ShapeHolder = addBall(event.x, event.y)
        // Bouncing animation with squash and stretch
        val startY: Float = newBall.y
        val endY = height - 50f
        val h = height.toFloat()
        val eventY = event.y
        val duration = (500 * ((h - eventY) / h)).toInt()
        val bounceAnim: ValueAnimator = ObjectAnimator.ofFloat(newBall, "y", startY, endY)
        bounceAnim.duration = duration.toLong()
        bounceAnim.interpolator = AccelerateInterpolator()
        val squashAnim1: ValueAnimator = ObjectAnimator.ofFloat(
            newBall, "x", newBall.x,
            newBall.x - 25f
        )
        squashAnim1.duration = (duration / 4).toLong()
        squashAnim1.repeatCount = 1
        squashAnim1.repeatMode = ValueAnimator.REVERSE
        squashAnim1.interpolator = DecelerateInterpolator()
        val squashAnim2: ValueAnimator = ObjectAnimator.ofFloat(
            newBall,
            "width",
            newBall.width,
            newBall.width + 50
        )
        squashAnim2.duration = (duration / 4).toLong()
        squashAnim2.repeatCount = 1
        squashAnim2.repeatMode = ValueAnimator.REVERSE
        squashAnim2.interpolator = DecelerateInterpolator()
        val stretchAnim1: ValueAnimator = ObjectAnimator.ofFloat(
            newBall, "y", endY,
            endY + 25f
        )
        stretchAnim1.duration = (duration / 4).toLong()
        stretchAnim1.repeatCount = 1
        stretchAnim1.interpolator = DecelerateInterpolator()
        stretchAnim1.repeatMode = ValueAnimator.REVERSE
        val stretchAnim2: ValueAnimator = ObjectAnimator.ofFloat(
            newBall,
            "height",
            newBall.height,
            newBall.height - 25
        )
        stretchAnim2.duration = (duration / 4).toLong()
        stretchAnim2.repeatCount = 1
        stretchAnim2.interpolator = DecelerateInterpolator()
        stretchAnim2.repeatMode = ValueAnimator.REVERSE
        val bounceBackAnim: ValueAnimator = ObjectAnimator.ofFloat(
            newBall, "y", endY,
            startY + (endY - startY) / 2
        )
        bounceBackAnim.duration = duration.toLong()
        bounceBackAnim.interpolator = DecelerateInterpolator()
        // Sequence the down/squash&stretch/up animations
        val bouncer = AnimatorSet()
        bouncer.play(bounceAnim).before(squashAnim1)
        bouncer.play(squashAnim1).with(squashAnim2)
        bouncer.play(squashAnim1).with(stretchAnim1)
        bouncer.play(squashAnim1).with(stretchAnim2)
        bouncer.play(bounceBackAnim).after(stretchAnim2)
        // Fading animation - remove the ball when the animation is done
        val fadeAnim: ValueAnimator = ObjectAnimator.ofFloat(newBall, "alpha", 1f, 0f)
        fadeAnim.duration = 250
        fadeAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                balls.remove(newBall)
            }
        })
        // Sequence the two animations to play one after the other
        val animatorSet = AnimatorSet()
        animatorSet.play(bouncer).before(fadeAnim)
        // Start the animation
        animatorSet.start()
        return true
    }

    private fun addBall(x: Float, y: Float): ShapeHolder {
        val circle = OvalShape()
        circle.resize(50f, 50f)
        val drawable = ShapeDrawable(circle)
        val shapeHolder = ShapeHolder(drawable)
        shapeHolder.x = x - 25f
        shapeHolder.y = y - 25
        val red = (Math.random() * 255).toInt()
        val green = (Math.random() * 255).toInt()
        val blue = (Math.random() * 255).toInt()
        val color = -0x1000000 or (red shl 16) or (green shl 8) or blue
        val paint = drawable.paint
        val darkColor = -0x1000000 or (red / 4 shl 16) or (green / 4 shl 8) or blue / 4
        val gradient = RadialGradient(
            37.5f, 12.5f,
            50f, color, darkColor, Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        balls.add(shapeHolder)
        return shapeHolder
    }

    override fun onDraw(canvas: Canvas) {
        Log.d("OnDraw", "OnDraw")
        for (i in balls.indices) {
            val shapeHolder: ShapeHolder = balls[i]
            canvas.save()
            canvas.translate(shapeHolder.x, shapeHolder.y)
            shapeHolder.shape.draw(canvas)
            canvas.restore()
        }
    }
}
