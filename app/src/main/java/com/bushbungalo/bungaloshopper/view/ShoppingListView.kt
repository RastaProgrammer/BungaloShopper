package com.bushbungalo.bungaloshopper.view

import com.bushbungalo.bungaloshopper.model.ShoppingListItemEntity

interface ShoppingListView
{
    fun insertAllShoppingLists(shoppingListItems: MutableList<ShoppingListItemEntity>)

    fun insertShoppingListItem(shoppingListItem: ShoppingListItemEntity)

    fun deleteAllShoppingLists()

    fun deleteShoppingList(date: Long)

    fun deleteShoppingListItem(shoppingListItem: ShoppingListItemEntity)

    fun exitApp()

    fun getAllShoppingLists()

    fun getListByDate(listDate: Long)
}// end of interface ShoppingListView