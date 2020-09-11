package com.bushbungalo.bungaloshopper.database

import android.content.Context
import androidx.lifecycle.LiveData
import com.bushbungalo.bungaloshopper.model.ShoppingListItemEntity
import com.bushbungalo.bungaloshopper.utils.SampleShoppingList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppRepository()
{
    //region Field Declarations

    lateinit var mAllLists: LiveData<MutableList<ShoppingListItemEntity>>
    lateinit var mShoppingList: LiveData<MutableList<ShoppingListItemEntity>>

    companion object
    {
        private lateinit var mRoomDb: AppDatabase
        private lateinit var mInstance: AppRepository

        fun getInstance(context: Context) : AppRepository
        {
            mInstance = AppRepository(context)

            return mInstance
        }// end of function getInstance
    }// end of companion object

    //endregion

    //region Constructors

    private constructor(context: Context) : this()
    {
        mRoomDb = AppDatabase.getInstance(context)
    }// end of default constructor

    //endregion

    /**
     * Function which fills the database with sample data for testing
     */
    fun addSampleData()
    {
        CoroutineScope(Dispatchers.IO).launch {
            mRoomDb.productDao().insertAllShoppingLists(SampleShoppingList.getShippingLists())
        }
    } // end of function addSampleData

    /**
     * Removes all lists from the database
     */
    fun deleteAllShoppingLists()
    {
        CoroutineScope(Dispatchers.IO).launch {
            mRoomDb.productDao().deleteAll()
        }
    } // end of function deleteAllShoppingLists

    /**
     * Removes a specific shopping list from the database
     *
     * @param shoppingListItem The item that should be removed
     */
    fun deleteShoppingListItem(shoppingListItem: ShoppingListItemEntity)
    {
        CoroutineScope(Dispatchers.IO).launch {
            mRoomDb.productDao().deleteShoppingListItem(shoppingListItem)
        }
    }// end of function deleteShoppingListItem

    /**
     * Removes a group of items that belong to the same shopping list
     * from the database
     *
     * @param listDate  The date of the shopping list
     */
    fun deleteShoppingList(listDate: Long)
    {
        CoroutineScope(Dispatchers.IO).launch {
            mRoomDb.productDao().deleteShoppingList(listDate)
        }
    }// end of function deleteShoppingList

    /**
     * Returns all lists stored in the database
     */
    fun getAllShoppingLists(): LiveData<MutableList<ShoppingListItemEntity>>
    {
        return mRoomDb.productDao().getAllShoppingLists()
    } // end of function getAllShoppingLists

    /**
     * Initializes the object that will contains everything in the database table
     */
    fun initializeAllShoppingList()
    {
        mAllLists =  getAllShoppingLists()
    }// end of function initializeShoppingLists

    /**
     * Initializes a shopping list for a specific date
     *
     * @param date All items which share the same shopping date
     */
    fun initializeShoppingList(date: Long)
    {
        mShoppingList = getListByDate(date)
    }// end of function initializeShoppingLists

    /**
     * Retrieves a list of items from the database which share the same date
     *
     * @param listDate The specific date to be found
     */
    fun getListByDate(listDate: Long) : LiveData<MutableList<ShoppingListItemEntity>>
    {
        return mRoomDb.productDao().getListByDate(listDate)
    }// end of function getListByDate

    /**
     * Inserts a list of objects into the database table
     *
     * @param shoppingListItems All list containing the [ShoppingListItemEntity] to be
     * inserted
     */
    fun insertAllShoppingLists(shoppingListItems: MutableList<ShoppingListItemEntity>)
    {
        CoroutineScope(Dispatchers.IO).launch {
            mRoomDb.productDao().insertAllShoppingLists(shoppingListItems)
        }
    }// end of function insertAllShoppingLists

    /**
     * Inserts a specific into [ShoppingListItemEntity] into the database table
     *
     * @param shoppingListItem The [ShoppingListItemEntity] to be inserted
     */
    fun insertShoppingListItem(shoppingListItem: ShoppingListItemEntity)
    {
        CoroutineScope(Dispatchers.IO).launch {
            mRoomDb.productDao().insertShoppingListItem(shoppingListItem)
        }
    } // end of function insertShoppingListItem
}// end of class AppRepository