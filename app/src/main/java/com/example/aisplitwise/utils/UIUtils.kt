package com.example.aisplitwise.utils


fun String?.ifNullOrEmpty(defaultValue: () -> String): String {
    return if (this.isNullOrEmpty()) {
        defaultValue()
    } else {
        this
    }
}