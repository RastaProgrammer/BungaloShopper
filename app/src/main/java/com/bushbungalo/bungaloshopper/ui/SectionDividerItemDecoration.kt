package com.bushbungalo.bungaloshopper.ui

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SectionDividerItemDecoration() : RecyclerView.ItemDecoration()
{
    private var divider: Drawable? = null

    companion object
    {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)
    }// end of companion object

    constructor(context: Context) : this()
    {
        val styledAttributes: TypedArray = context.obtainStyledAttributes(ATTRS)
        divider = styledAttributes.getDrawable(0)
        styledAttributes.recycle()
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State)
    {
        val left: Int = parent.paddingLeft
        val right: Int = parent.width - parent.paddingRight

        val childCount: Int = parent.childCount

        for (i in 0 until childCount)
        {
            val row: View = parent.getChildAt(i)

            if (row.tag == "last")
            {
                drawDivider(canvas, row, left, right)
            }
        }
    }

    private fun drawDivider(canvas: Canvas, row: View, left: Int, right: Int)
    {
        val params = row.layoutParams as RecyclerView.LayoutParams
        val top = row.bottom + params.bottomMargin
        val bottom: Int = top + divider?.intrinsicHeight!!

        divider?.setBounds(left, top, right, bottom)
        divider?.draw(canvas)
    }

}// end of class SectionDividerItemDecoration