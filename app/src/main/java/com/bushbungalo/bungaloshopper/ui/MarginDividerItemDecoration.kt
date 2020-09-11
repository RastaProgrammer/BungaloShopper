package com.bushbungalo.bungaloshopper.ui

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bushbungalo.bungaloshopper.R
import com.bushbungalo.bungaloshopper.utils.Utils
import kotlin.math.roundToInt


class MarginDividerItemDecoration(context: Context, orientation: Int, margin: Int)
    : RecyclerView.ItemDecoration()
{
    //region  Field Declarations
    private var mContext: Context = context
    private var mDivider: Drawable
    private var mOrientation = 0
    private var mMargin = 0

    companion object
    {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)

        const val HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL
        const val VERTICAL_LIST = LinearLayoutManager.VERTICAL
    }// end of companion object

    //endregion

    //region Field Initializations
    init
    {
        mMargin = margin
        val attr: TypedArray = context.obtainStyledAttributes(ATTRS)
        mDivider = attr.getDrawable(0)!!
        attr.recycle()
        setOrientation(orientation)
    }// end of init block

    private fun setOrientation(orientation: Int)
    {
        if(orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST)
        {
            throw  IllegalArgumentException("Invalid Orientation")
        }// end of if block

        mOrientation = orientation
    }// end of function setOrientation

    override fun onDrawOver(
        c: Canvas, parent: RecyclerView,
        state: RecyclerView.State
    )
    {
        if (mOrientation == VERTICAL_LIST)
        {
            drawVertical(c, parent)
        }// end of if block
        else
        {
            drawHorizontal(c, parent)
        }// end of else block
    }// end of function onDrawOver

    private fun drawVertical(c: Canvas, parent: RecyclerView)
    {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount

        for (i in 0 until childCount)
        {
            val child: View = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top: Int = child.bottom + params.bottomMargin
            val bottom = top + mDivider.intrinsicHeight

            mDivider.setBounds(left + dpToPx(mMargin), top, right, bottom)
            mDivider.setTint(parent.context.resources.getColor(R.color.main_color, null))

            if (parent.adapter?.getItemViewType(i) == Utils.ItemViewType.ITEM.ordinal && i < childCount - 1)
            {
                if(parent.adapter?.getItemViewType(i + 1) == Utils.ItemViewType.ITEM.ordinal)
                {
                    mDivider.draw(c)
                }// end of if block
            }// end of if block
        }// end of for range loop
    }// end of function drawVertical

    private fun drawHorizontal(c: Canvas, parent: RecyclerView)
    {
        val top = parent.paddingTop
        val bottom = parent.height - parent.paddingBottom
        val childCount = parent.childCount

        for (i in 0 until childCount)
        {
            val child: View = parent.getChildAt(i)
            val params = child
                .layoutParams as RecyclerView.LayoutParams
            val left: Int = child.right + params.rightMargin
            val right = left + mDivider.intrinsicHeight

            mDivider.setBounds(
                left, top + dpToPx(mMargin), right,
                bottom - dpToPx(mMargin)
            )
            mDivider.setTint(parent.context.resources.getColor(R.color.main_color, null))

            mDivider.draw(c)
        }// end of for range loop
    }// end of function drawHorizontal

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    )
    {
        if (mOrientation == VERTICAL_LIST)
        {
            outRect.set(0, 0, 0, 0)
        }// end of if block
        else
        {
            outRect.set(0, 0, 0, 0)
        }// end of else block
    }// end of function getItemOffsets

    /**
     * Converts from device pixel to screen pixels.
     * Function is a duplicate of [Utils.devicePixelsToPixels]
     *
     * @param dp    A value represent the number of device pixels
     * @return  An [Int] value representing the screen pixels
     */
    private fun dpToPx(dp: Int): Int
    {
        val r: Resources = mContext.resources

        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(), r.displayMetrics
        ).roundToInt()
    }// end of function dpToPx
}// end of class MarginDividerItemDecoration