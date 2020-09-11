package com.bushbungalo.bungaloshopper.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bushbungalo.bungaloshopper.R
import com.bushbungalo.bungaloshopper.model.ShoppingListItemEntity
import com.bushbungalo.bungaloshopper.ui.MarginDividerItemDecoration
import com.bushbungalo.bungaloshopper.ui.ShoppingListAdapter
import com.bushbungalo.bungaloshopper.utils.Utils
import com.bushbungalo.bungaloshopper.utils.Utils.addOpacity
import com.bushbungalo.bungaloshopper.utils.Utils.loadCustomFont
import com.bushbungalo.bungaloshopper.view.BungaloShopperApp.Companion.mShoppingListViewModel
import com.google.android.material.snackbar.Snackbar
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

class ShoppingListFragment : Fragment()
{
    //region Field Declarations
    private lateinit var mainListener: ShoppingListView
    private var mAppBarState = 0

    private lateinit var selectedListAppBarLayout: ConstraintLayout
    private lateinit var selectedListSearchAppBarLayout: ConstraintLayout

    private lateinit var mShoppingListDate: TextView
    private lateinit var mShoppingListRecycler: RecyclerView
    private lateinit var mAdapter: ShoppingListAdapter

    private lateinit var mLinearLayoutManager: LinearLayoutManager

    private lateinit var viewFragment: View
    private lateinit var mAddSheet: View
    private var mPos = 0

    private lateinit var mCategoryAdapter: ArrayAdapter<String>

    // Add Item Sheet
    private lateinit var mGoingShoppingDate: TextView
    private lateinit var mShoppingItem: EditText
    private lateinit var mCategory: Spinner
    private lateinit var mItemQuantity: EditText
    private lateinit var mUnitPrice: EditText
    private lateinit var btnAddItem: Button
    private lateinit var btnCancel: Button

    // Main Bar
    private lateinit var imvAdd: ImageView
    private lateinit var imvBill: ImageView
    private lateinit var imvSearch: ImageView

    // Search Bar
    private lateinit var edtSearchQuery: EditText
    private lateinit var imvBack: ImageView
    private lateinit var imvClearFilter: ImageView

    private lateinit var mContext: Context
    private var mSnackTime: View? = null

    var updateId = 0
    private var mBillTotal = 0.0
    var mFiltering = false
    lateinit var deletedItem: ShoppingListItemEntity
    var deletedItemIndex = 0

    var deleteItem = false
    var editItem = false

    private var mShoppingList = mutableListOf<ShoppingListItemEntity>()

