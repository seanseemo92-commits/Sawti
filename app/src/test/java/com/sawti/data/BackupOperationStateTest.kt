package com.sawti.data

import android.app.Application
import com.sawti.data.db.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
class BackupOperationStateTest {
    /** Deterministically complete the worker between a message clearer reading and writing.
     * A read/copy/set loses the terminal state; compare-and-set update must retry. */
    private fun interleavingClear(terminal: BackupOperationState): BackupOperationState {
        val context = RuntimeEnvironment.getApplication()
        val db = AppDatabase.build(context)
        try {
            val operations = BackupOperations(context, db)
            val field = BackupOperations::class.java.getDeclaredField("_state").apply { isAccessible = true }
            @Suppress("UNCHECKED_CAST")
            val real = field.get(operations) as MutableStateFlow<BackupOperationState>
            real.value = BackupOperationState(busy = true, restoring = true, message = "رسالة قديمة")
            val flow = object : MutableStateFlow<BackupOperationState> by real {
                var completed = false
                override var value: BackupOperationState
                    get() {
                        val prior = real.value
                        if (!completed) { completed = true; real.value = terminal }
                        return prior
                    }
                    set(value) { real.value = value }
            }
            field.set(operations, flow)
            operations.clearMessage()
            return operations.state.value
        } finally { db.close() }
    }
    @Test fun clearingMessageCannotEraseSuccessfulRestoreRestartGate() {
        val terminal = BackupOperationState(restartRequired = true, restoreSucceeded = true)
        assertEquals(terminal, interleavingClear(terminal))
    }
    @Test fun clearingMessageCannotResurrectBusyAfterFailure() {
        val terminal = BackupOperationState(message = "فشلت العملية")
        assertEquals(terminal.copy(message = null), interleavingClear(terminal))
    }
}
