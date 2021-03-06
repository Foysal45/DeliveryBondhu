package com.ajkerdeal.app.essential.utils

sealed class SearchType {
    object None: SearchType()
    object Product: SearchType()
    object Order: SearchType()
    object Status: SearchType()
}