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

    private constructor(context: Context) : this()
    {
        mRoomDb = AppDatabase.getInstance(context)
    }// end of default constructor

    fun addSampleData()
    {
        CoroutineScope(Dispatchers.IO).launch {
            mRoomDb.productDao().insertAllShoppingLists(SampleShoppingList.getShippingLists())
        }
    } // end of method addSampleData

    fun deleteAllShoppingLists()
    {
        CoroutineScope(Dispatchers.IO).launch {
            mRoomDb.productDao().deleteAll()
        }
    } // end of function deleteAllShoppingLists

    fun deleteShoppingListItem(shoppingListItem: ShoppingListItemEntity)
    {
        CoroutineScope(Dispatchers.IO).launch {
            mRoomDb.productDao().deleteShoppingListItem(shoppingListItem)
        }
    }// end of function deleteShoppingListItem

    fun deleteShoppingList(listDate: Long)
    {
        CoroutineScope(Dispatchers.IO).launch {
            mRoomDb.productDao().deleteShoppingList(listDate)
        }
    }// end of function deleteShoppingList

    fun getAllShoppingLists(): LiveData<MutableList<ShoppingListItemEntity>>
    {
        return mRoomDb.productDao().getAllShoppingLists()
    } // end of function getAllShoppingLists

    fun initializeAllShoppingList()
    {
        mAllLists =  getAllShoppingLists()
    }// end of function initializeShoppingLists

    fun initializeShoppingList(date: Long)
    {
        mShoppingList = getListByDate(date)
    }// end of function initializeShoppingLists

    fun getListByDate(listDate: Long) : LiveData<MutableList<ShoppingListItemEntity>>
    {
        return mRoomDb.productDao().getListByDate(listDate)
    }// end of function getListByDate

    fun insertAllShoppingLists(shoppingListItems: MutableList<ShoppingListItemEntity>)
    {
        CoroutineScope(Dispatchers.IO).launch {
            mRoomDb.productDao().insertAllShoppingLists(shoppingListItems)
        }
    }// end of function insertAllShoppingLists

    fun insertShoppingListItem(shoppingListItem: ShoppingListItemEntity)
    {
        CoroutineScope(Dispatchers.IO).launch {
            mRoomDb.productDao().insertShoppingListItem(shoppingListItem)
        }
    } // end of function insertShoppingListItem
}// end of class AppRepository