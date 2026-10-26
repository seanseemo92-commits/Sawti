package com.mywords.presentation.progress

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mywords.MyWordsApp
import com.mywords.domain.model.ProgressSummary
import com.mywords.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Feeds the caregiver-only progress tab. */
class ProgressViewModel(private val repository: ProgressRepository) : ViewModel() {

    val summary: StateFlow<ProgressSummary> = repository.observeSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressSummary())

    fun clear() {
        viewModelScope.launch { repository.clear() }
    }

    class Factory(private val app: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProgressViewModel((app as MyWordsApp).container.progressRepository) as T
    }
}
