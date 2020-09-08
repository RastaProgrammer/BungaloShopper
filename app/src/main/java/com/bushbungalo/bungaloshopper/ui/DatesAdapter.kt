package com.bushbungalo.bungaloshopper.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bushbungalo.bungaloshopper.R
import com.bushbungalo.bungaloshopper.model.ShoppingListItemEntity
import com.bushbungalo.bungaloshopper.utils.Utils
import com.bushbungalo.bungaloshopper.utils.Utils.loadCustomFont
import com.bushbungalo.bungaloshopper.utils.Utils.longDayAndMonthDate
import com.bushbungalo.bungaloshopper.view.MainActivity
import com.bushbungalo.bungaloshopper.view.ShoppingListView
import java.util.*

class DatesAdapter(private var shoppingDates: MutableList<ShoppingListItemEntity>,
                   private var mainListener: ShoppingListView):
    RecyclerView.Adapter<DatesAdapter.ShoppingListViewHolder>()
{
    private lateinit var mLayout: View
    private lateinit var txvShoppingDate: TextView
    private lateinit var imvRemoveList: ImageView

    private val shoppingDatesCopy = mutableListOf<ShoppingListItemEntity>()

    init {
        shoppingDatesCopy.addAll(shoppingDates)
    }

    inner class ShoppingListViewHolder(shoppingData: View): RecyclerView.ViewHolder(shoppingData)
    {
        fun bindShoppingDate(shoppingDate: ShoppingListItemEntity)
        {
            val shopDay = Date(shoppingDate.shoppingDate.toString().toLong())
            val day = longDayAndMonthDate.format(shopDay)

            txvShoppingDate.text = day
        }// end of function bindShoppingDate
    }// end of inner class ShoppingListViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListViewHolder
    {
        val shoppingListView: View = LayoutInflater.from(parent.context).inflate(
            R.layout.shopping_list_item, parent, false
        )

        val rootView: ViewGroup =
            shoppingListView.findViewById(R.id.shopping_list_item_layout)

        loadCustomFont(rootView)

        mLayout = shoppingListView

        return ShoppingListViewHolder(shoppingListView)
    }// end of function onCreateViewHolder

    override fun getItemCount(): Int
    {
        return shoppingDates.size
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
        txvShoppingDate = holder.itemView.findViewById(R.id.shopping_date_txv)
        imvRemoveList = holder.itemView.findViewById(R.id.remove_shopping_list_icon_imv)

        holder.bindShoppingDate(shoppingDates[position])

        mLayout.setOnClickListener {
            (mainListener as MainActivity).mSelectedShoppingList = shoppingDates[position].shoppingDate
            (mainListener as MainActivity).loadShoppingListFragment()
            mainListener.getListByDate(shoppingDates[position].shoppingDate)
        }

        imvRemoveList.setOnClickListener {
            removeItemPrompt(holder.itemView.context, shoppingDates[position].shoppingDate)
        }
    }// end of function onBindViewHolder

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
                val shopDay = Date(item.shoppingDate)
                val day = longDayAndMonthDate.format(shopDay)

                if (day.toLowerCase(Locale.ROOT).contains(filterBy))
                {
                    shoppingDates.add(item)
                }// end of if block
            }// end of range for loop
        }// end of else block

        notifyDataSetChanged()
    }// end of function filterItems

    private fun removeItemPrompt(context: Context, date: Long)
    {
        val updateListPromptDialogView =
            View.inflate(context, R.layout.remove_item_prompt_layout, null)
        val updateListPromptDialog: android.app.AlertDialog = android.app.AlertDialog.Builder(context).create()
        val txvRemoveItem: TextView = updateListPromptDialogView.findViewById(R.id.remove_item_prompt_txv)
        val btnYes: Button = updateListPromptDialogView.findViewById(R.id.remove_item_btn)
        val btnNo: Button = updateListPromptDialogView.findViewById(R.id.forget_removing_btn)
        val rootView: ViewGroup = updateListPromptDialogView.findViewById(R.id.remove_item_prompt_root)

        loadCustomFont(rootView)

        val shoppingDate = longDayAndMonthDate.format(date)

        val prompt = "Do you wish to the shopping list for $shoppingDate from the database?"
        txvRemoveItem.text = prompt

        btnYes.setOnClickListener {
            updateListPromptDialog.dismiss()

            removeItem(date)
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
        Utils.zoomInView(updateListPromptDialogView)
    }// end of function removeItemPrompt

    private fun removeItem(date: Long)
    {
        mainListener.deleteShoppingList(date)
    }// end of function removeItem
}// end of class DatesAdapter