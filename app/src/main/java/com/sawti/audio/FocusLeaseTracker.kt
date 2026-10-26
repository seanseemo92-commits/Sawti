package com.sawti.audio

/** Pure ownership guard: late completion callbacks cannot abandon a newer audio session. */
internal class FocusLeaseTracker {
    private var serial = 0L
    private var active: Long? = null

    fun next(): Long {
        val token = ++serial
        active = token
        return token
    }

    fun owns(token: Long): Boolean = active == token

    fun clear(token: Long): Boolean {
        if (!owns(token)) return false
        active = null
        return true
    }
}
