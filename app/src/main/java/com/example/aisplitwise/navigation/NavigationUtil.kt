package com.example.aisplitwise.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally


fun slideInOutTransition(
    duration: Int = 400,
    slideDirection: SlideDirection
): Pair<() -> EnterTransition, () -> ExitTransition> {
    return when (slideDirection) {
        SlideDirection.LeftToRight -> {
            {
                slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(duration,easing= FastOutSlowInEasing))
            } to {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(duration,easing= FastOutSlowInEasing))
            }
        }
        SlideDirection.RightToLeft -> {
            {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(duration,easing= FastOutSlowInEasing))
            } to {
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(duration,easing= FastOutSlowInEasing))
            }
        }
    }
}

enum class SlideDirection {
    LeftToRight,
    RightToLeft
}