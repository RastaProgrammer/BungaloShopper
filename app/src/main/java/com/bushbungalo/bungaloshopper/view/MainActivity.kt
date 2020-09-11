package com.bushbungalo.bungaloshopper.view

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bushbungalo.bungaloshopper.R
import com.bushbungalo.bungaloshopper.model.ShoppingListItemEntity
import com.bushbungalo.bungaloshopper.view.BungaloShopperApp.Companion.mShoppingListViewModel
import com.bushbungalo.bungaloshopper.view.BungaloShopperApp.Companion.mTotalListViewModel
import com.bushbungalo.bungaloshopper.viewmodel.ShoppingListViewModel
import com.bushbungalo.bungaloshopper.viewmodel.TotalListViewModel

class MainActivity : AppCompatActivity(), ShoppingListView
{
    //region Declarations
    private val mainFragment = MainFragment.newInstance()
    private val shoppingListFragment = ShoppingListFragment.newInstance()
    var mSelectedShoppingList = 0L
    //endregion

    //region AppCompatActivity Interface Implementations
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        mTotalListViewModel = ViewModelProvider(this)
            .get(TotalListViewModel::class.java)

        mShoppingListViewModel = ViewModelProvider(this)
            .get(ShoppingListViewModel::class.java)

        loadMainFragment()

        //mShoppingListViewModel.deleteAllShoppingLists()
        //mShoppingListViewModel.addSampleData()
    }// end of function onCreate

    //endregion

    //region Fragment Loading
    /**
     * Load the main fragment
     */
    private fun loadMainFragment()
    {
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.animator.enter_from_right,
                R.animator.exit_to_left,
                R.animator.enter_from_left,
                R.animator.exit_to_right
            )
            .replace(R.id.app_main_container_fl, mainFragment,
                "main").commit()
    }// end of function loadShoppingListFragment

    /**
     * Load the fragment for a single shopping list
     */
    fun loadShoppingListFragment()
    {
        supportFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(
                R.animator.enter_from_right,
                R.animator.exit_to_left,
                R.animator.enter_from_left,
                R.animator.exit_to_right
            )
            .replace(R.id.app_main_container_fl, shoppingListFragment,
                "day list").commit()
    }// end of function loadShoppingListFragment

    //endregion

    //region ShoppingListView Interface Implementations
    override fun insertAllShoppingLists(shoppingListItems: MutableList<ShoppingListItemEntity>)
    {
        mShoppingListViewModel.insertAllShoppingLists(shoppingListItems)
    }// end of function insertAllShoppingLists

    override fun insertShoppingListItem(shoppingListItem: ShoppingListItemEntity)
    {
        mShoppingListViewModel.insertShoppingListItem(shoppingListItem)
    }// end of function insertShoppingListItem

    override fun deleteAllShoppingLists()
    {
        mShoppingListViewModel.deleteAllShoppingLists()
    }// end of function deleteAllShoppingLists

    override fun deleteShoppingList(date: Long)
    {
        mShoppingListViewModel.deleteShoppingList(date)
    }// end of function deleteShoppingList

    override fun deleteShoppingListItem(shoppingListItem: ShoppingListItemEntity)
    {
        mShoppingListViewModel.deleteShoppingListItem(shoppingListItem)
    }// end of function deleteShoppingList

    override fun getAllShoppingLists()
    {
        mShoppingListViewModel.getAllShoppingList()
    }// end of function getAllShoppingLists

    override fun getListByDate(listDate: Long)
    {
        mShoppingListViewModel.getListByDate(listDate)
    }// end of function getListByDate
    //endregion
}// end of class MainActivity