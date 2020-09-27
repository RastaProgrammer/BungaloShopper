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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bushbungalo.bungaloshopper.R
import com.bushbungalo.bungaloshopper.databinding.MainFragmentBinding
import com.bushbungalo.bungaloshopper.model.ShoppingListItemEntity
import com.bushbungalo.bungaloshopper.ui.CategoriesAdapter
import com.bushbungalo.bungaloshopper.ui.DatesAdapter
import com.bushbungalo.bungaloshopper.utils.*
import com.bushbungalo.bungaloshopper.utils.Utils.loadCustomFont
import com.bushbungalo.bungaloshopper.view.BungaloShopperApp.Companion.mShoppingListViewModel
import com.bushbungalo.bungaloshopper.view.BungaloShopperApp.Companion.mTotalListViewModel
import kotlinx.android.synthetic.main.main_fragment.view.*
import kotlinx.android.synthetic.main.main_fragment_search_bar.view.*
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class MainFragment : Fragment(), DatesAdapter.ListItemListener
{
    //region Declarations
    private lateinit var binding: MainFragmentBinding
    private lateinit var listener: ShoppingListView

    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private lateinit var mAdapter: DatesAdapter

    private var mAppBarState = 0
    private lateinit var mCategoryAdapter: ArrayAdapter<String>
    private var mPos = 0

    companion object
    {
        // App Bar
        private const val STANDARD_APPBAR: Int = 0
        private const val SEARCH_APPBAR = 1

        // Add Sheet
        private const val APP_SHEET_DOWN_POSITION = 0
        private const val APP_SHEET_UP_POSITION = 1
    }// end of companion object

    //endregion

    //region Fragment Interface Implementations

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = MainFragmentBinding.inflate(inflater, container, false)
        val categories = resources.getStringArray(R.array.categories)
        mCategoryAdapter = CategoriesAdapter(binding.root.context, categories.toMutableList())
        binding.addItemLayout.itemCategorySpn.adapter = mCategoryAdapter

        loadCustomFont(binding.root)

        initRecyclerView()

        initObserver(savedInstanceState)

        setViewListeners()
        
        binding.mainToolBarLayout.billTotalImv.visibility = View.GONE
        binding.mainToolBarLayout.searchToolbarTitleTxv.text = resources.getString(R.string.default_fragment_name)

        requireActivity().onBackPressedDispatcher.addCallback (
            viewLifecycleOwner,
            object : OnBackPressedCallback(true)
            {
                override fun handleOnBackPressed()
                {
                    if(mAppBarState != STANDARD_APPBAR)
                    {
                        toggleToolBarState()
                        val im: InputMethodManager =
                            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        val view = view

                        try
                        {
                            im.hideSoftInputFromWindow(view!!.windowToken, 0) // make keyboard hide
                        }// end of try block
                        catch (e: NullPointerException) {}

                        binding.searchToolBarLayout.searchToolbarSearchEdt.setText("")
                        mAdapter.filterItems("")
                    }// end of if block
                    else
                    {
                        listener.exitApp()
                    }// end of else block
                }
            }
        )
        return binding.root
    }// end of function onCreate

    override fun onResume()
    {
        super.onResume()
        setAppBarState(STANDARD_APPBAR)
    }// end of function onResume

    override fun onStart()
    {
        super.onStart()

        listener = activity as MainActivity
        binding.addItemLayout.root.animate()
            .translationYBy(0f)
            .translationY(1000f)
            .duration = 0
    }// end of function onStart

    override fun onSaveInstanceState(outState: Bundle)
    {
        if(::binding.isInitialized)
        {
            with(binding.searchToolBarLayout.searchToolbarSearchEdt)
            {
                outState.putString(SEARCH_QUERY, text.toString())
                outState.putInt(SEARCH_CURSOR_POSITION, selectionStart)
            }// end of with block

            outState.putInt(APP_BAR_STATE, mAppBarState)

            with(binding.addItemLayout)
            {
                outState.putString(SHOPPING_DATE, goingShoppingOnTxv.text.toString())
                outState.putString(PRODUCT_NAME, itemNameTxv.text.toString())
                outState.putString(PRODUCT_CATEGORY, itemCategorySpn.selectedItem.toString())
                outState.putString(PRODUCT_QUANTITY, itemQuantityTxv.text.toString())
                outState.putString(PRODUCT_UNIT_PRICE, itemUnitPriceTxv.text.toString())
            }// end of when block

            outState.putInt(ADD_SHEET_POSITION, mPos)
        }// end of if block

        super.onSaveInstanceState(outState)
    }// end of function onSaveInstanceState

    //endregion

    //region Setup

    /**
     * Initialize the observers
     */
    private fun initObserver(savedInstanceState: Bundle?)
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

                if (noDuplicateDates.size == 0)
                {
                    binding.noListsLayout.visibility = View.VISIBLE
                    binding.dailyListsBodyScroller.visibility = View.INVISIBLE
                }// end of if block
                else
                {
                    binding.noListsLayout.visibility = View.INVISIBLE
                    binding.dailyListsBodyScroller.visibility = View.VISIBLE
                }// end of else block

                mAdapter = DatesAdapter(noDuplicateDates, this@MainFragment)
                binding.storedListsRcv.adapter = mAdapter
                mAdapter.notifyDataSetChanged()

                if(savedInstanceState != null)
                {
                    val previousAppBarState = savedInstanceState.getInt(APP_BAR_STATE)
                    val previousSheetPosition = savedInstanceState.getInt(ADD_SHEET_POSITION)

                    val queryString: String? = savedInstanceState.getString(SEARCH_QUERY)
                    val cursorPosition: Int = savedInstanceState.getInt(SEARCH_CURSOR_POSITION)

                    if(previousAppBarState == SEARCH_APPBAR)
                    {
                        toggleToolBarState()

                        if(queryString != null)
                        {
                            binding.searchToolBarLayout.root.selected_shopping_search_app_bar.visibility = View.VISIBLE
                            binding.mainToolBarLayout.root.main_tool_bar_layout.visibility = View.GONE
                            binding.searchToolBarLayout.searchToolbarSearchEdt.setText(
                                queryString)
                            binding.searchToolBarLayout.searchToolbarSearchEdt.setSelection(cursorPosition)
                        }// end of if block
                    }// end of else block

                    if(previousSheetPosition == APP_SHEET_UP_POSITION)
                    {
                        binding.addItemLayout.goingShoppingOnTxv.text = savedInstanceState.getString(SHOPPING_DATE)
                        binding.addItemLayout.itemNameTxv.setText(savedInstanceState.getString(PRODUCT_NAME))
                        binding.addItemLayout.itemCategorySpn.setSelection(mCategoryAdapter.getPosition(
                            savedInstanceState.getString(PRODUCT_CATEGORY)))
                        binding.addItemLayout.itemQuantityTxv.setText(savedInstanceState.getString(PRODUCT_QUANTITY))
                        binding.addItemLayout.itemUnitPriceTxv.setText(savedInstanceState.getString(PRODUCT_UNIT_PRICE))
                        binding.addItemLayout.itemNameTxv.setSelection(binding.addItemLayout.itemNameTxv.text.length)

                        toggleAddItemSheet()
                    }// end of if block
                }// end of if block
            })
    }// end of function initObserver

    /**
     * Initialize the recycler view
     */
    private fun initRecyclerView()
    {
        mLinearLayoutManager = LinearLayoutManager(context)

        with(binding.storedListsRcv)
        {
            setHasFixedSize(true)
            layoutManager = mLinearLayoutManager
        }
    }// end of function initRecyclerView

    /**
     * Setup the listener for the view
     */
    private fun setViewListeners()
    {
        // Main Toolbar
        binding.mainToolBarLayout.searchShoppingListImv.setOnClickListener {
            toggleToolBarState()
            binding.searchToolBarLayout.searchToolbarSearchEdt.requestFocus()
        }

        binding.mainToolBarLayout.addToShoppingListImv.setOnClickListener {
            toggleAddItemSheet()
        }// end of imvAdd.setOnClickListener block

        // Search Toolbar
        binding.searchToolBarLayout.backToNormalImv.setOnClickListener {
            toggleToolBarState()
        }

        binding.searchToolBarLayout.searchToolbarSearchEdt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (binding.searchToolBarLayout.searchToolbarSearchEdt.text.isNotEmpty()) {
                    binding.searchToolBarLayout.clearFilterImv.visibility = View.VISIBLE
                }// end of if block
                else {
                    binding.searchToolBarLayout.clearFilterImv.visibility = View.INVISIBLE
                }// end of if block

                if (::mAdapter.isInitialized) {
                    mAdapter.filterItems(binding.searchToolBarLayout.searchToolbarSearchEdt.text.toString())
                }
            }
        })

        binding.searchToolBarLayout.clearFilterImv.setOnClickListener {
            binding.searchToolBarLayout.searchToolbarSearchEdt.setText("")
        }

        // Add Item Sheet
        binding.addItemLayout.addItemBtn.setOnClickListener {
            val itemId = "bs${SimpleDateFormat("HHmmssSSS", Locale.ENGLISH).format(Date())}"
            val shoppingDate = Utils.longDayAndMonthDate.parse(binding.addItemLayout.goingShoppingOnTxv.text.toString())

            if(binding.addItemLayout.itemNameTxv.text.isNotEmpty())
            {
                val newItem = ShoppingListItemEntity(
                    0,
                    itemId,
                    Utils.toProperCase(binding.addItemLayout.itemNameTxv.text.toString()),
                    binding.addItemLayout.itemCategorySpn.selectedItem.toString(),
                    if (binding.addItemLayout.itemQuantityTxv.text.toString().isNotEmpty()) binding.addItemLayout.itemQuantityTxv.text.toString()
                        .toInt() else 1,
                    if (binding.addItemLayout.itemUnitPriceTxv.text.toString().isNotEmpty()) binding.addItemLayout.itemUnitPriceTxv.text.toString()
                        .toDouble() else 0.0,
                    shoppingDate!!.time
                )

                if (binding.addItemLayout.addItemBtn.text == getString(R.string.update_item)) {
                    createShoppingList(newItem)
                }// end of if block
                else {
                    createShoppingList(newItem)
                }// end of else block

                toggleAddItemSheet()
            }// end of if block
            else
            {
                Toast.makeText(binding.root.context, "Enter the item to be added!",
                    Toast.LENGTH_SHORT).show()
                binding.addItemLayout.addItemBtn.requestFocus()
            }// end of else block
        }

        binding.addItemLayout.cancelBtn.setOnClickListener {
            toggleAddItemSheet()
            binding.addItemLayout.itemNameTxv.setText("")
            binding.addItemLayout.itemCategorySpn.setSelection(0)
            binding.addItemLayout.itemNameTxv.setText("")
            binding.addItemLayout.itemUnitPriceTxv.setText("")
        }

        binding.addItemLayout.goingShoppingOnTxv.setOnClickListener {
            showDateTimeDialog(binding.root.context)
        }

        binding.addItemLayout.itemNameTxv.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if(!hasFocus)
                {
                    if(binding.addItemLayout.itemNameTxv.text.isNotEmpty())
                    {
                        binding.addItemLayout.itemNameTxv.setText(
                            Utils.toProperCase(binding.addItemLayout.itemNameTxv.text.toString()))
                    }// end of if block
                }// end of if block
            }

        binding.addItemLayout.itemUnitPriceTxv.onFocusChangeListener =
            View.OnFocusChangeListener { _, hasFocus ->
                if(!hasFocus)
                {
                    if(binding.addItemLayout.itemUnitPriceTxv.text.isNotEmpty() &&
                        binding.addItemLayout.itemUnitPriceTxv.text.toString().length > 1) {
                        val unitPrice = String.format("%.2f", binding.addItemLayout.itemUnitPriceTxv.text.toString().toFloat())

                        binding.addItemLayout.itemUnitPriceTxv.setText(unitPrice)
                    }// end of if block
                }// end of if block
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

        binding.addItemLayout.itemNameTxv.setText("")
        binding.addItemLayout.itemCategorySpn.setSelection(0)
        binding.addItemLayout.itemNameTxv.setText("")
        binding.addItemLayout.itemUnitPriceTxv.setText("")

        // Hide the sheet
        toggleAddItemSheet()
    }// end of function createShoppingList

    //endregion

    //region View Toggles

    /**
     * Show the sheet for adding items
     */
    private fun toggleAddItemSheet()
    {
        val h =  binding.addItemLayout.root.measuredHeight

        if(mPos == APP_SHEET_UP_POSITION)
        {
            binding.addItemLayout.root.animate()
                .translationYBy(0f)
                .translationY(h.toFloat())
                .setDuration(500)
                .withEndAction {
                    mPos = APP_SHEET_DOWN_POSITION
                    binding.addItemLayout.root.visibility = View.INVISIBLE
                }
        }// end of if block
        else
        {
            binding.addItemLayout.root.visibility = View.VISIBLE

            binding.addItemLayout.root.animate()
                .translationYBy(h.toFloat())
                .translationY(0f)
                .setDuration(500)
                .withEndAction {
                    mPos = APP_SHEET_UP_POSITION
                }
        }// end of else block

        binding.addItemLayout.goingShoppingOnTxv.text = Utils.longDayAndMonthDate.format(Date().time)
        binding.addItemLayout.itemNameTxv.requestFocus()
        binding.addItemLayout.addItemBtn.text = getString(R.string.add_item)
    }// end of function toggleAddItemSheet

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
            binding.searchToolBarLayout.root.animate()
                .alpha(1f)
                .setDuration(250)
                .setListener(object : AnimatorListenerAdapter()
                {
                    override fun onAnimationEnd(animation: Animator)
                    {
                        binding.searchToolBarLayout.root.selected_shopping_search_app_bar.visibility = View.GONE
                        binding.mainToolBarLayout.root.main_tool_bar_layout.visibility = View.VISIBLE
                    }
                })

            val view = view
            val im: InputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            try
            {
                im.hideSoftInputFromWindow(view!!.windowToken, 0) // make keyboard hide
            }// end of try block
            catch (e: NullPointerException) { }
        }// end of if block
        else if (mAppBarState == SEARCH_APPBAR)
        {
            binding.searchToolBarLayout.root.search_tool_bar_layout.animate()
                .alpha(1f)
                .setDuration(250)
                .setListener(object : AnimatorListenerAdapter()
                {
                    override fun onAnimationEnd(animation: Animator)
                    {
                        binding.mainToolBarLayout.root.main_tool_bar_layout.visibility = View.GONE
                        binding.searchToolBarLayout.root.selected_shopping_search_app_bar.visibility =
                            View.VISIBLE
                    }
                })

            val im: InputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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

    // endregion

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
            setFormatter { i ->
                String.format("%02d", i)
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
            if (from == 11 && to == 12)
            {
                if (mer == 0)
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
            else if (from == 12 && to == 11)
            {
                if (mer == 0)
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

        val dates = if (Utils.daysBetweenDates(
                Utils.shortFormatterDate.parse(lastDateOfPreviousMonth)!!, Date()) > 5)
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
        if (Arrays.stream(dates).anyMatch { t -> t == today })
        {
            todayIndex = dates.indexOf(today)
        }// end of if block

        val datesPicker: NumberPicker =
            dtPickerDialogView.findViewById(R.id.day_pick_txv)

        datesPicker.apply {
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
            val dd = Utils.formatterNoTimeZone.parse(dateSelected)

            binding.addItemLayout.goingShoppingOnTxv.text = Utils.longDayAndMonthDate.format(dd!!)
            dtDialog.dismiss()
        }

        val btnCancelDateTime: Button = dtPickerDialogView.findViewById(R.id.cancel_date_time_btn)

        btnCancelDateTime.setOnClickListener {
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

        if (mer == 0)
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

    //region Adapter Interface Implementations

    override fun onItemClick(date: Long)
    {
        val action = MainFragmentDirections.actionViewShoppingList(date)
        findNavController().navigate(action)
    }

    override fun deleteShoppingList(date: Long)
    {
        listener.deleteShoppingList(date)
    }

    //endregion
}// end of class MainFragment