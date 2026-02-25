package com.saboon.project_2511sch.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.ViewCompat
import com.google.android.material.progressindicator.CircularProgressIndicator

/**
 * OverscrollActionLayout
 * * A specialized FrameLayout implementing NestedScrollingParent3 to intercept and manage
 * vertical overscroll gestures from nested scrollable children (e.g., RecyclerView).
 * * Architecture & Mechanics:
 * 1. Offset Tracking: Uses 'totalDragY' to accumulate unconsumed scroll deltas.
 * 2. Visual Feedback: Maps displacement to scaling (0-300f) and progress filling (300f-400f).
 * 3. Immediate Execution: Triggers 'onActionTriggered' exactly at 400f without time delays.
 * 4. UX Fluidity: Automatically animates back to zero position upon trigger to clear visual space.
 */
class OverscrollActionLayout(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(context, attrs), NestedScrollingParent3 {

    companion object {
        private const val TAG = "OverscrollActionLayout"
    }

    // State Variables
    private var totalDragY = 0f                 // Accumulated vertical displacement in pixels
    private val friction = 0.5f                 // Resistance factor applied to touch input
    private val threshold = 400f                // Trigger point for the action
    private val scaleThreshold = 300f           // Point where indicators reach full size (1.0 scale)
    private val stickySlop = 80f                // Dead-zone before visual translation starts

    private var isAnimating = false             // Guard flag for the reset animation
    private var isActionTriggeredInSession = false // Ensures single execution per touch gesture

    private var canTriggerTopOverscroll = false    // Set if child is at the top boundary
    private var canTriggerBottomOverscroll = false // Set if child is at the bottom boundary

    /**
     * Listener for overscroll events.
     * @param isTop True for pull-down (top), False for pull-up (bottom).
     */
    var onActionTriggered: ((isTop: Boolean) -> Unit)? = null

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        // Only interested in vertical scrolls. Block new scrolls if animating.
        if ((axes and ViewCompat.SCROLL_AXIS_VERTICAL) == 0 || isAnimating) {
            return false
        }

        // Determine boundary conditions at the moment touch begins
        canTriggerTopOverscroll = !target.canScrollVertically(-1)
        canTriggerBottomOverscroll = !target.canScrollVertically(1)

        Log.d(TAG, "onStartNestedScroll: axes=$axes, type=$type, canTop=$canTriggerTopOverscroll, canBottom=$canTriggerBottomOverscroll")
        return true
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        super.onNestedScrollAccepted(child, target, axes)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        if (type == ViewCompat.TYPE_TOUCH) {
            Log.d(TAG, "onStopNestedScroll: Touch released. totalDragY=$totalDragY")

            // If displacement exists when finger is lifted, return to neutral
            if (totalDragY != 0f) {
                animateToZero(target)
            }

            // Reset per-session flags
            isActionTriggeredInSession = false
            canTriggerTopOverscroll = false
            canTriggerBottomOverscroll = false
        }
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        // Skip logic if no overscroll exists or action already fired
        if (totalDragY == 0f || isActionTriggeredInSession) return

        // Handle 'pull-back' logic: consuming delta to reduce overscroll before child scrolls
        if (totalDragY > 0 && dy > 0) {
            // Returning from Top overscroll
            val consume = dy.coerceAtMost(totalDragY.toInt())
            totalDragY -= consume
            if (totalDragY < stickySlop && totalDragY > 0) totalDragY = 0f
            applyTranslation(target)
            consumed[1] = consume
            updateIndicators(target)
        } else if (totalDragY < 0 && dy < 0) {
            // Returning from Bottom overscroll
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
        // Block processing if not a touch event or action already triggered
        if (type != ViewCompat.TYPE_TOUCH || isActionTriggeredInSession) return

        val isPullingDown = dyUnconsumed < 0
        val isPullingUp = dyUnconsumed > 0

        // Execute only if at boundaries
        val shouldExecute = (isPullingDown && canTriggerTopOverscroll) ||
                (isPullingUp && canTriggerBottomOverscroll)

        if (shouldExecute && dyConsumed == 0) {
            // Accumulate displacement with friction
            totalDragY -= dyUnconsumed * friction
            totalDragY = totalDragY.coerceIn(-threshold, threshold)

            applyTranslation(target)
            updateIndicators(target)
            consumed[1] = dyUnconsumed

            // Trigger Check: Threshold reached
            if (Math.abs(totalDragY) >= threshold) {
                Log.i(TAG, "onNestedScroll: Threshold reached! Triggering action. totalDragY=$totalDragY")
                isActionTriggeredInSession = true
                onActionTriggered?.invoke(totalDragY > 0)

                // Immediately return target to 0 even if finger is still down
                animateToZero(target)
            }
        }
    }

    /**
     * Applies Y-translation to the child target.
     * Subtracts stickySlop to create a 'sticky' feeling at the origin.
     */
    private fun applyTranslation(target: View) {
        if (Math.abs(totalDragY) < stickySlop) {
            target.translationY = 0f
        } else {
            val sign = if (totalDragY > 0) 1 else -1
            target.translationY = totalDragY - (sign * stickySlop)
        }
    }

    /**
     * UI Sync: Updates indicators based on displacement.
     * - Scaling: 0 to 300f
     * - Progress: 300f to 400f
     */
    private fun updateIndicators(target: View) {
        val topContainer = getChildAt(0) as? ViewGroup ?: return
        val bottomContainer = getChildAt(1) as? ViewGroup ?: return

        val currentDragAbs = Math.abs(totalDragY)

        // Scale Mapping: Reaches 1.0 at 300f
        val scaleProgress = (currentDragAbs / scaleThreshold).coerceIn(0f, 1f)

        // Progress Mapping: 0% at 300f, 100% at 400f
        val triggerProgress = if (currentDragAbs > scaleThreshold) {
            (((currentDragAbs - scaleThreshold) / (threshold - scaleThreshold)) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }

        // Apply states based on direction
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

    /**
     * Updates visual properties of an active indicator container.
     */
    private fun updateContainerState(container: ViewGroup, scale: Float, progress: Int) {
        container.scaleX = scale
        container.scaleY = scale
        container.alpha = scale
        findCircularProgress(container)?.progress = progress
    }

    /**
     * Resets visual properties of an inactive indicator container.
     */
    private fun resetContainerState(container: ViewGroup) {
        container.scaleX = 0f
        container.scaleY = 0f
        container.alpha = 0f
        findCircularProgress(container)?.progress = 0
    }

    /**
     * Traverses children to find the Material CircularProgressIndicator.
     */
    private fun findCircularProgress(viewGroup: ViewGroup): CircularProgressIndicator? {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is CircularProgressIndicator) return child
        }
        return null
    }

    /**
     * Smoothly animates the target view's translationY back to 0.
     * Syncs 'totalDragY' during animation to ensure indicators fade out correctly.
     */
    private fun animateToZero(target: View) {
        if (isAnimating) return
        isAnimating = true
        Log.d(TAG, "animateToZero: Starting reset animation from ${target.translationY}")

        ObjectAnimator.ofFloat(target, "translationY", 0f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                // Keep totalDragY in sync with the visual translation so updateIndicators works
                totalDragY = target.translationY + (if (totalDragY > 0) stickySlop else -stickySlop)
                if (Math.abs(target.translationY) < 1f) totalDragY = 0f

                updateIndicators(target)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    totalDragY = 0f
                    isAnimating = false
                    Log.d(TAG, "animateToZero: Reset animation finished.")
                }
            })
            start()
        }
    }

    // Required for NestedScrollingParent3, but logic is handled in the extended signature
    override fun onNestedScroll(t: View, dxC: Int, dyC: Int, dxU: Int, dyU: Int, ty: Int) {}
}