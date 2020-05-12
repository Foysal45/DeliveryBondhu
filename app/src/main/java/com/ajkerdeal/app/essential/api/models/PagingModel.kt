package com.ajkerdeal.app.essential.api.models

data class PagingModel<T> (
    var isInitLoad: Boolean = false,
    var totalCount: Int = 0,
    var dataList: T
)
