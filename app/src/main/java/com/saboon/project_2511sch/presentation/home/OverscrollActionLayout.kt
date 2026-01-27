package com.saboon.project_2511sch.presentation.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.ViewCompat

/**
 * A custom FrameLayout that implements NestedScrollingParent3 to handle overscroll actions.
 * It provides a "pull-to-action" mechanism both at the top and bottom of a nested scrollable child (e.g., RecyclerView).
 * * Mechanics:
 * 1. Tracks vertical overscroll using friction-based accumulation.
 * 2. Scales and translates indicator containers based on scroll depth.
 * 3. Triggers a timed action (ValueAnimator) once the displacement exceeds a defined threshold.
 * 4. Resets state if the user releases the touch before the action duration completes.
 */
class OverscrollActionLayout(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(context, attrs), NestedScrollingParent3 {

    // Internal state tracking for the vertical displacement (pixels)
    private var totalDragY = 0f

    // Friction applied to the raw scroll delta to simulate resistance
    private val friction = 0.5f

    // The displacement required to initiate the timed action process
    private val threshold = 400f

    // The displacement at which the visual scaling of indicators reaches 100%
    private val scaleThreshold = 200f

    // A dead-zone or 'sticky' area before the translation starts to visually affect the target
    private val stickySlop = 80f

    // Duration for the circular progress indicator to fill (milliseconds)
    private val actionDuration = 1000L

    // Animation flag to prevent concurrent reset animations
    private var isAnimating = false

    // State flag indicating the timed process (ValueAnimator) is currently running
    private var isActionProcessing = false

    // Reference to the active animator to allow cancellation on touch release
    private var currentActionAnimator: ValueAnimator? = null

    // Flags to determine if the child view is at its scrolling boundaries
    private var canTriggerTopOverscroll = false
    private var canTriggerBottomOverscroll = false

    /**
     * Callback triggered when the action is successfully completed.
     * @param isTop True if triggered from the top (pull-down), false if from bottom (pull-up).
     */
    var onActionTriggered: ((isTop: Boolean) -> Unit)? = null

    /**
     * Decides whether this layout should participate in a nested scroll.
     * We only accept vertical scrolls and only if no animations or actions are currently in progress.
     */
    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        if ((axes and ViewCompat.SCROLL_AXIS_VERTICAL) == 0 || isAnimating || isActionProcessing) {
            return false
        }
        // Capture scrollability state at the start of the gesture
        canTriggerTopOverscroll = !target.canScrollVertically(-1)
        canTriggerBottomOverscroll = !target.canScrollVertically(1)
        return true
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        super.onNestedScrollAccepted(child, target, axes)
    }

    /**
     * Handles the end of the scroll gesture (finger lifted).
     * If the action hasn't completed, we must cancel the progress and animate the view back to zero.
     */
    override fun onStopNestedScroll(target: View, type: Int) {
        if (type == ViewCompat.TYPE_TOUCH) {
            if (isActionProcessing) {
                cancelActionProcess()
            }

            if (totalDragY != 0f) {
                animateToZero(target)
            }
            canTriggerTopOverscroll = false
            canTriggerBottomOverscroll = false
        }
    }

    /**
     * Intercepts scroll delta before the child view consumes it.
     * Used to "re-capture" the scroll when the user is moving back towards the neutral position.
     */
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (totalDragY == 0f) return

        // If pulling back down from a bottom overscroll or pulling back up from a top overscroll
        if (totalDragY > 0 && dy > 0) {
            val consume = dy.coerceAtMost(totalDragY.toInt())
            totalDragY -= consume
            // Reset to 0 if within sticky zone and not processing an action
            if (totalDragY < stickySlop && totalDragY > 0 && !isActionProcessing) totalDragY = 0f
            applyTranslation(target)
            consumed[1] = consume
            updateIndicators(target)

            // Cancel action if user moves back above the threshold
            if (isActionProcessing && Math.abs(totalDragY) < threshold) {
                cancelActionProcess()
            }
        } else if (totalDragY < 0 && dy < 0) {
            val consume = dy.coerceAtLeast(totalDragY.toInt())
            totalDragY -= consume
            if (Math.abs(totalDragY) < stickySlop && totalDragY < 0 && !isActionProcessing) totalDragY = 0f
            applyTranslation(target)
            consumed[1] = consume
            updateIndicators(target)

            if (isActionProcessing && Math.abs(totalDragY) < threshold) {
                cancelActionProcess()
            }
        }
    }

    /**
     * Handles unconsumed scroll delta from the child, which represents the overscroll magnitude.
     */
    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        if (type != ViewCompat.TYPE_TOUCH) return

        val isPullingDown = dyUnconsumed < 0
        val isPullingUp = dyUnconsumed > 0
        val shouldExecute = (isPullingDown && canTriggerTopOverscroll) ||
                (isPullingUp && canTriggerBottomOverscroll)

        if (shouldExecute && dyConsumed == 0) {
            // Apply friction and clamp to absolute threshold
            totalDragY -= dyUnconsumed * friction
            totalDragY = totalDragY.coerceIn(-threshold, threshold)

            applyTranslation(target)
            updateIndicators(target)
            consumed[1] = dyUnconsumed

            // Initiate the timed action if threshold is reached
            if (Math.abs(totalDragY) >= threshold && !isActionProcessing) {
                startActionProcess(totalDragY > 0, target)
            }
        }
    }

    /**
     * Applies the calculated translation to the target view (RecyclerView).
     * Accounts for the sticky slop to provide a natural "snap" feel at the start.
     */
    private fun applyTranslation(target: View) {
        if (Math.abs(totalDragY) < stickySlop && !isActionProcessing) {
            target.translationY = 0f
        } else {
            val sign = if (totalDragY > 0) 1 else -1
            target.translationY = totalDragY - (sign * stickySlop)
        }
    }

    /**
     * Updates visual indicators (LinearLayouts) based on current translation.
     * Handles scaling, alpha, and visibility of the containers at index 0 (top) and 1 (bottom).
     */
    private fun updateIndicators(target: View) {
        val topContainer = getChildAt(0) ?: return
        val bottomContainer = getChildAt(1) ?: return

        val currentTranslationAbs = Math.abs(target.translationY)
        val maxScaleTranslation = scaleThreshold - stickySlop

        // Normalize progress for scaling based on the specific scaleThreshold
        val scaleProgress = if (maxScaleTranslation > 0) {
            (currentTranslationAbs / maxScaleTranslation).coerceIn(0f, 1f)
        } else 0f

        if (totalDragY > 0) {
            val scale = if (isActionProcessing) 1f else scaleProgress
            topContainer.scaleX = scale
            topContainer.scaleY = scale
            topContainer.alpha = scale

            bottomContainer.scaleX = 0f
            bottomContainer.scaleY = 0f
            bottomContainer.alpha = 0f
        } else if (totalDragY < 0) {
            val scale = if (isActionProcessing) 1f else scaleProgress
            bottomContainer.scaleX = scale
            bottomContainer.scaleY = scale
            bottomContainer.alpha = scale

            topContainer.scaleX = 0f
            topContainer.scaleY = 0f
            topContainer.alpha = 0f
        } else {
            // Reset both when neutral
            topContainer.scaleX = 0f
            topContainer.scaleY = 0f
            topContainer.alpha = 0f
            bottomContainer.scaleX = 0f
            bottomContainer.scaleY = 0f
            bottomContainer.alpha = 0f
        }
    }

    /**
     * Starts the timed ValueAnimator that fills the progress indicator.
     * @param isTop Direction of the overscroll.
     * @param target The child view to be reset upon completion.
     */
    private fun startActionProcess(isTop: Boolean, target: View) {
        isActionProcessing = true
        // Container mapping: Index 0 is Top, Index 1 is Bottom
        val container = (if (isTop) getChildAt(0) else getChildAt(1)) as? ViewGroup ?: return
        val progressBar = findCircularProgress(container) ?: return

        container.scaleX = 1f
        container.scaleY = 1f
        container.alpha = 1f
        progressBar.progress = 0

        currentActionAnimator = ValueAnimator.ofInt(0, 100).apply {
            duration = actionDuration
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                progressBar.progress = animator.animatedValue as Int
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // Verification that the action was held until the end
                    if (isActionProcessing && progressBar.progress == 100) {
                        onActionTriggered?.invoke(isTop)
                        resetProcess(container, progressBar)
                        animateToZero(target)
                    }
                }
            })
            start()
        }
    }

    /**
     * Helper to locate the CircularProgressIndicator within the LinearLayout containers.
     */
    private fun findCircularProgress(viewGroup: ViewGroup): CircularProgressIndicator? {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is CircularProgressIndicator) return child
        }
        return null
    }

    /**
     * Aborts the active action process (e.g., when the user lets go prematurely).
     */
    private fun cancelActionProcess() {
        currentActionAnimator?.cancel()
        currentActionAnimator = null
        isActionProcessing = false

        val topContainer = getChildAt(0) as? ViewGroup
        val bottomContainer = getChildAt(1) as? ViewGroup

        listOfNotNull(topContainer, bottomContainer).forEach { container ->
            container.scaleX = 0f
            container.scaleY = 0f
            container.alpha = 0f
            findCircularProgress(container)?.progress = 0
        }
    }

    /**
     * Animates the indicator container away after a successful action trigger.
     */
    private fun resetProcess(container: ViewGroup, progressBar: CircularProgressIndicator) {
        container.animate()
            .scaleX(0f)
            .scaleY(0f)
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                progressBar.progress = 0
                isActionProcessing = false
                currentActionAnimator = null
            }
            .start()
    }

    /**
     * Smoothly returns the target view (RecyclerView) back to translationY = 0.
     */
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

    // Required override for NestedScrollingParent3 (empty as logic is in the typed version)
    override fun onNestedScroll(t: View, dxC: Int, dyC: Int, dxU: Int, dyU: Int, ty: Int) {}
}