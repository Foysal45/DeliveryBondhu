package com.ajkerdeal.app.essential.api.models

data class PagingModel<T> (
    var isInitLoad: Boolean = false,
    var dataList: T
)
