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

    private val tensNames = arrayOf(
        "", " ten", " twenty", " thirty", " forty",
        " fifty", " sixty", " seventy", " eighty", " ninety"
    )

    private val numNames = arrayOf(
        "", " one", " two", " three", " four", " five",
        " six", " seven", " eight", " nine", " ten", " eleven", " twelve", " thirteen",
        " fourteen", " fifteen", " sixteen", " seventeen", " eighteen", " nineteen"
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

    fun englishNumberToWordConvert(number: Long): String? {
        // 0 to 999 999 999 999
        if (number == 0L) {
            return "zero"
        }
        var snumber = number.toString()

        // pad with "0"
        val mask = "000000000000"
        val df = DecimalFormat(mask)
        snumber = df.format(number)


        val billions = snumber.substring(0, 3).toInt()
        val millions = snumber.substring(3, 6).toInt()
        val hundredThousands = snumber.substring(6, 9).toInt()
        val thousands = snumber.substring(9, 12).toInt()
        val tradBillions: String
        tradBillions = when (billions) {
            0 -> ""
            1 -> DigitConverter.convertLessThanOneThousand(billions) + " billion "
            else -> DigitConverter.convertLessThanOneThousand(billions) + " billion "
        }
        var result = tradBillions
        val tradMillions: String
        tradMillions = when (millions) {
            0 -> ""
            1 -> DigitConverter.convertLessThanOneThousand(millions) + " million "
            else -> DigitConverter.convertLessThanOneThousand(millions) + " million "
        }
        result += tradMillions
        val tradHundredThousands: String
        tradHundredThousands = when (hundredThousands) {
            0 -> ""
            1 -> "one thousand "
            else -> convertLessThanOneThousand(hundredThousands) + " thousand "
        }
        result += tradHundredThousands
        val tradThousand: String? = convertLessThanOneThousand(thousands)
        result += tradThousand

        // remove extra spaces!
        return result.replace("^\\s+".toRegex(), "").replace("\\b\\s{2,}\\b".toRegex(), " ")
    }

    private fun convertLessThanOneThousand(number: Int): String? {
        var number = number
        var soFar: String
        if (number % 100 < 20) {
            soFar = DigitConverter.numNames.get(number % 100)
            number /= 100
        } else {
            soFar = DigitConverter.numNames.get(number % 10)
            number /= 10
            soFar = DigitConverter.tensNames.get(number % 10).toString() + soFar
            number /= 10
        }
        return if (number == 0) soFar else DigitConverter.numNames.get(number).toString() + " hundred" + soFar
    }

    fun isValidTimeRange(currentDate: String, timeRangeStart24H: String?, timeRangeEnd24H: String?, pattern: String = "yyyy-MM-dd HH:mm:ss"): Boolean {
        timeRangeStart24H ?: return false
        timeRangeEnd24H ?: return false
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.US)
            val currentTimeStamp: Date = Date()
            val liveStartStamp: Date? = sdf.parse("$currentDate $timeRangeStart24H")
            val liveEndStamp: Date? = sdf.parse("$currentDate $timeRangeEnd24H")
            !(currentTimeStamp.before(liveStartStamp) || currentTimeStamp.after(liveEndStamp))
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}