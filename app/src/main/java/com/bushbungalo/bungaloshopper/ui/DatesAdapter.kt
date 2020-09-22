package com.bushbungalo.bungaloshopper.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.bushbungalo.bungaloshopper.R
import com.bushbungalo.bungaloshopper.databinding.MainFragmentListItemBinding
import com.bushbungalo.bungaloshopper.databinding.RemoveItemPromptLayoutBinding
import com.bushbungalo.bungaloshopper.utils.Utils
import com.bushbungalo.bungaloshopper.utils.Utils.loadCustomFont
import com.bushbungalo.bungaloshopper.utils.Utils.longDayAndMonthDate
import kotlinx.android.synthetic.main.main_fragment_list_item.view.*
import java.util.*

class DatesAdapter(private var shoppingDates: MutableList<Long>,
                   private var fragmentListener: ListItemListener):
    RecyclerView.Adapter<DatesAdapter.ShoppingListViewHolder>()
{
    //region Declarations
    private val shoppingDatesCopy = mutableListOf<Long>()

    //endregion

    //region Field Initialization

    init
    {
        shoppingDatesCopy.addAll(shoppingDates)
    }// end of init block

    //endregion

    //region Inner class binding
    inner class ShoppingListViewHolder(shoppingDataView: View): RecyclerView.ViewHolder(shoppingDataView)
    {
        val binding = MainFragmentListItemBinding.bind(shoppingDataView)

        fun bindShoppingDate(shoppingDate: Long)
        {
            val shopDay = Date(shoppingDate)
            val day = longDayAndMonthDate.format(shopDay)

            binding.itemCrd.item_inner_layout.shopping_date_txv.text = day
        }// end of function bindShoppingDate
    }// end of inner class ShoppingListViewHolder

    //endregion

    //region Main Interface Implementations

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListViewHolder
    {
        val shoppingListView: View = LayoutInflater.from(parent.context).inflate(
            R.layout.main_fragment_list_item, parent, false
        )

        val rootView: ViewGroup =
            shoppingListView.findViewById(R.id.shopping_list_item_layout)

        loadCustomFont(rootView)

        return ShoppingListViewHolder(shoppingListView)
    }// end of function onCreateViewHolder

    override fun getItemCount() = shoppingDates.size

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
        holder.bindShoppingDate(shoppingDates[position])

        with(holder.binding)
        {
            root.setOnClickListener {
                fragmentListener.onItemClick(shoppingDates[position])
            }

            removeShoppingListIconImv.setOnClickListener {
                removeItemPrompt(holder.itemView.context, shoppingDates[position])
            }
        }
    }// end of function onBindViewHolder

    //endregion

    //region Action functions

    /**
     * Allows the list to be filtered using a specific string
     *
     * @param filterBy  The string in which the list should be filtered by
     */
    fun filterItems(filterBy: String)
    {
        shoppingDates.clear()

        if (filterBy.isEmpty())
        {
            shoppingDates.addAll(shoppingDatesCopy)
        }// end of if block
        else
        {
            for (item in shoppingDatesCopy)
            {
                val shopDay = Date(item)
                val day = longDayAndMonthDate.format(shopDay)

                if (day.toLowerCase(Locale.ROOT).contains(filterBy))
                {
                    shoppingDates.add(item)
                }// end of if block
            }// end of range for loop
        }// end of else block

        notifyDataSetChanged()
    }// end of function filterItems

    /**
     * Prompts the user before an item is removed from the list
     *
     * @param context   A valid view context
     * @param date  The date in which all items on the list belong to
     */
    private fun removeItemPrompt(context: Context, date: Long)
    {
        val updateListPromptDialogView =
            View.inflate(context, R.layout.remove_item_prompt_layout, null)

        val promptBinding = RemoveItemPromptLayoutBinding.bind(updateListPromptDialogView)
        val updateListPromptDialog: AlertDialog = AlertDialog.Builder(context).create()

        loadCustomFont(promptBinding.removeItemPromptRoot)

        val shoppingDate = longDayAndMonthDate.format(date)
        val prompt = "Do you wish to remove the shopping list for $shoppingDate from the database?"

        promptBinding.removeItemPromptTxv.text = prompt

        promptBinding.removeItemBtn.setOnClickListener {
            updateListPromptDialog.dismiss()

            removeItem(date)
        }

        promptBinding.forgetRemovingBtn.setOnClickListener {
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
        Utils.zoomInView(updateListPromptDialogView)
    }// end of function removeItemPrompt

    /**
     * Initiates the removal of an item from the list
     *
     * @param date The date in which all items on the list belong to
     */
    private fun removeItem(date: Long)
    {
        fragmentListener.deleteShoppingList(date)
    }// end of function removeItem

    //endregion

    //region Adapter Interface

    interface ListItemListener
    {
        fun onItemClick(date: Long)

        fun deleteShoppingList(date: Long)
    }// end of interface ListItemListener
    //endregion
}// end of class DatesAdapter