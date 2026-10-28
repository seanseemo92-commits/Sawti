package com.sawti.data

import android.content.Context
import android.net.Uri
import com.sawti.data.db.AppDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Backup work belongs to the process, not to a Compose destination. Leaving Settings
 * must never cancel a restore after Room has been closed or files have been moved.
 * BackupManager's durable restore journal handles an unexpected process/device stop.
 */
data class BackupOperationState(
    val busy: Boolean = false,
    val restoring: Boolean = false,
    val restartRequired: Boolean = false,
    val message: String? = null,
)

class BackupOperations(context: Context, database: AppDatabase) {
    private val manager = BackupManager(context.applicationContext, database)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(BackupOperationState())
    val state: StateFlow<BackupOperationState> = _state.asStateFlow()

    /** Refuse duplicate requests and all use of old Room handles after successful restore. */
    @Synchronized
    fun export(uri: Uri) {
        if (_state.value.busy || _state.value.restartRequired) return
        _state.value = BackupOperationState(busy = true)
        scope.launch {
            try {
                val receipt = manager.export(uri)
                val kib = (receipt.sizeBytes + 1023) / 1024
                _state.value = BackupOperationState(
                    message = "تم حفظ النسخة الاحتياطية والتحقق منها بنجاح ($kib كيلوبايت، ${receipt.payloadFiles} ملفات)."
                )
            } catch (cancel: CancellationException) {
                _state.value = BackupOperationState(message = "توقفت عملية إنشاء النسخة الاحتياطية.")
                throw cancel
            } catch (error: Exception) {
                _state.value = BackupOperationState(
                    message = "فشل إنشاء النسخة الاحتياطية: ${error.message ?: "خطأ غير معروف"}. لا تستخدم ملفاً فارغاً للاسترجاع."
                )
            }
        }
    }

    @Synchronized
    fun restore(uri: Uri) {
        if (_state.value.busy || _state.value.restartRequired) return
        _state.value = BackupOperationState(busy = true, restoring = true)
        scope.launch {
            try {
                manager.restore(uri)
                // Do not let any UI reopen Room after the restored files replace its database.
                _state.value = BackupOperationState(restartRequired = true)
            } catch (cancel: CancellationException) {
                _state.value = BackupOperationState(message = "توقفت عملية الاسترجاع؛ راجع حالة البيانات بعد إعادة فتح التطبيق.")
                throw cancel
            } catch (error: Exception) {
                _state.value = BackupOperationState(
                    message = "تعذّر الاسترجاع: ${error.message ?: "خطأ غير معروف"}"
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }
}
