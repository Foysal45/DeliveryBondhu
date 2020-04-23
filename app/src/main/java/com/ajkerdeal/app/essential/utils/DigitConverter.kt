package com.ajkerdeal.app.essential.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object DigitConverter {

    val banglaMonth = arrayOf(
        "জানুয়ারী",
        "ফেব্রুয়ারী",
        "মার্চ",
        "এপ্রিল",
        "মে",
        "জুন",
        "জুলাই",
        "আগস্ট",
        "সেপ্টেম্বর",
        "অক্টোবর",
        "নভেম্বর",
        "ডিসেম্বর"
    )
    private var decimalFormat: DecimalFormat? = null

    init {
        decimalFormat = DecimalFormat("#,##,##0.00")
        //decimalFormat?.isGroupingUsed = true
        //decimalFormat?.groupingSize = 3
    }

    fun toBanglaDigit(digits: Any?, isComma: Boolean = false): String {

        if (digits is String) {
            return engCahrReplacer(digits)
        } else if (digits is Int) {
            return if (isComma) {
                engCahrReplacer(formatNumber(digits))
            } else {
                engCahrReplacer(digits.toString())
            }
        } else if (digits is Double)
            return if (isComma) {
                engCahrReplacer(formatNumber(digits))
            } else {
                engCahrReplacer(digits.toString())
            }
        else {
            return (digits as? String).toString()
        }
    }

    fun toBanglaDateWithTime(banglaDate: String?, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {

        if (banglaDate == null) {
            return ""
        }
        val dateFormatter = SimpleDateFormat(pattern, Locale.US)
        try {
            val date = dateFormatter.parse(banglaDate)
            val calendar = Calendar.getInstance()
            if (date != null) {
                calendar.time = date
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                //val second = calendar.get(Calendar.SECOND)

                return engCahrReplacer(day.toString()) + " " + banglaMonth[month] + ", " + engCahrReplacer(year.toString()) + " " + engCahrReplacer("${timePhase(hour)} ${hourIn12(hour)}:${minute.toString().padStart(2,'0')}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return banglaDate
    }

    fun toBanglaDate(banglaDate: String?, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {

        if (banglaDate == null) {
            return ""
        }
        val dateFormatter = SimpleDateFormat(pattern, Locale.US)
        try {
            val date = dateFormatter.parse(banglaDate)
            val calendar = Calendar.getInstance()
            if (date != null) {
                calendar.time = date
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                //val hour = calendar.get(Calendar.HOUR_OF_DAY)
                //val minute = calendar.get(Calendar.MINUTE)
                //val second = calendar.get(Calendar.SECOND)

                return engCahrReplacer(day.toString()) + " " + banglaMonth[month] + ", " + engCahrReplacer(year.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return banglaDate
    }


    fun toBanglaDate(stamp: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {

        if (stamp == 0L) {
            return ""
        }
        val dateFormatter = SimpleDateFormat(pattern, Locale.US)
        try {

            val date = Date(stamp)
            val calendar = Calendar.getInstance()
            if (date != null) {
                calendar.time = date
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                //val second = calendar.get(Calendar.SECOND)

                return engCahrReplacer(day.toString()) + " " + banglaMonth[month] + ", " + engCahrReplacer(year.toString()) + " " + engCahrReplacer("${timePhase(hour)} ${hourIn12(hour)}:${minute.toString().padStart(2,'0')}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun toBanglaTime(banglaDate: String?, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {

        if (banglaDate == null) {
            return ""
        }
        val dateFormatter = SimpleDateFormat(pattern, Locale.US)
        try {

            val date = dateFormatter.parse(banglaDate)
            val calendar = Calendar.getInstance()
            if (date != null) {
                calendar.time = date
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                return engCahrReplacer("${timePhase(hour)} ${hourIn12(hour)}:${minute.toString().padStart(2,'0')}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return banglaDate
    }

    fun toBanglaDate(dayOfMonth: Int, monthOfYear: Int, year: Int): String {

        try {
            //return engCahrReplacer(dayOfMonth.toString()) + " " + banglaMonth[monthOfYear] + ", " + engCahrReplacer(year.toString())
            return "${engCahrReplacer(dayOfMonth.toString())} ${banglaMonth[monthOfYear]}, ${engCahrReplacer(
                year.toString()
            )}"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun engCahrReplacer(string: String): String {
        return string.replace('0', '০')
            .replace('1', '১')
            .replace('2', '২')
            .replace('3', '৩')
            .replace('4', '৪')
            .replace('5', '৫')
            .replace('6', '৬')
            .replace('7', '৭')
            .replace('8', '৮')
            .replace('9', '৯')
    }

    private fun formatNumber(digits: Any): String {

        return decimalFormat?.format(digits) ?: digits.toString()
    }

    fun timePhase(hourOfDay: Int) = when (hourOfDay) {
        in 0..4 -> "রাত"
        in 5..11 -> "সকাল"
        in 12..15 -> "দুপুর"
        in 16..17 -> "বিকাল"
        in 18..20 -> "সন্ধ্যা"
        in 21..24 -> "রাত"
        else -> {
            ""
        }
    }

    fun hourIn12(hourOfDay: Int): Int = if(hourOfDay == 12 || hourOfDay == 0) 12 else hourOfDay % 12

}