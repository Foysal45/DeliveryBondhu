package com.ajkerdeal.app.essential.api.models.print

data class PrintModel (
    var userName: String = "",
    var userPhone: String = "",
    var merchantName: String? = "",
    var merchantPhone: String? = "",
    var dataList: List<PrintData> = listOf()
)