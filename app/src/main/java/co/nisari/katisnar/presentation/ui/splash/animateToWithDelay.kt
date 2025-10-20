package co.nisari.katisnar.presentation.ui.splash

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import androidx.core.animation.doOnEnd
import kotlinx.coroutines.*

fun ProgressBar.animateToWithDelay(
    target: Int,
    delayMs: Long = 300L,
    durationMs: Long = 600L
) {
    val clamped = target.coerceIn(0, max)
    this.postDelayed({
        val anim = ValueAnimator.ofInt(progress, clamped).apply {
            duration = durationMs
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { valueAnim ->
                this@animateToWithDelay.progress = valueAnim.animatedValue as Int
            }
        }
        anim.start()
    }, delayMs)
}
