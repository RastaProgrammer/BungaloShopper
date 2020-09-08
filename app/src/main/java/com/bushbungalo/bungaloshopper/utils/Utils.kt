package com.bushbungalo.bungaloshopper.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.TextView
import com.bushbungalo.bungaloshopper.R
import com.bushbungalo.bungaloshopper.view.BungaloShopperApp
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.*


@Suppress("UNUSED")
object Utils
{
    // Internal field
    private const val regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"

    // Open fields
    val formatterWithTimeZone = SimpleDateFormat(
        "E, MMM dd, yyyy h:mm a zzz", Locale.ENGLISH
    )

    val formatterNoTimeZone = SimpleDateFormat(
        "E, MMM dd, yyyy h:mm a",
        Locale.ENGLISH
    )

    val shortFormatterDate = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.ENGLISH
    )

    val shortFormatterWeekDay = SimpleDateFormat(
        "E, MMM dd",
        Locale.ENGLISH
    )

    val longDayAndMonthDate = SimpleDateFormat(
        "EEEE, MMMM dd, yyyy", Locale.ENGLISH
    )

    // User rows that have already been animated
    var userAnimatedUserNames: ArrayList<String> = ArrayList()

    // Tasks that were previously animated after and update
    // if there is a new update to the row, it will be removed
    // and reinserted after indicating that an update occurred.
    var taskAnimatedIds: ArrayList<String> = ArrayList()

    // Aliases for the time of day
    enum class TimeOfDay{
        MORNING,
        AFTERNOON,
        EVENING,
        NIGHT
    }

    const val hiddenColour: String = "#FFFFFF"
    const val visibleColour: String = "#808080"

    /**
     * Converts a color code into a transparent color code
     *
     * @param colorCode An Int value representing a color code
     * @param opacityLevel An Int value representing the level of opacity
     * @return An Int value representing a transparent color code
     * @author Hemant Chand
     * <br></br>
     * [&#39;https://github.com/duggu-hcd/TransparentColorCode&#39;]['https://github.com/duggu-hcd/TransparentColorCode']
     */
    fun addOpacity(colorCode: Int, opacityLevel: Int): Int
    {
        // convert color code into hex string and remove starting 2 digits
        val color = Integer.toHexString(colorCode).toUpperCase(
            Locale.ROOT).substring(2)

        if (opacityLevel < 100)
        {
            val hexString = Integer.toHexString((255 * opacityLevel / 100f).roundToInt())
            val conversion = (if (hexString.length < 2) "0" else "") + hexString

            return if (color.trim { it <= ' ' }.length == 6)
            {
                Color.parseColor("#$conversion$color")
            } // end of if block
            else
            {
                Log.i("Utils", "The color is already transparent!")
                Color.parseColor(conversion + color)
            } // end of else block
        } // end of if block

        // if color is empty or any other problem occurs then we return default color;
        return Color.parseColor(
            "#" + Integer.toHexString(
               BungaloShopperApp.shopperContext.resources.getColor(
                   R.color.colorAccent, null
               )).toUpperCase(Locale.ROOT).substring(2)
        )
    } // end of method addOpacity

    /**
     * Determines the difference between two dates in days
     *
     * @param d1    The first date
     * @param d2    The second date
     * @return A value representing the number of days between the two dates
     */
    fun daysBetweenDates(d1: Date, d2: Date) : Long
    {
        return abs((d2.time - d1.time) / (60 * 60 * 24 * 1000))
    }// end of function daysBetweenDates

    /**
     * Determine whether or not a database file exists locally
     *
     * @param  context  A valid application context
     * @param  dbName   The name of the SQLite database
     * @return  True/False dependent on the outcome of the check
     */
    fun doesDatabaseExist(context: Context, dbName: String): Boolean
    {
        val dbFile: File = context.getDatabasePath(dbName)
        return dbFile.exists()
    }

    /**
     * Converts from device pixels to screen pixels
     *
     * @param dps   An integer representing the device pixel count
     * @param context   A context associated with the application
     * @return A computed value after the calculation is completed
     */
    fun devicePixelsToPixels(dps: Int, context: Context) : Int
    {
        val r: Resources = context.resources

        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dps.toFloat(), r.displayMetrics
        ).toInt()
    }// end of function devicePixelsToPixels

    /**
     * Converts from screen pixels to device pixels
     *
     * @param px   An integer representing the screen pixel count
     * @param context   A context associated with the application
     * @return A computed value after the calculation is completed
     */
    fun pixelsToDevice(px: Int, context: Context) : Int
    {
        val r: Resources = context.resources

        return ceil(px / r.displayMetrics.density).toInt()
    }// end of function pixelsToDevice

    /**
     * Format a string representation of a telephone number
     *
     * @param phoneNumber   The telephone number to be formatted
     * @param local         Whether the number is local or international
     * @return  A formatted version of the telephone number
     */
    fun formatTelephoneNumber(phoneNumber: String, local: Boolean): String
    {
        if(phoneNumber.length < 10)
        {
            // Incorrect number length so return it
            // to th user
            return phoneNumber
        }// end of if block
        else
        {
            val phoneTransformed: StringBuilder = StringBuilder()

            if(!local)
            {
                phoneTransformed.append("+1 ")
            } // end of if block

            for(i in phoneNumber.indices)
            {
                when (i)
                {
                    0 -> {
                        phoneTransformed.append("(").append(phoneNumber[i])
                    }

                    2 -> {
                        phoneTransformed.append(phoneNumber[i]).append(")")
                    }

                    3 -> {
                        phoneTransformed.append(" ").append(phoneNumber[i])
                    }

                    6 -> {
                        phoneTransformed.append("-").append(phoneNumber[i])
                    }
                    else->{
                        phoneTransformed.append(phoneNumber[i]) 
                    }
                }// end of when block
            }// end of for loop

            return phoneTransformed.toString()
        }// end of else block
    }// end of method formatTelephoneNumber

    /**
     * Loads a custom font to all applicable child Objects of a ViewGroup
     * @param view The ViewGroup containing children with font attributes
     */
    fun loadCustomFont(view: ViewGroup)
    {
        for (i in 0 until view.childCount)
        {
            val v: View = view.getChildAt(i)

            if (v is TextView)
            {

                if(v.typeface.isBold)
                {
                    v.typeface = BungaloShopperApp.currentFontBold
                }// end of if block
                else
                {
                    v.typeface = BungaloShopperApp.currentFontNormal
                }// end of else block
            } // end of if block
            else if (v is ViewGroup)
            {
                loadCustomFont(v)
            } // end of else if block
        } // end of for loop
    } // end of method loadCustomFont

    /**
     * Ensure that an email address is in a valid format in
     * accordance with set standards for email addresses
     *
     * @param address   The email address to be validated
     * @return A @see Boolean value indicating if the format is correct
     */
    fun validEmailAddress(address: String): Boolean
    {
        //initialize the Pattern object
        val pattern: Pattern = Pattern.compile(regex)
        val matcher: Matcher = pattern.matcher(address)

        return matcher.matches()
    }// end of method validEmailAddress

    /**
     * Get the current time of date
     *
     * @return An enum value of MORNING, NOON, EVENING, and NIGHT
     */
    fun getTimeOfDay(): TimeOfDay
    {
        val now = Calendar.getInstance()
        val checkCalendar = Calendar.getInstance()
        val checkOtherCalendar = Calendar.getInstance()
        val timeZone = SimpleDateFormat(
            "zzz",
            Locale.ENGLISH
        ).format(now.time)
        val isDaylightSavings = timeZone.substring(1) == "DT"

        if (isDaylightSavings)
        {
            checkCalendar.set(Calendar.HOUR_OF_DAY, 17)
            checkCalendar.set(Calendar.MINUTE, 59)
            checkCalendar.set(Calendar.SECOND, 59)

            checkOtherCalendar.set(Calendar.HOUR_OF_DAY, 20)
            checkOtherCalendar.set(Calendar.MINUTE, 59)
            checkOtherCalendar.set(Calendar.SECOND, 59)

            if (now.time.after(checkCalendar.time) &&
                now.time.before(checkOtherCalendar.time))
            {
                return TimeOfDay.EVENING
            } // end of if block

            checkCalendar.set(Calendar.HOUR_OF_DAY, 20)
            checkCalendar.set(Calendar.MINUTE, 59)
            checkCalendar.set(Calendar.SECOND, 59)

            if (now.time.after(checkCalendar.time))
            {
                // anytime after 9:00 should be considered a night time
                return TimeOfDay.NIGHT
            } // end of else if block

            checkCalendar.set(Calendar.HOUR_OF_DAY, 0)
            checkCalendar.set(Calendar.MINUTE, 0)
            checkCalendar.set(Calendar.SECOND, 0)

            checkOtherCalendar.set(Calendar.HOUR_OF_DAY, 11)
            checkOtherCalendar.set(Calendar.MINUTE, 59)
            checkOtherCalendar.set(Calendar.SECOND, 59)

            if (now.time == checkCalendar.time ||
                now.time.after(checkCalendar.time) &&
                now.time.before(checkOtherCalendar.time))
            {
                return TimeOfDay.MORNING
            } // end of else if block
            else
            {
                // if the time is'nt after 6:00 PM we will assume that it is still afternoon
                TimeOfDay.AFTERNOON
            } // end of else block
        } // end of if block

        // Standard time
        checkCalendar.set(Calendar.HOUR_OF_DAY, 16)
        checkCalendar.set(Calendar.MINUTE, 59)
        checkCalendar.set(Calendar.SECOND, 59)

        checkOtherCalendar.set(Calendar.HOUR_OF_DAY, 20)
        checkOtherCalendar.set(Calendar.MINUTE, 59)
        checkOtherCalendar.set(Calendar.SECOND, 59)

        if (now.time.after(checkCalendar.time) &&
            now.time.before(checkOtherCalendar.time))
        {
            return TimeOfDay.EVENING
        } // end of if block

        checkCalendar.set(Calendar.HOUR_OF_DAY, 20)
        checkCalendar.set(Calendar.MINUTE, 59)
        checkCalendar.set(Calendar.SECOND, 59)

        checkOtherCalendar.set(Calendar.HOUR_OF_DAY, 20)
        checkOtherCalendar.set(Calendar.MINUTE, 59)
        checkOtherCalendar.set(Calendar.SECOND, 59)

        if (now.time.after(checkCalendar.time))
        {
            // anytime after 9:00 should be considered a night time
            return TimeOfDay.NIGHT
        } // end of if block

        checkCalendar.set(Calendar.HOUR_OF_DAY, 0)
        checkCalendar.set(Calendar.MINUTE, 0)
        checkCalendar.set(Calendar.SECOND, 0)

        checkOtherCalendar.set(Calendar.HOUR_OF_DAY, 11)
        checkOtherCalendar.set(Calendar.MINUTE, 59)
        checkOtherCalendar.set(Calendar.SECOND, 59)

        return if (now.time == checkCalendar.time ||
            now.time.after(checkCalendar.time) &&
            now.time.before(checkOtherCalendar.time))
        {
            TimeOfDay.MORNING
        } // end of else if block
        else
        {
            // if the time is'nt after 5:00 PM we will assume that it is still afternoon
            TimeOfDay.AFTERNOON
        } // end of else block
    } // end of method getTimeOfDay

    /**
     * Converts a sequence to alphabetic characters to sentence case.
     *
     * @param text The @see String value to be converted.
     * @return     A @see String representation of text in a sentence format.
     */
    fun toProperCase(text: String): String
    {
        val sep = arrayOf(" ", "-", "/", "'")
        val cycle = text.length
        val sequence = StringBuilder(text.toLowerCase(Locale.ROOT))

        for (i in 0..cycle)
        {
            if (i == 0 && Character.isAlphabetic(sequence[i].toInt()))
            {
                sequence.replace(
                    sequence.indexOf(sequence[i].toString()),
                    sequence.indexOf(sequence[i].toString()) + 1,
                    Character.toUpperCase(sequence[i]).toString()
                )
            } // end of if block
            else if (i < cycle && Character.isAlphabetic(sequence[i].toInt()) &&
                sequence[i - 1] == 'c' && sequence[i - 2] == 'M')
            {
                sequence.replace(
                    sequence.indexOf(sequence[i].toString()),
                    sequence.indexOf(sequence[i].toString()) + 1,
                    sequence[i].toString().toUpperCase(Locale.ROOT)
                )
            } // end of else if block
            else if (i < cycle && Character.isAlphabetic(sequence[i].toInt()) &&
                (sequence[i - 1].toString() == sep[0] || sequence[i - 1].toString() == sep[1]))
            {
                sequence.replace(
                    i, i + 1, sequence[i].toString().toUpperCase(Locale.ROOT)
                )
            } // end of else if block
        } // end of for loop

        return sequence.toString()
    } // end of function toProperCase

    fun distanceBetweenTwoPointsInFeet(
        lat_a: Double, lng_a: Double, lat_b: Double,
        lng_b: Double
    ): Double
    {
        val pk = (180f / Math.PI).toFloat()
        val a1 = lat_a / pk
        val a2 = lng_a / pk
        val b1 = lat_b / pk
        val b2 = lng_b / pk
        val t1 = cos(a1) * cos(a2) * cos(b1) * cos(b2)
        val t2 = cos(a1) * sin(a2) * cos(b1) * sin(b2)
        val t3 = sin(a1) * sin(b1)
        val tt = acos(t1 + t2 + t3)
        val meters = 6366000 * tt

        return meters * 3.28084 // The distance in feet
    }// end of function distanceBetweenTwoPointsInFeet

    /** Custom zoom in animation
    *
    * @param zoomView The view to be zoomed in.
    */
    fun zoomInView(zoomView: View)
    {
        val scaleAnim: Animation = ScaleAnimation(
            0.85f, 1.0f,  // Start and end values for the X axis scaling
            0.85f, 1.0f,  // Start and end values for the Y axis scaling
            Animation.RELATIVE_TO_SELF, 0.5f,  // Pivot point of X scaling
            Animation.RELATIVE_TO_SELF, 0.5f
        ) // Pivot point of Y scaling
        scaleAnim.fillAfter = true // Needed to keep the result of the animation
        val alphaAnim: Animation = AlphaAnimation(0.0f, 1.0f)
        alphaAnim.fillAfter = true // Needed to keep the result of the animation
        val animSet = AnimationSet(true)
        animSet.addAnimation(scaleAnim)
        animSet.addAnimation(alphaAnim)
        animSet.duration = 250
        zoomView.startAnimation(animSet)
        scaleAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(arg0: Animation) {}
            override fun onAnimationRepeat(arg0: Animation) {}
            override fun onAnimationEnd(arg0: Animation) {
                // quickly zoom back out by 4%
                zoomView.animate().scaleX(0.96f).scaleY(0.96f).duration = 100
            }
        })
    } // end of method zoomInView
}// end of class Utils
