package com.saboon.project_2511sch.util

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SwipeRevealLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var downX = 0f
    private var downY = 0f
    private var translationXOnDown = 0f
    private var maxSwipe = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var isHorizontalSwipe = false

    var onOpened: (() -> Unit)? = null

    init {
        clipChildren = false
        clipToPadding = false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {

        when (ev.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                isHorizontalSwipe = false
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = ev.x - downX
                val dy = ev.y - downY

                if (abs(dx) > touchSlop && abs(dx) > abs(dy)) {
                    // Horizontal swipe başladı
                    isHorizontalSwipe = true
                    parent.requestDisallowInterceptTouchEvent(true)
                    return true
                }
            }
        }

        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (maxSwipe == 0f) return true

        val foreground = getChildAt(1)

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                translationXOnDown = foreground.translationX
                return true
            }

            MotionEvent.ACTION_MOVE -> {

                val dx = event.x - downX
                val dy = event.y - downY

                if (!isHorizontalSwipe) {
                    if (abs(dx) > touchSlop && abs(dx) > abs(dy)) {
                        isHorizontalSwipe = true
                        parent.requestDisallowInterceptTouchEvent(true)
                    } else {
                        return false
                    }
                }

                val newTranslation = translationXOnDown + dx
                foreground.translationX =
                    min(0f, max(-maxSwipe, newTranslation))
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {

                val currentX = foreground.translationX

                if (currentX <= -maxSwipe / 2) {
                    foreground.animate().translationX(-maxSwipe).setDuration(200).start()
                    onOpened?.invoke()
                } else {
                    foreground.animate().translationX(0f).setDuration(200).start()
                }

                isHorizontalSwipe = false
            }
        }

        return true
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)

        if (childCount >= 2) {
            val background = getChildAt(0)
            maxSwipe = background.measuredWidth.toFloat()
        }

        getChildAt(1)?.translationX = 0f
    }

    fun close() {
        getChildAt(1)?.animate()?.translationX(0f)?.setDuration(200)?.start()
    }
}