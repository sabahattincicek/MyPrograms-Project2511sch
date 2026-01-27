package com.saboon.project_2511sch.presentation.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.ViewCompat

class OverscrollActionLayout(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(context, attrs), NestedScrollingParent3 {

    private val TAG = "OverscrollLog"
    private var totalDragY = 0f
    private val friction = 0.5f
    private val threshold = 400f

    // Increased sticky slop as requested
    private val stickySlop = 80f

    private var isAnimating = false
    private var isActionTriggered = false

    private var canTriggerTopOverscroll = false
    private var canTriggerBottomOverscroll = false

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        if ((axes and ViewCompat.SCROLL_AXIS_VERTICAL) == 0 || isAnimating || isActionTriggered) {
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
            if (Math.abs(totalDragY) >= threshold && !isActionTriggered) {
                triggerAction(target)
            } else if (totalDragY != 0f && !isActionTriggered) {
                animateToZero(target)
            }
            canTriggerTopOverscroll = false
            canTriggerBottomOverscroll = false
        }
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (isActionTriggered || totalDragY == 0f) return

        if (totalDragY > 0 && dy > 0) {
            val consume = dy.coerceAtMost(totalDragY.toInt())
            totalDragY -= consume
            if (totalDragY < stickySlop && totalDragY > 0) totalDragY = 0f
            applyTranslation(target)
            consumed[1] = consume
            updateButtonEffects(target)
        } else if (totalDragY < 0 && dy < 0) {
            val consume = dy.coerceAtLeast(totalDragY.toInt())
            totalDragY -= consume
            if (Math.abs(totalDragY) < stickySlop && totalDragY < 0) totalDragY = 0f
            applyTranslation(target)
            consumed[1] = consume
            updateButtonEffects(target)
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
        if (type != ViewCompat.TYPE_TOUCH || isActionTriggered) return

        val isPullingDown = dyUnconsumed < 0
        val isPullingUp = dyUnconsumed > 0

        val shouldExecute = (isPullingDown && canTriggerTopOverscroll) ||
                (isPullingUp && canTriggerBottomOverscroll)

        if (shouldExecute && dyConsumed == 0) {
            totalDragY -= dyUnconsumed * friction
            totalDragY = totalDragY.coerceIn(-threshold, threshold)

            applyTranslation(target)
            updateButtonEffects(target)
            consumed[1] = dyUnconsumed
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

    private fun updateButtonEffects(target: View) {
        val prevBtn = getChildAt(0) ?: return
        val nextBtn = getChildAt(1) ?: return

        val currentTranslation = target.translationY
        // Normalized progress based on visible translation
        val maxVisibleTranslation = threshold - stickySlop
        val progress = (Math.abs(currentTranslation) / maxVisibleTranslation).coerceIn(0f, 1f)

        // When action is triggered, keep buttons visible at max alpha/scale
        val finalAlpha = if (isActionTriggered) 1f else progress

        if (totalDragY > 0) {
            prevBtn.alpha = finalAlpha
            prevBtn.scaleX = 0.8f + (0.2f * finalAlpha)
            prevBtn.scaleY = 0.8f + (0.2f * finalAlpha)
            nextBtn.alpha = 0f
        } else if (totalDragY < 0) {
            nextBtn.alpha = finalAlpha
            nextBtn.scaleX = 0.8f + (0.2f * finalAlpha)
            nextBtn.scaleY = 0.8f + (0.2f * finalAlpha)
            prevBtn.alpha = 0f
        } else {
            prevBtn.alpha = 0f
            nextBtn.alpha = 0f
        }
    }

    private fun triggerAction(target: View) {
        isActionTriggered = true
        // Ensure buttons are at full visibility for the duration
        updateButtonEffects(target)

        target.postDelayed({
            animateToZero(target)
        }, 3000)
    }

    private fun animateToZero(target: View) {
        if (isAnimating) return
        isAnimating = true

        ObjectAnimator.ofFloat(target, "translationY", 0f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                // Sync internal state with animation during recovery
                if (isActionTriggered) {
                    // While waiting or returning from action, map translation back to totalDragY
                    val sign = if (totalDragY > 0) 1 else -1
                    totalDragY = Math.abs(target.translationY) + stickySlop
                    totalDragY *= sign
                }
                updateButtonEffects(target)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    totalDragY = 0f
                    isAnimating = false
                    isActionTriggered = false
                    updateButtonEffects(target)
                }
            })
            start()
        }
    }

    override fun onNestedScroll(t: View, dxC: Int, dyC: Int, dxU: Int, dyU: Int, ty: Int) {}
}