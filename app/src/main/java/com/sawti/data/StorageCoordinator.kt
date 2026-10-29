package com.sawti.data

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Shared by every repository and backup manager using the application's one Room instance. */
internal class StorageCoordinator {
    private val mutex = Mutex()
    private var retired = false

    suspend fun <T> access(action: suspend () -> T): T = mutex.withLock {
        check(!retired) { "أعد تشغيل التطبيق لإكمال الاسترجاع" }
        action()
    }

    suspend fun cleanupIfActive(action: suspend () -> Unit) = mutex.withLock {
        // An old repository must never delete files in a newly restored data set.
        if (!retired) action()
    }

    /** Called only while holding access, immediately before closing/swapping live Room. */
    fun retire() { retired = true }
}
