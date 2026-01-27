package com.saboon.project_2511sch.presentation.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.ViewCompat

/**
 * A custom FrameLayout that implements NestedScrollingParent3 to handle offset-based overscroll actions.
 * Mechanics:
 * 1. Tracks vertical overscroll and maps the displacement directly to progress indicators.
 * 2. Threshold (400f) acts as the immediate trigger point without any time-based delay.
 * 3. Indicators scale up based on scaleThreshold (200f) and fill based on a range between 200f and 400f.
 */
class OverscrollActionLayout(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(context, attrs), NestedScrollingParent3 {

    private var totalDragY = 0f
    private val friction = 0.5f
    private val threshold = 400f
    private val scaleThreshold = 300f
    private val stickySlop = 80f

    private var isAnimating = false
    private var isActionTriggeredInSession = false

    private var canTriggerTopOverscroll = false
    private var canTriggerBottomOverscroll = false

    var onActionTriggered: ((isTop: Boolean) -> Unit)? = null

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        if ((axes and ViewCompat.SCROLL_AXIS_VERTICAL) == 0 || isAnimating) {
            return false
        }
        canTriggerTopOverscroll = !target.canScrollVertically(-1)
        canTriggerBottomOverscroll = !target.canScrollVertically(1)
        return true
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        super.onNestedScrollAccepted(child, target, axes)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        if (type == ViewCompat.TYPE_TOUCH) {
            if (totalDragY != 0f) {
                animateToZero(target)
            }
            // Reset session trigger flag when finger is lifted
            isActionTriggeredInSession = false
            canTriggerTopOverscroll = false
            canTriggerBottomOverscroll = false
        }
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (totalDragY == 0f) return

        if (totalDragY > 0 && dy > 0) {
            val consume = dy.coerceAtMost(totalDragY.toInt())
            totalDragY -= consume
            if (totalDragY < stickySlop && totalDragY > 0) totalDragY = 0f
            applyTranslation(target)
            consumed[1] = consume
            updateIndicators(target)
        } else if (totalDragY < 0 && dy < 0) {
            val consume = dy.coerceAtLeast(totalDragY.toInt())
            totalDragY -= consume
            if (Math.abs(totalDragY) < stickySlop && totalDragY < 0) totalDragY = 0f
            applyTranslation(target)
            consumed[1] = consume
            updateIndicators(target)
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        if (type != ViewCompat.TYPE_TOUCH || isActionTriggeredInSession) return

        val isPullingDown = dyUnconsumed < 0
        val isPullingUp = dyUnconsumed > 0
        val shouldExecute = (isPullingDown && canTriggerTopOverscroll) ||
                (isPullingUp && canTriggerBottomOverscroll)

        if (shouldExecute && dyConsumed == 0) {
            totalDragY -= dyUnconsumed * friction
            totalDragY = totalDragY.coerceIn(-threshold, threshold)

            applyTranslation(target)
            updateIndicators(target)
            consumed[1] = dyUnconsumed

            // Immediate trigger when threshold is reached
            if (Math.abs(totalDragY) >= threshold) {
                isActionTriggeredInSession = true
                onActionTriggered?.invoke(totalDragY > 0)
            }
        }
    }

    private fun applyTranslation(target: View) {
        if (Math.abs(totalDragY) < stickySlop) {
            target.translationY = 0f
        } else {
            val sign = if (totalDragY > 0) 1 else -1
            target.translationY = totalDragY - (sign * stickySlop)
        }
    }

    /**
     * Maps the totalDragY to CircularProgressIndicator values.
     * Scale reaches 1.0 at scaleThreshold (200f).
     * Progress fills from 0 to 100 within the 200f to 400f range.
     */
    private fun updateIndicators(target: View) {
        val topContainer = getChildAt(0) as? ViewGroup ?: return
        val bottomContainer = getChildAt(1) as? ViewGroup ?: return

        val currentDragAbs = Math.abs(totalDragY)

        // Scale logic: reaches 1.0 at 200f
        val scaleProgress = (currentDragAbs / scaleThreshold).coerceIn(0f, 1f)

        // Trigger progress logic: 0% at 200f, 100% at 400f
        val triggerProgress = if (currentDragAbs > scaleThreshold) {
            (((currentDragAbs - scaleThreshold) / (threshold - scaleThreshold)) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        if (totalDragY > 0) {
            updateContainerState(topContainer, scaleProgress, triggerProgress)
            resetContainerState(bottomContainer)
        } else if (totalDragY < 0) {
            updateContainerState(bottomContainer, scaleProgress, triggerProgress)
            resetContainerState(topContainer)
        } else {
            resetContainerState(topContainer)
            resetContainerState(bottomContainer)
        }
    }

    private fun updateContainerState(container: ViewGroup, scale: Float, progress: Int) {
        container.scaleX = scale
        container.scaleY = scale
        container.alpha = scale
        findCircularProgress(container)?.progress = progress
    }

    private fun resetContainerState(container: ViewGroup) {
        container.scaleX = 0f
        container.scaleY = 0f
        container.alpha = 0f
        findCircularProgress(container)?.progress = 0
    }

    private fun findCircularProgress(viewGroup: ViewGroup): CircularProgressIndicator? {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is CircularProgressIndicator) return child
        }
        return null
    }

    private fun animateToZero(target: View) {
        if (isAnimating) return
        isAnimating = true

        ObjectAnimator.ofFloat(target, "translationY", 0f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                updateIndicators(target)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    totalDragY = 0f
                    isAnimating = false
                }
            })
            start()
        }
    }

    override fun onNestedScroll(t: View, dxC: Int, dyC: Int, dxU: Int, dyU: Int, ty: Int) {}
}