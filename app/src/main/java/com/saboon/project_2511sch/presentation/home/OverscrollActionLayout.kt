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

class OverscrollActionLayout(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(context, attrs), NestedScrollingParent3 {

    private var totalDragY = 0f
    private val friction = 0.5f
    private val threshold = 400f
    private val stickySlop = 80f
    private val actionDuration = 2000L

    private var isAnimating = false
    private var isActionProcessing = false
    private var currentActionAnimator: ValueAnimator? = null

    private var canTriggerTopOverscroll = false
    private var canTriggerBottomOverscroll = false

    var onActionTriggered: ((isTop: Boolean) -> Unit)? = null

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        if ((axes and ViewCompat.SCROLL_AXIS_VERTICAL) == 0 || isAnimating || isActionProcessing) {
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

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (totalDragY == 0f) return

        if (totalDragY > 0 && dy > 0) {
            val consume = dy.coerceAtMost(totalDragY.toInt())
            totalDragY -= consume
            if (totalDragY < stickySlop && totalDragY > 0 && !isActionProcessing) totalDragY = 0f
            applyTranslation(target)
            consumed[1] = consume
            updateIndicators(target)

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
            totalDragY -= dyUnconsumed * friction
            totalDragY = totalDragY.coerceIn(-threshold, threshold)

            applyTranslation(target)
            updateIndicators(target)
            consumed[1] = dyUnconsumed

            if (Math.abs(totalDragY) >= threshold && !isActionProcessing) {
                startActionProcess(totalDragY > 0, target)
            }
        }
    }

    private fun applyTranslation(target: View) {
        if (Math.abs(totalDragY) < stickySlop && !isActionProcessing) {
            target.translationY = 0f
        } else {
            val sign = if (totalDragY > 0) 1 else -1
            target.translationY = totalDragY - (sign * stickySlop)
        }
    }

    private fun updateIndicators(target: View) {
        // Now referencing the LinearLayout containers
        val topContainer = getChildAt(0) ?: return
        val bottomContainer = getChildAt(1) ?: return

        val currentTranslation = target.translationY
        val maxVisibleTranslation = threshold - stickySlop
        val progress = if (maxVisibleTranslation > 0) {
            (Math.abs(currentTranslation) / maxVisibleTranslation).coerceIn(0f, 1f)
        } else 0f

        if (totalDragY > 0) {
            topContainer.alpha = if (isActionProcessing) 1f else progress
            bottomContainer.alpha = 0f
        } else if (totalDragY < 0) {
            bottomContainer.alpha = if (isActionProcessing) 1f else progress
            topContainer.alpha = 0f
        } else {
            topContainer.alpha = 0f
            bottomContainer.alpha = 0f
        }
    }

    private fun startActionProcess(isTop: Boolean, target: View) {
        isActionProcessing = true
        val container = (if (isTop) getChildAt(0) else getChildAt(1)) as? ViewGroup ?: return

        // Find the CircularProgressIndicator within the LinearLayout
        val progressBar = findCircularProgress(container) ?: return

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

    private fun findCircularProgress(viewGroup: ViewGroup): CircularProgressIndicator? {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is CircularProgressIndicator) return child
        }
        return null
    }

    private fun cancelActionProcess() {
        currentActionAnimator?.cancel()
        currentActionAnimator = null
        isActionProcessing = false

        val topContainer = getChildAt(0) as? ViewGroup
        val bottomContainer = getChildAt(1) as? ViewGroup

        topContainer?.let {
            it.alpha = 0f
            findCircularProgress(it)?.progress = 0
        }
        bottomContainer?.let {
            it.alpha = 0f
            findCircularProgress(it)?.progress = 0
        }
    }

    private fun resetProcess(container: ViewGroup, progressBar: CircularProgressIndicator) {
        container.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                progressBar.progress = 0
                isActionProcessing = false
                currentActionAnimator = null
            }
            .start()
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