package com.crushtech.mycollegecgpa.utils

open class Events<out T>(
    private val content: T
) {
    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled() = if (hasBeenHandled) {
        null
    } else {
        hasBeenHandled = true
        content
    }

    //returns the content
    fun peekContent() = content
}