package com.sawti.domain.model

/** Built-in categories are immutable; custom IDs live in their own reserved namespace. */
object CustomCategoryRules {
    fun canManage(id: String): Boolean = id.startsWith("custom_") && id.length in 12..70
    fun validPalette(value: Int): Boolean = value in 0..11
    fun validSortOrder(value: Int): Boolean = value in 0..10_000
    fun canDelete(id: String, itemCount: Int): Boolean = canManage(id) && itemCount == 0
}
