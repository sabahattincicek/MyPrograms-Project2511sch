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

    private var isAnimating = false
    private var isActionTriggered = false

    // Independent gates for top and bottom boundaries
    private var canTriggerTopOverscroll = false
    private var canTriggerBottomOverscroll = false

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        if ((axes and ViewCompat.SCROLL_AXIS_VERTICAL) == 0 || isAnimating || isActionTriggered) {
            return false
        }

        // Logic: Check edge status strictly at the moment of touch start
        // A gate is only opened if the list is ALREADY at that specific edge.
        canTriggerTopOverscroll = !target.canScrollVertically(-1)
        canTriggerBottomOverscroll = !target.canScrollVertically(1)

        Log.d(TAG, "onStartNestedScroll: Gates -> Top:$canTriggerTopOverscroll, Bottom:$canTriggerBottomOverscroll")
        return true
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        super.onNestedScrollAccepted(child, target, axes)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        if (type == ViewCompat.TYPE_TOUCH) {
            if (Math.abs(totalDragY) >= threshold && !isActionTriggered) {
                triggerAction(target)
            } else if (totalDragY != 0f) {
                animateToZero(target)
            }
            // Reset gates on finger lift
            canTriggerTopOverscroll = false
            canTriggerBottomOverscroll = false
        }
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (isActionTriggered || totalDragY == 0f) return

        // Consume delta to reduce active overscroll before letting the child scroll
        if (totalDragY > 0 && dy > 0) {
            val consume = dy.coerceAtMost(totalDragY.toInt())
            totalDragY -= consume
            target.translationY = totalDragY
            consumed[1] = consume
            updateButtonEffects()
        } else if (totalDragY < 0 && dy < 0) {
            val consume = dy.coerceAtLeast(totalDragY.toInt())
            totalDragY -= consume
            target.translationY = totalDragY
            consumed[1] = consume
            updateButtonEffects()
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

        // Direction: dyUnconsumed < 0 means pulling DOWN (Top Edge)
        // Direction: dyUnconsumed > 0 means pulling UP (Bottom Edge)

        val isPullingDown = dyUnconsumed < 0
        val isPullingUp = dyUnconsumed > 0

        // Only proceed if the specific gate for the direction was opened at touch start
        val shouldExecute = (isPullingDown && canTriggerTopOverscroll) ||
                (isPullingUp && canTriggerBottomOverscroll)

        if (shouldExecute && dyConsumed == 0) {
            totalDragY -= dyUnconsumed * friction
            totalDragY = totalDragY.coerceIn(-threshold, threshold)
            target.translationY = totalDragY
            updateButtonEffects()
            consumed[1] = dyUnconsumed
        }
    }

    private fun updateButtonEffects() {
        val prevBtn = getChildAt(0) ?: return
        val nextBtn = getChildAt(1) ?: return
        val progress = Math.abs(totalDragY) / threshold

        if (totalDragY > 0) {
            prevBtn.alpha = progress.coerceIn(0f, 1f)
            prevBtn.scaleX = 0.8f + (0.2f * progress)
            prevBtn.scaleY = 0.8f + (0.2f * progress)
            nextBtn.alpha = 0f
        } else if (totalDragY < 0) {
            nextBtn.alpha = progress.coerceIn(0f, 1f)
            nextBtn.scaleX = 0.8f + (0.2f * progress)
            nextBtn.scaleY = 0.8f + (0.2f * progress)
            prevBtn.alpha = 0f
        } else {
            prevBtn.alpha = 0f
            nextBtn.alpha = 0f
        }
    }

    private fun triggerAction(target: View) {
        isActionTriggered = true
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
            addUpdateListener { updateButtonEffects() }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    totalDragY = 0f
                    isAnimating = false
                    isActionTriggered = false
                    updateButtonEffects()
                }
            })
            start()
        }
    }

    override fun onNestedScroll(t: View, dxC: Int, dyC: Int, dxU: Int, dyU: Int, ty: Int) {}
}