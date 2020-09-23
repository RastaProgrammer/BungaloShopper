package com.bushbungalo.bungaloshopper.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bushbungalo.bungaloshopper.R
import com.bushbungalo.bungaloshopper.model.ShoppingListItemEntity
import com.bushbungalo.bungaloshopper.utils.DEFAULT_ITEM_QUANTITY
import com.bushbungalo.bungaloshopper.utils.DEFAULT_ITEM_UNIT_PRICE
import com.bushbungalo.bungaloshopper.utils.Utils
import com.bushbungalo.bungaloshopper.utils.Utils.ItemViewType
import com.bushbungalo.bungaloshopper.utils.Utils.loadCustomFont
import com.bushbungalo.bungaloshopper.view.BungaloShopperApp
import com.bushbungalo.bungaloshopper.view.ShoppingListFragment
import kotlinx.android.synthetic.main.day_shopping_list_fragment.*
import java.util.*

class ShoppingListAdapter(
    private var shoppingItems: MutableList<ShoppingListItemEntity>,
    private var fragment: ShoppingListFragment)
    : RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder>()
{
    //region Field Declarations
    private lateinit var mLayout: View

    private lateinit var imvProductCategoryIcon: ImageView
    private lateinit var txvProductCategoryName: TextView
    private lateinit var txvProductItem: TextView
    private lateinit var imvEditIcon: ImageView
    private lateinit var imvRemoveIcon: ImageView
    private lateinit var mCategoryHeading: ConstraintLayout

    private var mCurrentCategory: String = ""
    private var categoryChange = false

    private val shoppingListCopy = mutableListOf<ShoppingListItemEntity>()

    //endregion

    //region Construction

    init
    {
        shoppingListCopy.addAll(shoppingItems)
    }// end of init block

    //endregion

    //region ViewHolder

    inner class ShoppingListViewHolder(taskItem: View): RecyclerView.ViewHolder(taskItem)
    {
        fun bindShoppingList(shoppingItem: ShoppingListItemEntity)
        {
            if(shoppingItem.productName == Utils.HEADER_ITEM)
            {
                when(shoppingItem.productCategory)
                {
                    "Baked Goods" -> {
                        imvProductCategoryIcon.setImageResource(R.drawable.sl_loaf_of_bread)
                    }
                    "Canned Goods" -> {
                        imvProductCategoryIcon.setImageResource(R.drawable.sl_canned_goods)
                    }
                    "Clothing" -> {
                        imvProductCategoryIcon.setImageResource(R.drawable.sl_clothing)
                    }
                    "Dairy" -> {
                        imvProductCategoryIcon.setImageResource(R.drawable.sl_dairy)
                    }
                    "Electronics" -> {
                        imvProductCategoryIcon.setImageResource(R.drawable.sl_electronics)
                    }
                    "Health and Personal Care" -> {
                        imvProductCategoryIcon.setImageResource(R.drawable.sl_health)
                    }
                    "Meat" -> {
                        imvProductCategoryIcon.setImageResource(R.drawable.sl_meat)
                    }
                    "Meats" -> {
                        imvProductCategoryIcon.setImageResource(R.drawable.sl_meat)
                    }
                    "Produce" -> {
                        imvProductCategoryIcon.setImageResource(R.drawable.sl_produce)
                    }
                    "Toiletries" -> {
                        imvProductCategoryIcon.setImageResource(R.drawable.sl_toiletries)
                    }
                    else -> {
                        imvProductCategoryIcon.setImageResource(R.drawable.sl_general)
                    }// end of else block
                }// end of else block

                imvProductCategoryIcon.setColorFilter(
                    ContextCompat.getColor(
                        BungaloShopperApp.shopperContext,
                        R.color.milk_white
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                txvProductCategoryName.text = Utils.toProperCase(shoppingItem.productCategory)
            }// end of if block
            else
            {
                txvProductItem.text = shoppingItem.productName
            }// end of else block
        }// end of function bindShoppingList
    }// end of inner class ShoppingListViewHolder

    //endregion

    //region RecyclerView.Adapter Interface Implementations

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListViewHolder
    {
        categoryChange =
            mCurrentCategory.isNotEmpty() && mCurrentCategory != shoppingItems[viewType].productCategory

        val shoppingListView: View = when(viewType)
        {
            ItemViewType.HEADER.ordinal -> {
                LayoutInflater.from(parent.context).inflate(
                    R.layout.day_shopping_list_category_header_item, parent, false
                )
            }

            ItemViewType.HEADER.ordinal -> {
                LayoutInflater.from(parent.context).inflate(
                    R.layout.day_shopping_list_item, parent, false
                )
            }
            else -> {

                LayoutInflater.from(parent.context).inflate(
                    R.layout.day_shopping_list_item, parent, false
                )
            }
        }// end of when block

        val rootView: ViewGroup =
            shoppingListView.findViewById(R.id.shopping_list_item_layout)

        loadCustomFont(rootView)

        mLayout = shoppingListView

        return ShoppingListViewHolder(shoppingListView)
    }// end of function onCreateViewHolder

    override fun getItemCount(): Int = shoppingItems.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int
    {
        return if(shoppingItems[position].productName == Utils.HEADER_ITEM)
            ItemViewType.HEADER.ordinal else ItemViewType.ITEM.ordinal
    }// end of function getItemViewType

    override fun onBindViewHolder(holder: ShoppingListViewHolder, position: Int)
    {
        if(shoppingItems[position].productName == Utils.HEADER_ITEM)
        {
            mCategoryHeading = holder.itemView.findViewById(R.id.product_category_layout)
            imvProductCategoryIcon = holder.itemView.findViewById(R.id.category_icon_imv)
            txvProductCategoryName = holder.itemView.findViewById(R.id.category_name_txv)
        }// end of if block
        else
        {
            txvProductItem = holder.itemView.findViewById(R.id.item_name_txv)
            imvEditIcon = holder.itemView.findViewById(R.id.edit_icon_imv)
            imvRemoveIcon = holder.itemView.findViewById(R.id.remove_icon_imv)

            imvEditIcon.setOnClickListener {
                fragment.hideBillingSnackBar()
                updateItem(shoppingItems[position])
            }

            mLayout.setOnClickListener {
                fragment.hideBillingSnackBar()
                updateItem(shoppingItems[position])
            }

            imvRemoveIcon.setOnClickListener {
                fragment.removeItemPrompt(holder.itemView.context, shoppingItems[position])
            }
        }// end of else block

        holder.bindShoppingList(shoppingItems[position])

        if(position == itemCount - 1 && !fragment.mFiltering )
        {
            fragment.calculateBillTotal()
        }// end of if block
    }// end of function onBindViewHolder

    //endregion

    //region Adapter specific functions

    /**
     * Filters the [RecyclerView] using a [String]
     *
     * @param filterBy The [String] that the list should be filtered by
     */
    fun filterItems(filterBy: String)
    {
        shoppingItems.clear()

        if (filterBy.isEmpty())
        {
            shoppingItems.addAll(shoppingListCopy)
            fragment.no_results_txv.visibility = View.INVISIBLE
        }// end of if block
        else
        {
            val previousCategories = mutableListOf<String>()

            for (item in shoppingListCopy)
            {
                if (item.productName.toLowerCase(Locale.ROOT).contains(filterBy))
                {
                    // Insert a header for this item if the header is not already present
                    if(item.productName != Utils.HEADER_ITEM)
                    {
                        if(!previousCategories.contains(item.productCategory))
                        {
                            shoppingItems.add(
                                ShoppingListItemEntity(
                                    id = 0,
                                    shoppingListId = "",
                                    productName = Utils.HEADER_ITEM,
                                    productCategory = item.productCategory,
                                    productQuantity = DEFAULT_ITEM_QUANTITY,
                                    unitPrice = DEFAULT_ITEM_UNIT_PRICE,
                                    shoppingDate = item.shoppingDate
                                ))
                        }// end of if block

                        shoppingItems.add(item)
                        previousCategories.add(item.productCategory)
                    }// end of if block
                }// end of if block
            }// end of range for loop

            if(shoppingItems.size == 0)
            {
                val info = "No results for \"$filterBy\""
                fragment.no_results_txv.text = info
                fragment.no_results_txv.visibility = View.VISIBLE
            }// end of if block
            else
            {
                fragment.no_results_txv.visibility = View.INVISIBLE
            }// end of else block
        }// end of else block

        mCurrentCategory = ""
        notifyDataSetChanged()
    }// end of function filterItems

    /**
     * Allows the user to update an item in the database
     *
     * @param item  The item that should be updated in the list
     */
    private fun updateItem(item: ShoppingListItemEntity)
    {
        fragment.updateId = item.id
        fragment.toggleAddItemSheet(item)
    }// end of function updateItem

    //endregion

}// end of class ShoppingListAdapter