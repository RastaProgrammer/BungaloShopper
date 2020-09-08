package com.bushbungalo.bungaloshopper.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bushbungalo.bungaloshopper.database.AppRepository
import com.bushbungalo.bungaloshopper.model.ShoppingListItemEntity
import com.bushbungalo.bungaloshopper.view.BungaloShopperApp

class TotalListViewModel : ViewModel()
{
    private var mRepository: AppRepository = AppRepository.getInstance(BungaloShopperApp.shopperContext)
    lateinit var mAllLists: LiveData<MutableList<ShoppingListItemEntity>>

    /**
     * Initialize all important member variables
     */
    fun initializeAllShoppingLists()
    {
        mRepository.initializeAllShoppingList()
        mAllLists = mRepository.mAllLists
    }// end of function initializeMembers

    fun getAllShoppingList()
    {
        mRepository.getAllShoppingLists()
    }
}// end of ShoppingListViewModel