package com.bushbungalo.bungaloshopper.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.bushbungalo.bungaloshopper.database.AppRepository
import com.bushbungalo.bungaloshopper.model.ShoppingListItemEntity
import com.bushbungalo.bungaloshopper.view.BungaloShopperApp

@Suppress("UNUSED")
class ShoppingListViewModel : ViewModel()
{
    private var mRepository: AppRepository = AppRepository.getInstance(BungaloShopperApp.shopperContext)
    lateinit var mShoppingList: LiveData<MutableList<ShoppingListItemEntity>>

    fun addSampleData()
    {
        mRepository.addSampleData()
    }

    fun deleteAllShoppingLists() {
        mRepository.deleteAllShoppingLists()
    }
    /**
     * The product to be added to the shopping list
     *
     * @param shoppingListItem The task to be assigned to the user
     */
    fun addProductToShoppingList(shoppingListItem: ShoppingListItemEntity)
    {
        mRepository.insertShoppingListItem(shoppingListItem)
    }// end of function addProductToShoppingList

    /**
     * Initialize all important member variables
     */
    fun initializeProducts(date: Long)
    {
        mRepository.initializeShoppingList(date)
        mShoppingList = mRepository.mShoppingList
    }// end of function initializeMembers

    fun getAllShoppingList() {
        mRepository.getAllShoppingLists()
    }

    fun addItem(shoppingItem: ShoppingListItemEntity)
    {
        mRepository.insertShoppingListItem(shoppingItem)
    }

    fun getListByDate(listDate: Long)
    {
        mRepository.getListByDate(listDate)
    }

    fun insertAllShoppingLists(shoppingListItems: MutableList<ShoppingListItemEntity>) {
        mRepository.insertAllShoppingLists(shoppingListItems)
    }

    fun insertShoppingListItem(shoppingListItem: ShoppingListItemEntity)
    {
        mRepository.insertShoppingListItem(shoppingListItem)
    }

    fun deleteShoppingListItem(shoppingListItem: ShoppingListItemEntity)
    {
        mRepository.deleteShoppingListItem(shoppingListItem)
    }

    fun deleteShoppingList(date: Long)
    {
        mRepository.deleteShoppingList(date)
    }
}// end of ShoppingListViewModel