    /**
     * Callback which handles the user swiping the recycler view items
     */
    private val iTouchCallback = object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
    {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            viewHolder1: RecyclerView.ViewHolder): Boolean
        {
            return false
        }// end of function onMove

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                 dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean)
        {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            val deleteBackground = ColorDrawable(mContext.resources.getColor(R.color.lava_red, null))
            val editBackground = ColorDrawable(mContext.resources.getColor(R.color.papaya_orange, null))
            val itemView = viewHolder.itemView
            val itemHeight = itemView.height
            val isCancelled = dX == 0f && !isCurrentlyActive

            val deleteDrawable = ContextCompat.getDrawable(mContext, R.drawable.sl_remove)
            deleteDrawable?.setTint(mContext.resources.getColor(R.color.milk_white, null))
            val editDrawable = ContextCompat.getDrawable(mContext, R.drawable.sl_edit)
            editDrawable?.setTint(mContext.resources.getColor(R.color.milk_white, null))

            val intrinsicWidth = deleteDrawable?.intrinsicWidth
            val intrinsicHeight = deleteDrawable?.intrinsicHeight

            if (isCancelled)
            {
                clearCanvas(c, itemView.right + dX, itemView.top.toFloat(),
                    itemView.right.toFloat(), itemView.bottom.toFloat())
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                return
            }// end of if block

            val deleteIconTop: Int
            val deleteIconMargin: Int
            val deleteIconLeft: Int
            val deleteIconRight: Int
            val deleteIconBottom: Int

            val editIconTop: Int
            val editIconMargin: Int
            val editIconLeft: Int
            val editIconRight: Int
            val editIconBottom: Int

            when
            {
                dX > 0 -> {
                    editBackground.setBounds(itemView.left, itemView.top,
                        itemView.left + dX.toInt(), itemView.bottom)

                    editIconTop = itemView.top + (itemHeight - intrinsicHeight!!) / 2
                    editIconMargin = (itemHeight - intrinsicHeight) / 2
                    editIconLeft = itemView.left + editIconMargin
                    editIconRight = intrinsicWidth!! + editIconMargin
                    editIconBottom = editIconTop + intrinsicHeight

                    editDrawable?.setBounds(editIconLeft,editIconTop, editIconRight, editIconBottom)
                    editBackground.draw(c)
                    editDrawable?.draw(c)

                    editItem = true
                    deleteItem = false
                }
                dX < 0 -> {
                    deleteBackground.setBounds(itemView.right + dX.toInt(),
                        itemView.top, itemView.right, itemView.bottom)

                    deleteIconTop = itemView.top + (itemHeight - intrinsicHeight!!) / 2
                    deleteIconMargin = (itemHeight - intrinsicHeight) / 2
                    deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth!!
                    deleteIconRight = itemView.right - deleteIconMargin
                    deleteIconBottom = deleteIconTop + intrinsicHeight

                    deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                    deleteBackground.draw(c)
                    deleteDrawable.draw(c)

                    deleteItem = true
                    editItem = false
                }
                else -> {
                    deleteBackground.setBounds(0, 0, 0, 0)
                    editBackground.setBounds(0, 0, 0, 0)
                    deleteItem = false
                    editItem = false
                }
            }// end of when block

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }// end of function onChildDraw

        private fun clearCanvas(c: Canvas, left: Float, top: Float, right: Float, bottom: Float)
        {
            val mClearPaint = Paint()
            mClearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

            c.drawRect(left, top, right, bottom, mClearPaint)
        }// end of function clearCanvas

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int)
        {
            deletedItem = mShoppingList[viewHolder.adapterPosition]
            deletedItemIndex = viewHolder.adapterPosition
            mShoppingList.removeAt(viewHolder.adapterPosition)
            val t = mutableListOf<ShoppingListItemEntity>()
            t.addAll(mShoppingList)

            for(i in 0 until t.size)
            {
                if(i < t.size - 1)
                {
                    if(t[i].productName == Utils.HEADER_ITEM && t[i + 1].productName == Utils.HEADER_ITEM)
                    {
                        t.removeAt(i)
                    }// end of if block
                    else if(i+1 == t.size - 1 && t[i + 1].productName == Utils.HEADER_ITEM)
                    {
                        t.removeAt(i + 1)
                    }// end of else if block
                }
            }// end of range for loop

            if(t.size == 1 && t[0].productName== Utils.HEADER_ITEM)
            {
                t.clear()
                hideBillingSnackBar()
            }// end of if block

            mShoppingList.clear()
            mShoppingList.addAll(t)
            mAdapter.notifyDataSetChanged()

            // Return the view back to its position immediately and
            // confirm that it should be deleted
            restoreItem(deletedItem, deletedItemIndex)

            // Restore
            val handler = Handler(Looper.getMainLooper())
            val runnable =  Runnable {
                if(deleteItem)
                {
                    removeItemPrompt(mContext, deletedItem)
                }// end of if block
                else if(editItem)
                {
                    hideBillingSnackBar()
                    updateId = deletedItem.id
                    toggleAddItemSheet(deletedItem)
                }// end of else block
            }// end of Runnable block

            handler.post(runnable)
        }// end of function onSwiped

        // Headers cannot be swiped away
        override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int
        {
            if (mShoppingList[viewHolder.adapterPosition].productName == Utils.HEADER_ITEM) return 0
            return super.getSwipeDirs(recyclerView, viewHolder)
        }// end of function getSwipeDirs
    }// end of iTouchCallback block

    companion object
    {
        private var mInstance = ShoppingListFragment()

        // App Bar
        private const val STANDARD_APPBAR: Int = 0
        private const val SEARCH_APPBAR = 1

        fun newInstance(): ShoppingListFragment
        {
            return mInstance
        }// end of function newInstance
    }// end of companion object

    //endregion

    //region Interface Implementations
    override fun onAttach(context: Context)
    {
        super.onAttach(context)
        mainListener = context as ShoppingListView
    }// end of function onAttach

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View?
    {
        super.onCreateView(inflater, container, savedInstanceState)

        viewFragment = inflater.inflate(R.layout.selected_shopping_list, container, false)
        mContext = viewFragment.context

        val rootView: ViewGroup = viewFragment.findViewById(R.id.day_shopping_list_root)

        loadCustomFont(rootView)

        bindViewsToFragment(viewFragment.context)

        setViewListeners()

        initRecyclerView()

        initObserver()

        setAppBarState(STANDARD_APPBAR)

        return viewFragment
    }// end of function onCreate

    override fun onResume()
    {
        super.onResume()
        setAppBarState(STANDARD_APPBAR)
    }// end of function onResume

    //endregion

    //region Main Brain

    /**
     * Add or updated an item on the shopping list
     *
     * @param shoppingItem The item to be added or updated
     */
    private fun addOrUpdate(shoppingItem: ShoppingListItemEntity)
    {
        // Use the id of the item to be updated so it will be replaced
        if(btnAddItem.text == getString(R.string.update_item))
        {
            shoppingItem.id = updateId
        }// end of if block

        mShoppingListViewModel.addItem(shoppingItem)

        mShoppingItem.setText("")
        mCategory.setSelection(0)
        mItemQuantity.setText("")
        mUnitPrice.setText("")

        // Hide the sheet
        toggleAddItemSheet(null)

        snackNotification(
            "Bill Total: $${(mBillTotal * 100.0).roundToInt() / 100.0}",
            mContext.resources.getColor(R.color.colorPrimaryDark, null))
    }// end of function

    @SuppressLint("DiscouragedPrivateApi")
    private fun animatePicker(picker: NumberPicker, from: Int, to: Int)
    {
        var counter = from
        picker.value = counter

        val increment = from <= to

        val handler = Handler(Looper.getMainLooper())

        val incrementWheel: Runnable = object : Runnable
        {
            override fun run()
            {
                val method: Method

                try
                {
                    method = picker.javaClass.getDeclaredMethod(
                        "changeValueByOne", Boolean::class.javaPrimitiveType)
                    method.isAccessible = true
                    method.invoke(picker, increment)
                }// end of try block
                catch (e: NoSuchMethodException) {}
                catch (e: IllegalArgumentException) {}
                catch (e: IllegalAccessException) {}
                catch (e: InvocationTargetException) {}
                finally
                {
                    ++counter

                    if (counter >= to)
                    {
                        return
                    }// end of if block
                    else
                    {
                        handler.postDelayed(this, 20)
                    }// end of else block
                }// end of finally block
            }// end of function run
        }// end of function incrementWheel

        handler.postDelayed(incrementWheel, 20)
    }// end of function animatePicker

    /**
     * Bind the views that will be referenced
     *
     * @param context  A valid application context
     */
    private fun bindViewsToFragment(context: Context)
    {
        mCategory = viewFragment.findViewById(R.id.item_category_spn)
        mShoppingListRecycler = viewFragment.findViewById(R.id.shopping_list_rv)
        mShoppingListDate = viewFragment.findViewById(R.id.shopping_date_txv)

        selectedListAppBarLayout = viewFragment.findViewById(R.id.main_tool_bar_layout)
        selectedListSearchAppBarLayout = viewFragment.findViewById(R.id.search_tool_bar_layout)

        edtSearchQuery = viewFragment.findViewById(R.id.search_toolbar_search_edt)
        imvBack = viewFragment.findViewById(R.id.back_to_normal_imv)
        imvSearch = viewFragment.findViewById(R.id.search_shopping_list_imv)
        imvAdd = viewFragment.findViewById(R.id.add_to_shopping_list_imv)
        imvBill = viewFragment.findViewById(R.id.bill_total_imv)
        imvClearFilter = viewFragment.findViewById(R.id.clear_filter_imv)

        mAddSheet = viewFragment.findViewById(R.id.add_item_layout)
        mShoppingItem = viewFragment.findViewById(R.id.item_name_txv)
        mItemQuantity = viewFragment.findViewById(R.id.item_quantity_txv)
        mUnitPrice = viewFragment.findViewById(R.id.item_unit_price_txv)

        btnAddItem = viewFragment.findViewById(R.id.add_item_btn)
        btnCancel = viewFragment.findViewById(R.id.cancel_btn)
        mGoingShoppingDate = viewFragment.findViewById(R.id.going_shopping_on_txv)

        mAddSheet.animate()
            .translationYBy(0f)
            .translationY(1000f)
            .duration = 0

        val categories = resources.getStringArray(R.array.categories)

        mCategoryAdapter = ArrayAdapter(context,
            android.R.layout.simple_spinner_item, categories)

        mCategory.adapter = mCategoryAdapter
    }// end of function bindViewsToFragment

    /**
     * Calculates a running total for the bill
     */
    fun calculateBillTotal()
    {
        mBillTotal = 0.0

        for(item in mShoppingList)
        {
            mBillTotal += (item.productQuantity * item.unitPrice)
        }// end or range loop

        snackNotification("Bill Total (-Tax): ${
                Utils.convertToCurrency(mBillTotal, Locale.US)}",
            mContext.resources.getColor(R.color.colorPrimaryDark, null))

        // Hide the bill total after five seconds
        val handler = Handler(Looper.getMainLooper())
        val runnable = Runnable {
            hideBillingSnackBar()
        }

        handler.postDelayed(runnable, 5000)
    }// end of function calculateBillTotal

    /**
     * Returns a list with group heading inserted into the list
     *
     * @param ungroupedList The list in its original order
     * @return The grouped list
     */
    private fun groupedList(ungroupedList: MutableList<ShoppingListItemEntity>) :
            MutableList<ShoppingListItemEntity>
    {
        val gList = mutableListOf<ShoppingListItemEntity>()
        var currentCategory = ungroupedList[0].productCategory
        var categoryChange: Boolean

        gList.add(
            ShoppingListItemEntity(
                id = 0,
                shoppingListId = "",
                productName = Utils.HEADER_ITEM,
                productCategory = ungroupedList[0].productCategory,
                productQuantity = 0,
                unitPrice = 0.0,
                shoppingDate = ungroupedList[0].shoppingDate
            )
        )

        for(item in ungroupedList)
        {
            if(currentCategory == item.productCategory)
            {
                categoryChange = false
            }// end of if block
            else
            {
                currentCategory = item.productCategory
                categoryChange = true
            }// end of else block

            if(categoryChange)
            {
                gList.add(
                    ShoppingListItemEntity(
                        id = 0,
                        shoppingListId = "",
                        productName = Utils.HEADER_ITEM,
                        productCategory = item.productCategory,
                        productQuantity = 0,
                        unitPrice = 0.0,
                        shoppingDate = item.shoppingDate
                    )
                )
                gList.add(item)
            }// end of if block
            else
            {
                gList.add(item)
            }// end of else block
        }// end of range for loop

        return gList
    }// end of function groupedList

    /**
     * Hide the [Snackbar] which displays the current bill total
     */
    fun hideBillingSnackBar()
    {
        if(mSnackTime != null)
        {
            if(mSnackTime!!.visibility == View.VISIBLE)
            {
                // animate the view downward before removing it from the layout
                mSnackTime!!.animate()
                    .translationY(200f)
                    .setDuration(500)
                    .withEndAction {
                        (mSnackTime!!.parent as ViewGroup)
                            .removeView(mSnackTime)
                    }
            }// end of if block
        }// end of if block
    }// end of function hideBillingSnackBar

    /**
     * Show the [Snackbar] which displays the current bill total
     */
    private fun showBillingSnackBar()
    {
        snackNotification("Bill Total (-Tax): ${
            Utils.convertToCurrency(mBillTotal, Locale.US)}",
            mContext.resources.getColor(R.color.colorPrimaryDark, null))
    }// end of function showBillingSnackBar

    /**
     * Initialize the observers
     */
    private fun initObserver()
    {
        val formatterDate = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.ENGLISH)
        val sListDate = Date((mainListener as MainActivity).mSelectedShoppingList)

        // Get the shopping list for a specific date
        mShoppingListViewModel.initializeProducts(sListDate.time)

        mShoppingListViewModel.mShoppingList.observe(viewLifecycleOwner,
            { shoppingLists ->
                mShoppingList.clear()
                mShoppingList.addAll(groupedList(shoppingLists))
                mAdapter = ShoppingListAdapter(mShoppingList, mInstance)
                mShoppingListRecycler.adapter = mAdapter
                mAdapter.notifyDataSetChanged()

                mShoppingListDate.text = formatterDate.format(sListDate)
            })
    }// end of function initObserver

    /**
     * Initialize the recycler view
     */
    private fun initRecyclerView()
    {
        mLinearLayoutManager = LinearLayoutManager(context)
        mShoppingListRecycler.setHasFixedSize(true)

        val divider = MarginDividerItemDecoration(
            BungaloShopperApp.shopperContext,
            mLinearLayoutManager.orientation,
            Utils.devicePixelsToPixels(8, BungaloShopperApp.shopperContext)
        )

        mShoppingListRecycler.addItemDecoration(divider)
        mShoppingListRecycler.layoutManager = mLinearLayoutManager
        ItemTouchHelper(iTouchCallback).attachToRecyclerView(mShoppingListRecycler)
    } // end of function initRecyclerView

    /**
     * Loads the dates that the date picker will contain usually a
     * two months span
     *
     * @param firstDate The starting date
     * @param secondDate The ending date
     */
    private fun loadDates(firstDate: String, secondDate: String): Array<String>
    {
        var start: Date = Utils.shortFormatterDate.parse(firstDate)!!
        val end: Date = Utils.shortFormatterDate.parse(secondDate)!!
        val totalDates: MutableList<String> = ArrayList()
        val c = Calendar.getInstance()
        c.time = start

        while (start.before(end))
        {
            totalDates.add(Utils.shortFormatterWeekDay.format(start))
            c.add(Calendar.DATE, 1)
            start = c.time
        }// end of while loop

        return totalDates.toTypedArray()
    }// end of function loadDates

    /**
     * Displays a prompt to confirm that the user wishes to remove an item from the list
     *
     * @param context  A valid application context
     * @param item     The item that should be removed from the list
     */
    fun removeItemPrompt(context: Context, item: ShoppingListItemEntity)
    {
        val updateListPromptDialogView =
            View.inflate(context, R.layout.remove_item_prompt_layout, null)
        val updateListPromptDialog: android.app.AlertDialog = android.app.AlertDialog.Builder(
            context
        ).create()
        val txvRemoveItem: TextView = updateListPromptDialogView.findViewById(R.id.remove_item_prompt_txv)
        val btnYes: Button = updateListPromptDialogView.findViewById(R.id.remove_item_btn)
        val btnNo: Button = updateListPromptDialogView.findViewById(R.id.forget_removing_btn)
        val rootView: ViewGroup = updateListPromptDialogView.findViewById(R.id.remove_item_prompt_root)

        val prompt = "Do you wish to remove ${item.productName} from the list?"

        loadCustomFont(rootView)

        txvRemoveItem.text = prompt

        btnYes.setOnClickListener {
            updateListPromptDialog.dismiss()

            mShoppingList.removeAt(deletedItemIndex)

            // Remove the item from the database table
            removeItem(item)
        }

        btnNo.setOnClickListener {
            updateListPromptDialog.dismiss()

            //restoreItem(deletedItem, deletedItemIndex)
            calculateBillTotal()
        }

        updateListPromptDialog.setView(updateListPromptDialogView)
        updateListPromptDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        updateListPromptDialog.setCancelable(false)
        updateListPromptDialog.show()

        // Adjust the layout after the window is displayed
        val dialogWindow: Window = updateListPromptDialog.window!!

        dialogWindow.setLayout(980, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialogWindow.setGravity(Gravity.CENTER)
        Utils.zoomInView(updateListPromptDialogView)
    }// end of function removeItemPrompt

    /**
     * Removes an item from the database table
     *
     * @param item The item that should be removed from the list
     */
    private fun removeItem(item: ShoppingListItemEntity)
    {
        mainListener.deleteShoppingListItem(item)
    }// end of function removeItem

    /**
     * Restores an item that was removed from the recycler view
     *
     * @param item  The item that was removed
     * @param position  The location that the item was removed from
     */
    private fun restoreItem(item: ShoppingListItemEntity, position: Int)
    {
        val previousCategories = mutableListOf<String>()

        // Keep track of a the categories that a currently displayed
        // in the recycler view
        for (listItem in mShoppingList)
        {
            if(!previousCategories.contains(listItem.productCategory))
            {
                previousCategories.add(listItem.productCategory)
            }// end of if block
        }// end of for each loop

        if(!previousCategories.contains(item.productCategory))
        {
            mShoppingList.add(
                position - 1,
                ShoppingListItemEntity(
                    id = 0,
                    shoppingListId = "",
                    productName = Utils.HEADER_ITEM,
                    productCategory = item.productCategory,
                    productQuantity = 0,
                    unitPrice = 0.0,
                    shoppingDate = item.shoppingDate
                )
            )

            mAdapter.notifyItemInserted(position - 1)
        }// end of if block

        mShoppingList.add(position, item)
        mAdapter.notifyItemInserted(position)
    }// end of function restoreItem

    /**
     * Set the state of the app bar
     *
     * @param state The current state of the app bar
     */
    private fun setAppBarState(state: Int)
    {
        mAppBarState = state

        if (mAppBarState == STANDARD_APPBAR)
        {
            selectedListSearchAppBarLayout.animate()
                .alpha(1f)
                .setDuration(250)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        selectedListSearchAppBarLayout.visibility = View.GONE
                        selectedListAppBarLayout.visibility = View.VISIBLE
                    }
                })

            val view = view
            val im: InputMethodManager =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            try
            {
                im.hideSoftInputFromWindow(view!!.windowToken, 0) // make keyboard hide
            }// end of try block
            catch (e: NullPointerException) {}
        }// end of if block
        else if (mAppBarState == SEARCH_APPBAR)
        {
            selectedListAppBarLayout.animate()
                .alpha(1f)
                .setDuration(250)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        selectedListAppBarLayout.visibility = View.GONE
                        selectedListSearchAppBarLayout.visibility = View.VISIBLE
                    }
                })

            val im: InputMethodManager =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0) // make keyboard popup
        }// end of else if block
    }// end of function setAppBarState

    /**
     * Setup the listener for the view
     */
    private fun setViewListeners()
    {
        btnAddItem.setOnClickListener {
            val itemId = "bs${SimpleDateFormat("HHmmssSSS", Locale.ENGLISH).format(Date())}"
            val shoppingDate = Utils.longDayAndMonthDate.parse(mGoingShoppingDate.text.toString())

            val newItem = ShoppingListItemEntity(
                0,
                itemId,
                Utils.toProperCase(mShoppingItem.text.toString()),
                mCategory.selectedItem.toString(),
                if (mItemQuantity.text.toString().isNotEmpty()) mItemQuantity.text.toString()
                    .toInt() else 1,
                if (mUnitPrice.text.toString().isNotEmpty()) mUnitPrice.text.toString()
                    .toDouble() else 0.0,
                shoppingDate!!.time
            )

            if(btnAddItem.text == getString(R.string.update_item))
            {
                addOrUpdate(newItem)
            }// end of if block
            else
            {
                addOrUpdate(newItem)
            }// end of else block
        }// end of btnAddItem.setOnClickListener block

        btnCancel.setOnClickListener {
            toggleAddItemSheet(null)
            mShoppingItem.setText("")
            mCategory.setSelection(0)
            mItemQuantity.setText("")
            mUnitPrice.setText("")
        }// end of btnCancel.setOnClickListener block

        mGoingShoppingDate.setOnClickListener {
            showDateTimeDialog(mContext)
        }// end of mGoingShoppingDate.setOnClickListener block

        imvAdd.setOnClickListener {
            toggleAddItemSheet(null)
            hideBillingSnackBar()
        }// end of imvAdd.setOnClickListener block

        imvBill.setOnClickListener {
            showBillingSnackBar()
        }// end of imvBill.setOnClickListener block

        imvSearch.setOnClickListener {
            toggleToolBarState()
        }// end of imvSearch.setOnClickListener block

        imvBack.setOnClickListener {
            toggleToolBarState()
        }// end of function imvBack.setOnClickListener

        edtSearchQuery.addTextChangedListener(object : TextWatcher
        {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int)
            {
                if (edtSearchQuery.text.isNotEmpty())
                {
                    imvClearFilter.visibility = View.VISIBLE
                }// end of if block
                else {
                    imvClearFilter.visibility = View.INVISIBLE
                }// end of if block

                mAdapter.filterItems(edtSearchQuery.text.toString())
            }
        })// end of edtSearchQuery.addTextChangedListener block

        imvClearFilter.setOnClickListener{
            edtSearchQuery.setText("")
        }// end of imvClearFilter.setOnClickListener block
    }// end of function setViewListeners

    /**
     * Show a custom date time picker
     *
     * @param c The context of the current fragment
     */
    private fun showDateTimeDialog(c: Context)
    {
        val dtPickerDialogView =
            View.inflate(c, R.layout.date_time_picker, null)
        val dtDialog: android.app.AlertDialog = android.app.AlertDialog.Builder(c).create()

        val rootView: ViewGroup = dtPickerDialogView.findViewById(R.id.time_root)
        loadCustomFont(rootView)

        val hourOfDay: NumberPicker = dtPickerDialogView.findViewById(R.id.hour_pick_txv)

        val d = Calendar.getInstance()

        hourOfDay.apply {
            minValue = 1
            maxValue = 12
            value = d.get(Calendar.HOUR)
        }

        val timeSeparator: NumberPicker = dtPickerDialogView.findViewById(R.id.time_separator_txv)

        timeSeparator.apply {
            minValue = 0
            maxValue = 0
            displayedValues = arrayOf(":")
        }

        val minuteOfDay: NumberPicker = dtPickerDialogView.findViewById(R.id.minute_pick_txv)

        minuteOfDay.apply {
            minValue = 0
            maxValue = 59
            value = d.get(Calendar.MINUTE)

            // Format the digits
            setFormatter{ i -> String.format("%02d", i)
            }
        }

        val meridiem: NumberPicker =
            dtPickerDialogView.findViewById(R.id.time_meridiem_txv)
        var mer = 0

        if (d.get(Calendar.AM_PM) == Calendar.PM)
        {
            mer = 1
        }// end of if block

        val meridiemArray = arrayOf("AM", "PM")

        meridiem.apply {
            minValue = 0
            maxValue = 1
            displayedValues = meridiemArray
            value = mer
        }

        hourOfDay.setOnValueChangedListener { _, from, to ->
            if(from == 11 && to == 12)
            {
                if(mer == 0)
                {
                    meridiem.value = 1
                    animatePicker(meridiem, 1, 0)
                    mer = 1
                }// end of if block
                else
                {
                    meridiem.value = 0
                    animatePicker(meridiem, 0, 1)
                    mer = 0
                }// end of else block
            }// end of if block
            else if(from == 12 && to == 11)
            {
                if(mer == 0)
                {
                    meridiem.value = 1
                    animatePicker(meridiem, 1, 0)
                    mer = 1
                }// end of if block
                else
                {
                    meridiem.value = 0
                    animatePicker(meridiem, 0, 1)
                    mer = 0
                }// end of else block
            }// end of else if block
        }// end of hourOfDay.setOnValueChangedListener block

        // Load the dates to pick from
        val aCalendar = Calendar.getInstance()

        // Add -1 month to current month
        aCalendar.add(Calendar.MONTH, -1)

        // Set actual maximum date of previous month
        aCalendar[Calendar.DATE] = aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val lastDateOfPreviousMonth = Utils.shortFormatterDate.format(aCalendar.time)

        // Add 2 months to last month for next month
        aCalendar.add(Calendar.MONTH, 2)

        // Set actual maximum date of next month
        aCalendar[Calendar.DATE] = aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val endOfNextMonth = Utils.shortFormatterDate.format(aCalendar.time)

        val dates = if(Utils.daysBetweenDates(
                Utils.shortFormatterDate.parse(lastDateOfPreviousMonth)!!, Date()
            ) > 5)
        {
            loadDates(lastDateOfPreviousMonth, endOfNextMonth)
        }// end of if block
        else
        {
            // If a new month just began, start at the middle of last month
            val cCalendar = Calendar.getInstance()
            val midLastMonth = Utils.shortFormatterDate.parse(lastDateOfPreviousMonth)!!
            cCalendar.time = midLastMonth
            cCalendar.add(Calendar.DAY_OF_MONTH, -15)
            val middleOfPreviousMonth = Utils.shortFormatterDate.format(cCalendar.time)

            loadDates(middleOfPreviousMonth, endOfNextMonth)
        }// end of else block

        val today: String = Utils.shortFormatterWeekDay.format(d.time)
        var todayIndex = 0

        // Check the array to see it it contains today's date and get its index
        // in the array
        if(Arrays.stream(dates).anyMatch { t -> t == today })
        {
            todayIndex = dates.indexOf(today)
        }// end of if block

        val datesPicker: NumberPicker =
            dtPickerDialogView.findViewById(R.id.day_pick_txv)

        datesPicker.apply{
            minValue = 0
            maxValue = dates.size - 1
            displayedValues = dates
            value = todayIndex
        }

        val btnSetDateTime: Button = dtPickerDialogView.findViewById(R.id.set_date_time_txv)

        btnSetDateTime.setOnClickListener {
            // Format the date the user selected
            val dateSelected = "${dates[datesPicker.value]}, ${d.get(Calendar.YEAR)} " +
                    "${hourOfDay.value}: ${minuteOfDay.value} ${meridiemArray[meridiem.value]}"
            val dd =  Utils.formatterNoTimeZone.parse(dateSelected)

            mGoingShoppingDate.text = Utils.longDayAndMonthDate.format(dd!!)
            dtDialog.dismiss()
        }

        val btnCancelDateTime: Button = dtPickerDialogView.findViewById(R.id.cancel_date_time_btn)

        btnCancelDateTime.setOnClickListener{
            dtDialog.dismiss()
        }

        dtDialog.setView(dtPickerDialogView)
        dtDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dtDialog.setCancelable(false)
        dtDialog.show()

        // Animate the dials of the date time picker
        val h = d.get(Calendar.HOUR)
        val m = d.get(Calendar.MINUTE)

        animatePicker(datesPicker, abs(todayIndex - 5), todayIndex)
        animatePicker(hourOfDay, abs(h - 5), h)
        animatePicker(minuteOfDay, abs(m - 5), m)

        if(mer == 0)
        {
            meridiem.value = 1
            animatePicker(meridiem, 1, 0)
        }// end of if block
        else
        {
            meridiem.value = 0
            animatePicker(meridiem, 0, 1)
        }// end of else block

    } // end of function showDateTimeDialog

    /**
     * Displays a snack rar containing a message
     *
     * @param  message  The message to be displayed to the user
     * @param  bgColor  The background color of the [Snackbar]
     */
    private fun snackNotification(message: String, bgColor: Int)
    {
        val activityView: View = (mainListener as MainActivity).window.decorView.findViewById(
            android.R.id.content)
        val actionReaction = Snackbar.make(activityView, message, Snackbar.LENGTH_INDEFINITE)

        actionReaction.setAction("DISMISS") {
            actionReaction.dismiss()
        }

        val billingView = actionReaction.view
        billingView.setBackgroundColor(
            addOpacity(
                bgColor, 90
            )
        )
        val infoText: TextView = billingView.findViewById(
            com.google.android.material.R.id.snackbar_text
        )

        mSnackTime = actionReaction.view
        infoText.setTextColor(Color.WHITE)
        actionReaction.setActionTextColor(Color.GREEN)
        actionReaction.show()
    }// end of function snackNotification

    /**
     * Show the sheet for adding items
     */
    fun toggleAddItemSheet(shoppingItem: ShoppingListItemEntity?)
    {
        val h = mAddSheet.measuredHeight

        if(mPos == 1)
        {
            mAddSheet.animate()
                .translationYBy(0f)
                .translationY(h.toFloat())
                .setDuration(500)
                .withEndAction {
                    mPos = 0
                }
        }// end of if block
        else
        {
            mAddSheet.animate()
                .translationYBy(h.toFloat())
                .translationY(0f)
                .setDuration(500)
                .withEndAction {
                    mPos = 1
                }
        }// end of else block

        if(shoppingItem != null)
        {
            mGoingShoppingDate.text = Utils.longDayAndMonthDate.format(shoppingItem.shoppingDate)
            mShoppingItem.setText(shoppingItem.productName)
            mCategory.setSelection(mCategoryAdapter.getPosition(shoppingItem.productCategory))
            mItemQuantity.setText(shoppingItem.productQuantity.toString())
            mUnitPrice.setText(shoppingItem.unitPrice.toString())

            btnAddItem.text = getString(R.string.update_item)
        }// end of if block
        else
        {
            btnAddItem.text = getString(R.string.add_item)
        }// end of else block
    }// end of function toggleAddItemSheet

    /**
     * Toggles the state of the app bar
     */
    private fun toggleToolBarState()
    {
        mFiltering = if (mAppBarState == STANDARD_APPBAR)
        {
            setAppBarState(SEARCH_APPBAR)
            true
        }// end of if block
        else
        {
            setAppBarState(STANDARD_APPBAR)
            mAdapter.filterItems("")
            false
        }// end of else block
    }// end of function toggleToolBarState

    //endregion
}// end of class ShoppingListFragment