package com.bushbungalo.bungaloshopper.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bushbungalo.bungaloshopper.R
import com.bushbungalo.bungaloshopper.model.ShoppingListItemEntity
import com.bushbungalo.bungaloshopper.utils.Utils
import com.bushbungalo.bungaloshopper.utils.Utils.loadCustomFont
import com.bushbungalo.bungaloshopper.utils.Utils.zoomInView
import com.bushbungalo.bungaloshopper.view.BungaloShopperApp
import com.bushbungalo.bungaloshopper.view.ShoppingListFragment
import com.bushbungalo.bungaloshopper.view.ShoppingListView
import java.util.*

class ShoppingListAdapter(private var shoppingItems: MutableList<ShoppingListItemEntity>,
                          private var fragment: ShoppingListFragment,
                          private var mainListener: ShoppingListView):
    RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder>()
{
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

    init {
        shoppingListCopy.addAll(shoppingItems)
    }

    inner class ShoppingListViewHolder(taskItem: View): RecyclerView.ViewHolder(taskItem)
    {
        fun bindShoppingList(shoppingItem: ShoppingListItemEntity)
        {
            if(categoryChange)
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
                txvProductItem.text = shoppingItem.productName
            }// end of if block
            else
            {
                txvProductItem.text = shoppingItem.productName
            }// end of else block
        }// end of function bindShoppingList
    }// end of inner class ShoppingListViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListViewHolder
    {
        categoryChange =
            mCurrentCategory.isNotEmpty() && mCurrentCategory != shoppingItems[viewType].productCategory

        val shoppingListView: View = LayoutInflater.from(parent.context).inflate(
            R.layout.selected_list_item, parent, false
        )

        val rootView: ViewGroup =
            shoppingListView.findViewById(R.id.shopping_list_item_layout)

        loadCustomFont(rootView)

        mCategoryHeading = shoppingListView.findViewById(R.id.product_category_layout)
        mLayout = shoppingListView

        return ShoppingListViewHolder(shoppingListView)
    }// end of function onCreateViewHolder

    override fun getItemCount(): Int
    {
        return shoppingItems.size
    }// end of function getItemCount

    override fun getItemId(position: Int): Long
    {
        return position.toLong()
    }// end of function getItemId

    override fun getItemViewType(position: Int): Int
    {
        return position
    }// end of function getItemViewType

    override fun onBindViewHolder(holder: ShoppingListViewHolder, position: Int)
    {
        imvProductCategoryIcon = holder.itemView.findViewById(R.id.category_icon_imv)
        txvProductCategoryName = holder.itemView.findViewById(R.id.category_name_txv)
        txvProductItem = holder.itemView.findViewById(R.id.item_name_txv)
        imvEditIcon = holder.itemView.findViewById(R.id.edit_icon_imv)
        imvRemoveIcon = holder.itemView.findViewById(R.id.remove_icon_imv)

        val categoryBar: ConstraintLayout = holder.itemView.findViewById(R.id.product_category_layout)
        val dimsParams: ViewGroup.LayoutParams = categoryBar.layoutParams
        val marginParams = categoryBar.layoutParams as ViewGroup.MarginLayoutParams

        if(mCurrentCategory == shoppingItems[position].productCategory)
        {
            dimsParams.height = 1   // Make the column header appear as a divider
            marginParams.leftMargin =
                Utils.devicePixelsToPixels(16, BungaloShopperApp.shopperContext)
            categoryBar.requestLayout()
            categoryChange = false
        }// end of if block
        else
        {
            mCurrentCategory = shoppingItems[position].productCategory
            categoryChange = true
            dimsParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            marginParams.leftMargin = 0
            categoryBar.requestLayout()
        }// end of else block

        holder.bindShoppingList(shoppingItems[position])

        imvEditIcon.setOnClickListener {
            updateItem(shoppingItems[position])
        }

        imvRemoveIcon.setOnClickListener {
            removeItemPrompt(holder.itemView.context, shoppingItems[position])
        }
    }// end of function onBindViewHolder

    fun filterItems(filterBy: String)
    {
        shoppingItems.clear()

        if (filterBy.isEmpty())
        {
            shoppingItems.addAll(shoppingListCopy)
        }// end of if block
        else
        {
            for (item in shoppingListCopy)
            {
                if (item.productName.toLowerCase(Locale.ROOT).contains(filterBy))
                {
                    shoppingItems.add(item)
                }// end of if block
            }// end of range for loop
        }// end of else block

        mCurrentCategory = ""
        notifyDataSetChanged()
    }// end of function filterItems

    private fun removeItemPrompt(context: Context, item: ShoppingListItemEntity)
    {
        val updateListPromptDialogView =
            View.inflate(context, R.layout.remove_item_prompt_layout, null)
        val updateListPromptDialog: android.app.AlertDialog = android.app.AlertDialog.Builder(context).create()
        val txvRemoveItem: TextView = updateListPromptDialogView.findViewById(R.id.remove_item_prompt_txv)
        val btnYes: Button = updateListPromptDialogView.findViewById(R.id.remove_item_btn)
        val btnNo: Button = updateListPromptDialogView.findViewById(R.id.forget_removing_btn)
        val rootView: ViewGroup = updateListPromptDialogView.findViewById(R.id.remove_item_prompt_root)

        loadCustomFont(rootView)

        val prompt = "Do you wish to remove ${item.productName} from the list?"
        txvRemoveItem.text = prompt

        btnYes.setOnClickListener {
            updateListPromptDialog.dismiss()

            removeItem(item)
        }

        btnNo.setOnClickListener {
            updateListPromptDialog.dismiss()
        }

        updateListPromptDialog.setView(updateListPromptDialogView)
        updateListPromptDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        updateListPromptDialog.setCancelable(false)
        updateListPromptDialog.show()

        // Adjust the layout after the window is displayed
        val dialogWindow: Window = updateListPromptDialog.window!!

        dialogWindow.setLayout(980,ViewGroup.LayoutParams.WRAP_CONTENT)
        dialogWindow.setGravity(Gravity.CENTER)
        zoomInView(updateListPromptDialogView)
    }// end of function removeItemPrompt

    private fun removeItem(item: ShoppingListItemEntity)
    {
        mainListener.deleteShoppingListItem(item)
    }// end of function removeItem

    private fun updateItem(item: ShoppingListItemEntity)
    {
        fragment.updateId = item.id
        fragment.toggleAddItemSheet(item)
    }// end of function updateItem
}// end of class ShoppingListAdapter