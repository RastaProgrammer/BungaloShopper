package com.bushbungalo.bungaloshopper.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bushbungalo.bungaloshopper.R
import com.bushbungalo.bungaloshopper.view.BungaloShopperApp

class CategoriesAdapter(context: Context, categories: MutableList<String>) :
    ArrayAdapter<String>(context, 0, categories)
{
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        return initView(position, convertView, parent)
    }// end of function getView

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        return initView(position, convertView, parent)
    }// end of function getDropDownView

    private fun getCategoryIcon(category: String) : Int
    {
        return when(category)
        {
            "Baked Goods" -> {
               R.drawable.sl_loaf_of_bread
            }
            "Canned Goods" -> {
               R.drawable.sl_canned_goods
            }
            "Clothing" -> {
                R.drawable.sl_clothing
            }
            "Dairy" -> {
                R.drawable.sl_dairy
            }
            "Electronics" -> {
                R.drawable.sl_electronics
            }
            "Health and Personal Care" -> {
                R.drawable.sl_health
            }
            "Meat" -> {
                R.drawable.sl_meat
            }
            "Meats" -> {
                R.drawable.sl_meat
            }
            "Produce" -> {
                R.drawable.sl_produce
            }
            "Toiletries" -> {
                R.drawable.sl_toiletries
            }
            else -> {
                R.drawable.sl_general
            }// end of else block
        }// end of else block
    }// end of function getCategoryIcon

    private fun initView(position: Int, convertView: View?, parent: ViewGroup) : View
    {
        var mConvertView = convertView

        if(mConvertView == null)
        {
            mConvertView = LayoutInflater.from(context).inflate(
                R.layout.spinner_item_layout,
                parent, false
            )
        }// end of if block

        val imvCategory: ImageView = mConvertView?.findViewById(R.id.item_category_imv)!!
        val txvSpinnerText: TextView = mConvertView.findViewById(R.id.spinner_text_txv)!!
        txvSpinnerText.text = getItem(position)
        txvSpinnerText.typeface = BungaloShopperApp.currentFontNormal
        imvCategory.setImageResource(getCategoryIcon(txvSpinnerText.text.toString()))

        return mConvertView
    }// end of function initView
} // end of class CategoriesAdapter
