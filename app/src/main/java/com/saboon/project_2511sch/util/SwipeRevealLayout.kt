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
    var isSwipeable: Boolean = true

    init {
        clipChildren = false
        clipToPadding = false
    }

//    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
//
//        when (ev.actionMasked) {
//
//            MotionEvent.ACTION_DOWN -> {
//                downX = ev.x
//                downY = ev.y
//                isHorizontalSwipe = false
//            }
//
//            MotionEvent.ACTION_MOVE -> {
//                val dx = ev.x - downX
//                val dy = ev.y - downY
//
//                if (abs(dx) > touchSlop && abs(dx) > abs(dy)) {
//                    // Horizontal swipe başladı
//                    isHorizontalSwipe = true
//                    parent.requestDisallowInterceptTouchEvent(true)
//                    return true
//                }
//            }
//        }
//
//        return super.onInterceptTouchEvent(ev)
//    }

//    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
//
//        if (!isSwipeable) return false
//
//        when (ev.actionMasked) {
//            MotionEvent.ACTION_DOWN -> {
//                downX = ev.x
//                downY = ev.y
//                isHorizontalSwipe = false
//                // Dokunma anındaki mevcut pozisyonu kaydet
//                val foreground = getChildAt(1)
//                translationXOnDown = foreground?.translationX ?: 0f
//            }
//
//            MotionEvent.ACTION_MOVE -> {
//                val dx = ev.x - downX
//                val dy = ev.y - downY
//
//                // Eğer parmak yatayda dikeyden daha fazla hareket etmişse
//                // ve bu hareket touchSlop'tan büyükse olaya EL KOY (Intercept)
//                if (abs(dx) > touchSlop && abs(dx) > abs(dy)) {
//                    isHorizontalSwipe = true
//                    // Parent'a (RecyclerView) "sen karışma ben kaydırıyorum" de
//                    parent.requestDisallowInterceptTouchEvent(true)
//                    return true // True dönerek dokunma olayını alt View'lardan (mcvForeground) çal!
//                }
//            }
//        }
//        return super.onInterceptTouchEvent(ev)
//    }


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isSwipeable) return false

        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                isHorizontalSwipe = false
                // Mevcut pozisyonu al
                translationXOnDown = getChildAt(1)?.translationX ?: 0f
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = ev.x - downX
                val dy = ev.y - downY

                // KRİTİK DÜZELTME:
                // 1. Yatay hareket touchSlop'tan büyük mü?
                // 2. Yatay hareket, dikey hareketten daha mı baskın? (abs(dx) > abs(dy))
                if (abs(dx) > touchSlop && abs(dx) > abs(dy)) {
                    isHorizontalSwipe = true
                    // Sadece yatay kaydırma kesinleşirse RecyclerView'ı durdur
                    parent.requestDisallowInterceptTouchEvent(true)
                    return true
                }

                // Eğer dikey hareket daha fazlaysa (abs(dy) > abs(dx)),
                // ASLA true dönme, RecyclerView akmaya devam etsin.
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//
//        if (!isSwipeable || maxSwipe == 0f) return false
//
//        val foreground = getChildAt(1) ?: return true
//
//        when (event.actionMasked) {
//            MotionEvent.ACTION_DOWN -> {
//                downX = event.x
//                translationXOnDown = foreground.translationX
//                return true
//            }
//
//            MotionEvent.ACTION_MOVE -> {
//                val dx = event.x - downX
//                val dy = event.y - downY
//
//                if (!isHorizontalSwipe) {
//                    if (abs(dx) > touchSlop && abs(dx) > abs(dy)) {
//                        isHorizontalSwipe = true
//                        parent.requestDisallowInterceptTouchEvent(true)
//                    } else {
//                        return false
//                    }
//                }
//
//                val newTranslation = translationXOnDown + dx
//                foreground.translationX =
//                    min(0f, max(-maxSwipe, newTranslation))
//            }
//
//            MotionEvent.ACTION_UP,
//            MotionEvent.ACTION_CANCEL -> {
//
//                val currentX = foreground.translationX
//
//                if (currentX <= -maxSwipe / 2) {
//                    foreground.animate().translationX(-maxSwipe).setDuration(200).start()
//                    onOpened?.invoke()
//                } else {
//                    foreground.animate().translationX(0f).setDuration(200).start()
//                }
//
//                isHorizontalSwipe = false
//            }
//        }
//
//        return true
//    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isSwipeable || maxSwipe == 0f) return false

        val foreground = getChildAt(1) ?: return true

        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - downX
                val dy = event.y - downY

                // Hareketin yönünden emin değilsek kontrol et
                if (!isHorizontalSwipe) {
                    if (abs(dx) > touchSlop && abs(dx) > abs(dy)) {
                        isHorizontalSwipe = true
                        parent.requestDisallowInterceptTouchEvent(true)
                    } else if (abs(dy) > touchSlop) {
                        // Dikey kaydırma baskınsa, bu event'i tamamen bırak
                        return false
                    }
                }

                if (isHorizontalSwipe) {
                    val newTranslation = translationXOnDown + dx
                    foreground.translationX = min(0f, max(-maxSwipe, newTranslation))
                    return true // Olayı tükettik
                }
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
        return super.onTouchEvent(event)
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