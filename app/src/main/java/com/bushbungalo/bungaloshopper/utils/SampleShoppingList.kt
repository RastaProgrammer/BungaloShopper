package com.bushbungalo.bungaloshopper.utils

import com.bushbungalo.bungaloshopper.model.ShoppingListItemEntity
import java.util.*

// Sample shopping list for testing
object SampleShoppingList
{
    private lateinit var mShoppingList1: ShoppingListItemEntity
    private lateinit var mShoppingList2: ShoppingListItemEntity
    private lateinit var mShoppingList3: ShoppingListItemEntity
    private lateinit var mShoppingList4: ShoppingListItemEntity
    private lateinit var mShoppingList5: ShoppingListItemEntity
    private lateinit var mShoppingList6: ShoppingListItemEntity

    fun getShippingLists(): MutableList<ShoppingListItemEntity>
    {
        val lists =  mutableListOf<ShoppingListItemEntity>()

        mShoppingList1 = ShoppingListItemEntity(
            0,
            "bs1234",
            "Milk",
            "Dairy",
            1,
            1.74,
            getDate(0).time
        )

        mShoppingList2 = ShoppingListItemEntity(
            0,
            "bs5678",
            "Bread",
            "Baked Goods",
            1,
            0.98,
            getDate(-1).time
        )

        mShoppingList3 = ShoppingListItemEntity(
            0,
            "bs9876",
            "Chicken",
            "Meats",
            1,
            3.98,
            getDate(-2).time
        )

        mShoppingList4 = ShoppingListItemEntity(
            0,
            "bs3098",
            "Ice Cream",
            "Dairy",
            1,
            1.98,
            getDate(-2).time
        )

        mShoppingList5 = ShoppingListItemEntity(
            0,
            "bs8067",
            "Hamburger Buns",
            "Baked Goods",
            1,
            0.98,
            getDate(-2).time
        )

        mShoppingList6 = ShoppingListItemEntity(
            0,
            "bs6698",
            "Beef",
            "Meats",
            1,
            4.98,
            getDate(-2).time
        )

        lists.add(mShoppingList1)
        lists.add(mShoppingList2)
        lists.add(mShoppingList3)
        lists.add(mShoppingList4)
        lists.add(mShoppingList5)
        lists.add(mShoppingList6)

        return lists
    } // end of function getShippingLists

    private fun getDate(diff: Int): Date
    {
        val cal = GregorianCalendar()
        cal.add(Calendar.MILLISECOND, diff)
        return cal.time
    } // end of function getDate
}// end of class SampleShoppingList