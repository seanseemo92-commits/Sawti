package com.sawti.presentation.category

import com.sawti.domain.model.Category
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryLoadState(
    val categories: List<Category> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
)

/** Calls must be serialized on the owning scope's UI thread. */
class CategoryLoadController(
    private val scope: CoroutineScope,
    private val seed: suspend () -> Unit,
    private val observe: () -> Flow<List<Category>>,
) {
    private val mutableState = MutableStateFlow(CategoryLoadState())
    val state = mutableState.asStateFlow()
    private var job: Job? = null

    fun start() {
        if (job == null) load()
    }

    fun retry(): Boolean {
        if (state.value.loading) return false
        load()
        return true
    }

    private fun load() {
        val previous = job
        mutableState.value = state.value.copy(loading = true, error = null)
        val next = scope.launch(start = CoroutineStart.LAZY) {
            try {
                previous?.cancelAndJoin()
                seed()
                currentCoroutineContext().ensureActive()
                observe().collect { categories ->
                    currentCoroutineContext().ensureActive()
                    mutableState.value = CategoryLoadState(
                        categories = categories,
                        loading = false,
                        error = if (categories.isEmpty()) "لم تظهر الأقسام. حاول مرة أخرى." else null,
                    )
                }
                if (state.value.loading) {
                    mutableState.value = state.value.copy(
                        loading = false,
                        error = "تعذّر تحميل الأقسام. حاول مرة أخرى.",
                    )
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (error: Exception) {
                mutableState.value = state.value.copy(
                    loading = false,
                    error = "تعذّر تحميل الأقسام. حاول مرة أخرى.",
                )
            }
        }
        job = next
        next.start()
    }
}
