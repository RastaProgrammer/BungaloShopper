package com.bushbungalo.bungaloshopper.view

import android.app.Application
import android.content.Context
import android.graphics.Typeface
import com.bushbungalo.bungaloshopper.viewmodel.ShoppingListViewModel
import com.bushbungalo.bungaloshopper.viewmodel.TotalListViewModel

class BungaloShopperApp : Application()
{
    companion object{
        lateinit var shopperContext: Context
        lateinit var currentFontNormal: Typeface
        lateinit var currentFontBold: Typeface
        lateinit var mTotalListViewModel: TotalListViewModel
        lateinit var mShoppingListViewModel: ShoppingListViewModel
    }

    override fun onCreate()
    {
        super.onCreate()
        shopperContext = this
        currentFontNormal = Typeface.createFromAsset(assets, "fonts/product_sans.ttf")
        currentFontBold = Typeface.createFromAsset(assets, "fonts/product_sans_bold.ttf")
    }// end of function onCreate
}// end of classBungaloShopperApp