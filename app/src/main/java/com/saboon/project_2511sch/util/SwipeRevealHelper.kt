package com.saboon.project_2511sch.util

import android.content.Context
import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.saboon.project_2511sch.R

class SwipeRevealHelper(
    private val context: Context,
    private val onSwipe: (position: Int) -> Unit
): ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val bottomLayoutWidth = 700f // İki alttaki layoutun toplam genişliği (70+80+70)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Burayı boş bırakıyoruz çünkü öğeyi listeden silmiyoruz!
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val foregroundView = itemView.findViewById<View>(R.id.mcv_foreground)

        // KAYDIRMA SINIRI: dX değerini (kaydırma miktarı) buton genişliğiyle sınırlıyoruz
        val translationX = dX.coerceAtLeast(-bottomLayoutWidth)

        foregroundView.translationX = translationX
    }

}