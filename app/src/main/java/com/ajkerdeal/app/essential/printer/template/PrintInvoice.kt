package com.ajkerdeal.app.essential.printer.template

import android.content.Context
import android.text.TextUtils
import com.ajkerdeal.app.essential.api.models.print.PrintModel
import com.ajkerdeal.app.essential.printer.command.AlignmentType
import com.ajkerdeal.app.essential.printer.command.PrinterCommand
import com.ajkerdeal.app.essential.utils.DigitConverter
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class PrintInvoice(private val context: Context, private val model: PrintModel) {

    private val tag = "PrintInvoice"
    private val printerCommand: PrinterCommand = PrinterCommand.getInstance(context)
    private val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US)

    fun print() {

        val date = sdf.format(Date())
        val s1 = "আজকের ডিল ডট কম"
        val s2 = "ফোন:০১৮৪৪১৭২৩২৮,০১৮৪৪১৭২৩২৭"
        val s3 = "ইমেইল:fulfillment@ajkerdeal.com"
        val s4 = "ইনভয়েস পেপার"
        val s5 = "মার্চেন্ট-"
        val s6 = "বুকিং কোড -- পরিমান -- মূল্য"
        val s7 = "কালেক্টর: "
        val s8 = "ফোন:"
        val s9 = "--------------------------------"
        val collectorName = model.userName
        val collectorPhone = model.userPhone

        var merchantName: String? = model.merchantName
        if (merchantName != null && !TextUtils.isEmpty(merchantName) && merchantName.length > 20) {
            merchantName = merchantName.substring(0, 19) + ".."
        }
        merchantName = s5 + merchantName
        val merchantPhone = s8 + model.merchantPhone

        var sum = 0

        printerCommand.setLineGap(1)
        printerCommand.setBold(true)
        printerCommand.setAlignmentType(AlignmentType.CENTER)
        printerCommand.printText(s1)
        Timber.tag(tag).d(s1)
        printerCommand.printText(s2)
        Timber.tag(tag).d(s2)
        printerCommand.printText(s3)
        Timber.tag(tag).d(s3)
        printerCommand.printText(s4)
        Timber.tag(tag).d(s4)
        printerCommand.printText(date)
        Timber.tag(tag).d(date)
        printerCommand.printText(merchantName)
        Timber.tag(tag).d(merchantName)
        printerCommand.printText(merchantPhone)
        Timber.tag(tag).d(merchantPhone)
        printerCommand.printText(s9)
        Timber.tag(tag).d(s9)
        printerCommand.printText(s6)
        Timber.tag(tag).d(s6)
        printerCommand.printText(s9)
        Timber.tag(tag).d(s9)

        model.dataList.forEach { data ->
            sum += data.price
            val couponId: String = DigitConverter.toBanglaDigit(data.couponId)
            val quantity: String = DigitConverter.toBanglaDigit(data.quantity)
            val price: String = DigitConverter.toBanglaDigit(data.price)
            val row = couponId + " --- " + quantity + "টি --- " + price + "৳"
            printerCommand.printText(row)
            Timber.tag(tag).d(row)
        }

        printerCommand.printText(s9)
        Timber.tag(tag).d(s9)

        val amount: String = addPaddingToString(DigitConverter.toBanglaDigit(sum.toString()), 6, 1).toString() + "৳"
        val totalPrice = "সর্বমোট:                $amount"
        printerCommand.printText(totalPrice)
        Timber.tag(tag).d(totalPrice)

        printerCommand.printText(s9)
        Timber.tag(tag).d(s9)

        val kothayText = "কথায়: " + DigitConverter.englishNumberToWordConvert(sum.toLong()).toString() + " only."
        printerCommand.printText(kothayText)

        val collector = "$s7$collectorName($collectorPhone)"
        printerCommand.printText(collector)
        Timber.tag(tag).d(collector)
        printerCommand.setLineGap(4)

    }

    private fun addPaddingToString(inputString: String, chunkSize: Int, paddingDirection: Int): String? {
        if (chunkSize - inputString.length > 0) {
            val itemNameSpacing = String(CharArray(chunkSize - inputString.length)).replace('\u0000', ' ')
            if (paddingDirection == 0) { // right padding
                return inputString + itemNameSpacing
            } else if (paddingDirection == 1) { // left padding
                return itemNameSpacing + inputString
            }
        }
        return inputString
    }
}