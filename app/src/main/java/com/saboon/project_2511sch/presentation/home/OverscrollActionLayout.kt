package com.saboon.project_2511sch.presentation.home

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.ViewCompat

class OverscrollActionLayout(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(context, attrs), NestedScrollingParent3 {

    private var totalDragY = 0f
    private val friction = 0.5f // Çekme direnci (Damping)
    private val threshold = 400f // Butonun tam görünmesi için gereken px
    private var isActionPending = false

    // Parent, Child kaydırmaya başlamadan önce "Ben bu scroll'u yakalamak istiyorum" der.
    override fun onStartNestedScroll(
        child: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0
    }

    override fun onNestedScrollAccepted(
        child: View,
        target: View,
        axes: Int,
        type: Int
    ) {

    }

    override fun onStopNestedScroll(target: View, type: Int) {
        if (isActionPending) {
            // Aksiyon tetiklendiğinde listeyi butonun arkasında bırakmak istersen
            // buradaki animasyonu çalıştırma veya butonun yüksekliği kadar bırak.
            // Şimdilik 0'a dönmesi ama butonun tıklanabilir olması için:
            resetWithDelay(target)
        } else {
            animateToZero(target)
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {

    }

    // Child daha scroll yapmadan önce Parent'a sorar: "Önce sen mi kaydıracaksın?"
    // Bu, RV kaymış durumdayken (totalDragY != 0) parmak ters yöne giderse RV'yi eski yerine çekmek için kritiktir.
    override fun onNestedPreScroll(
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        if (totalDragY > 0 && dy > 0) { // Yukarıdan aşağı çekilmişti, şimdi kullanıcı yukarı itiyor
            val consume = dy.coerceAtMost(totalDragY.toInt())
            totalDragY -= consume
            target.translationY = totalDragY
            updateButtonEffects()
            consumed[1] = consume
        } else if (totalDragY < 0 && dy < 0) { // Aşağıdan yukarı çekilmişti, şimdi kullanıcı aşağı itiyor
            val consume = dy.coerceAtLeast(totalDragY.toInt())
            totalDragY -= consume
            target.translationY = totalDragY
            updateButtonEffects()
            consumed[1] = consume
        }
    }
    // Child (RV) scroll işlemini bitirdi ve hala artan miktar (overscroll) varsa buraya düşer.
    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        if (dyUnconsumed != 0) {
            // Gelen overscroll miktarını dirençle çarpıp biriktiriyoruz
            totalDragY -= dyUnconsumed * friction

            // Limitleme
            totalDragY = totalDragY.coerceIn(-threshold, threshold)

            // RV'yi fiziksel olarak kaydır
            target.translationY = totalDragY

            // Butonların Alpha (şeffaflık) değerini güncelle
            updateButtonEffects()

            // Miktarı tükettiğimizi sisteme bildiriyoruz
            consumed[1] = dyUnconsumed

            // Eğer limit noktasına ulaşıldıysa aksiyonu hazırla
            isActionPending = Math.abs(totalDragY) >= threshold
        }
    }

    private fun updateButtonEffects() {
        val prevBtn = getChildAt(0)
        val nextBtn = getChildAt(1)

        if (totalDragY > 0) {
            prevBtn.alpha = (totalDragY / threshold).coerceIn(0f, 1f)
            nextBtn.alpha = 0f
        } else {
            nextBtn.alpha = (-totalDragY / threshold).coerceIn(0f, 1f)
            prevBtn.alpha = 0f
        }
    }

    private fun resetWithDelay(target: View) {
        // Kullanıcıya işlemin başladığını göstermek için
        // listeyi olduğu yerde kısa bir süre (örneğin 500ms) bekletiyoruz.
        target.postDelayed({
            animateToZero(target)
            isActionPending = false
        }, 3000)
    }
    private fun animateToZero(target: View) {
        ObjectAnimator.ofFloat(target, "translationY", 0f).apply {
            duration = 250
            interpolator = DecelerateInterpolator()
            start()
        }
        totalDragY = 0f
        updateButtonEffects()
    }

}