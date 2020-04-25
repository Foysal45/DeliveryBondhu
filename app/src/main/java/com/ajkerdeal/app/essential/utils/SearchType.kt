package com.ajkerdeal.app.essential.utils

sealed class SearchType {
    object None: SearchType()
    object Product: SearchType()
    object Status: SearchType()
}