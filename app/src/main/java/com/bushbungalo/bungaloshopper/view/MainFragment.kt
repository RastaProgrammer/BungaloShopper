package com.bushbungalo.bungaloshopper.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bushbungalo.bungaloshopper.R
import com.bushbungalo.bungaloshopper.model.ShoppingListItemEntity
import com.bushbungalo.bungaloshopper.ui.CategoriesAdapter
import com.bushbungalo.bungaloshopper.ui.DatesAdapter
import com.bushbungalo.bungaloshopper.utils.Utils
import com.bushbungalo.bungaloshopper.utils.Utils.loadCustomFont
import com.bushbungalo.bungaloshopper.view.BungaloShopperApp.Companion.mShoppingListViewModel
import com.bushbungalo.bungaloshopper.view.BungaloShopperApp.Companion.mTotalListViewModel
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class MainFragment : Fragment()
{
    //region Declarations
    private lateinit var listener: ShoppingListView
    private lateinit var txvFragmentTitle: TextView
    private lateinit var txvListDescription: TextView
    private lateinit var rcvStoredShoppingLists: RecyclerView
    private lateinit var noListLayout: ConstraintLayout
    private lateinit var scvBodyScroller: ScrollView

    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: DatesAdapter

    private lateinit var viewFragment: View

    private var mAppBarState = 0
    private lateinit var selectedListAppBarLayout: ConstraintLayout
    private lateinit var selectedListSearchAppBarLayout: ConstraintLayout

    // Main Bar
    private lateinit var imvAdd: ImageView
    private lateinit var imvBill: ImageView
    private lateinit var imvSearch: ImageView

    // Search Bar
    private lateinit var edtSearchQuery: EditText
    private lateinit var imvBack: ImageView
    private lateinit var imvClearFilter: ImageView

    // Add Item Sheet
    private lateinit var mGoingShoppingDate: TextView
    private lateinit var mShoppingItem: EditText
    private lateinit var mCategory: Spinner
    private lateinit var mItemQuantity: EditText
    private lateinit var mUnitPrice: EditText
    private lateinit var btnAddItem: Button
    private lateinit var btnCancel: Button

    private lateinit var mAddSheet: View
    private lateinit var mCategoryAdapter: ArrayAdapter<String>

    private var mPos = 0

    private lateinit var mContext: Context

    companion object
    {
        private var mInstance = MainFragment()

        // App Bar
        private const val STANDARD_APPBAR: Int = 0
        private const val SEARCH_APPBAR = 1

        fun newInstance(): MainFragment
        {
            return mInstance
        }// end of function newInstance
    }// end of companion object

    //endregion

    //region Interface Implementations
    override fun onAttach(context: Context)
    {
        super.onAttach(context)
        listener = context as ShoppingListView
    }// end of function onAttach

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        super.onCreateView(inflater, container, savedInstanceState)

        viewFragment = inflater.inflate(
            R.layout.shopping_list, container, false
        )

        mContext = viewFragment.context

        val rootView: ViewGroup = viewFragment.findViewById(R.id.all_shopping_lists_layout)

        bindViewsToFragment()

        loadCustomFont(rootView)

        initRecyclerView()

        setViewListeners()

        imvBill.visibility = View.GONE
        txvFragmentTitle.text = resources.getString(R.string.main_fragment_name)

        return viewFragment
    }// end of function onCreate

    override fun onResume()
    {
        super.onResume()
        setAppBarState(STANDARD_APPBAR)
    }// end of function onResume

    //endregion

    //region Setup

    /**
     * Bind the views that will be referenced
     *
     */
    private fun bindViewsToFragment()
    {
        txvFragmentTitle = viewFragment.findViewById(R.id.search_toolbar_title_txv)
        txvListDescription = viewFragment.findViewById(R.id.shopping_lists_txv)
        rcvStoredShoppingLists = viewFragment.findViewById(R.id.stored_lists_rcv)
        scvBodyScroller = viewFragment.findViewById(R.id.daily_lists_body_scroller)
        noListLayout = viewFragment.findViewById(R.id.no_lists_layout)
        mCategory = viewFragment.findViewById(R.id.item_category_spn)

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
        mCategoryAdapter = CategoriesAdapter(mContext, categories.toMutableList())
        mCategory.adapter = mCategoryAdapter

    }// end of function bindViewsToFragment

    /**
     * Initialize the observers
     */
    private fun initObserver()
    {
        mTotalListViewModel.initializeAllShoppingLists()
        mTotalListViewModel.getAllShoppingList()

        mTotalListViewModel.mAllLists.observe(viewLifecycleOwner,
            { shoppingListsEntities ->
                val noDuplicateDates = mutableListOf<Long>()

                for (s in shoppingListsEntities)
                {
                    if (!noDuplicateDates.contains(s.shoppingDate))
                    {
                        noDuplicateDates.add(s.shoppingDate)
                    }// end of if block
                }// end of for range loop

                if (noDuplicateDates.size == 0) {
                    noListLayout.visibility = View.VISIBLE
                    scvBodyScroller.visibility = View.INVISIBLE
                }// end of if block
                else {
                    noListLayout.visibility = View.INVISIBLE
                    scvBodyScroller.visibility = View.VISIBLE
                }// end of else block

                mAdapter = DatesAdapter(noDuplicateDates, listener)
                rcvStoredShoppingLists.adapter = mAdapter
                mAdapter.notifyDataSetChanged()
            })
    }// end of function initObserver

    /**
     * Initialize the recycler view
     */
    private fun initRecyclerView()
    {
        mLinearLayoutManager = LinearLayoutManager(context)
        rcvStoredShoppingLists.setHasFixedSize(true)
        rcvStoredShoppingLists.layoutManager = mLinearLayoutManager
        initObserver()
    }// end of function initRecyclerView

    /**
     * Setup the listener for the view
     */
    private fun setViewListeners()
    {
        imvSearch.setOnClickListener {
            toggleToolBarState()
        }

        imvBack.setOnClickListener {
            toggleToolBarState()
        }

        edtSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (edtSearchQuery.text.isNotEmpty()) {
                    imvClearFilter.visibility = View.VISIBLE
                }// end of if block
                else {
                    imvClearFilter.visibility = View.INVISIBLE
                }// end of if block

                mAdapter.filterItems(edtSearchQuery.text.toString())
            }
        })

        imvClearFilter.setOnClickListener{
            edtSearchQuery.setText("")
        }

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
                createShoppingList(newItem)
            }// end of if block
            else
            {
                createShoppingList(newItem)
            }// end of else block
        }

        btnCancel.setOnClickListener {
            toggleAddItemSheet()
            mShoppingItem.setText("")
            mCategory.setSelection(0)
            mItemQuantity.setText("")
            mUnitPrice.setText("")
        }
        mGoingShoppingDate.setOnClickListener {
            showDateTimeDialog(mContext)
        }

        imvAdd.setOnClickListener {
            toggleAddItemSheet()
        }
    }// end of function setViewListeners
    //endregion

    //region Database Actions

    /**
     * Add or updated an item on the shopping list
     *
     * @param shoppingItem The item to be added or updated
     */
    private fun createShoppingList(shoppingItem: ShoppingListItemEntity)
    {
        mShoppingListViewModel.addItem(shoppingItem)

        mShoppingItem.setText("")
        mCategory.setSelection(0)
        mItemQuantity.setText("")
        mUnitPrice.setText("")

        // Hide the sheet
        toggleAddItemSheet()
    }// end of function createShoppingList

    //endregion

    //region AppBar functions

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
     * Toggles the state of the app bar
     */
    private fun toggleToolBarState()
    {
        if (mAppBarState == STANDARD_APPBAR)
        {
            setAppBarState(SEARCH_APPBAR)
        }// end of if block
        else
        {
            setAppBarState(STANDARD_APPBAR)
        }// end of else block
    }// end of function toggleToolBarState

    /**
     * Show the sheet for adding items
     */
    private fun toggleAddItemSheet()
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
    }// end of function toggleAddItemSheet

    //endregion

    //region DateTimePicker Functions

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
                        "changeValueByOne",
                        Boolean::class.javaPrimitiveType
                    )
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

    //endregion
}// end of class MainFragment