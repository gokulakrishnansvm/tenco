package com.tenco.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/** Central motion tokens so animations feel like one coherent system. */
object Motion {
    const val FAST = 150
    const val STANDARD = 300
    const val SLOW = 500
    const val EMPHASIZED = 700

    /** Material 3 standard easing. */
    val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val Decelerate: Easing = CubicBezierEasing(0f, 0f, 0f, 1f)

    fun <T> tweenStandard(durationMs: Int = STANDARD): FiniteAnimationSpec<T> =
        tween(durationMs, easing = Standard)

    fun <T> bouncy(): FiniteAnimationSpec<T> =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
}
