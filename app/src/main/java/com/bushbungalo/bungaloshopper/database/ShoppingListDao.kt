package com.bushbungalo.bungaloshopper.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.bushbungalo.bungaloshopper.model.ShoppingListItemEntity

@Dao
interface ShoppingListDao
{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllShoppingLists(shoppingListItems: MutableList<ShoppingListItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertShoppingListItem(shoppingListItem: ShoppingListItemEntity)

    @Delete
    fun deleteShoppingListItem(shoppingListItem: ShoppingListItemEntity)

    @Query("DELETE FROM shopping_list WHERE shopping_date = :listDate")
    fun deleteShoppingList(listDate: Long)

    @Query("DELETE FROM shopping_list")
    fun deleteAll(): Int

    // Ensure that the list is ordered by shopping_date
    @Query("SELECT * FROM shopping_list ORDER BY shopping_date DESC")
    fun getAllShoppingLists(): LiveData<MutableList<ShoppingListItemEntity>>

    // Ensure that the list is ordered by product_category
    @Query("SELECT * FROM shopping_list WHERE shopping_date = :listDate ORDER BY product_category ASC")
    fun getListByDate(listDate: Long): LiveData<MutableList<ShoppingListItemEntity>>

}// end of interface ShoppingListDao
