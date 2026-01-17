package com.callblocker.presentation.screens.blockedcalls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callblocker.domain.model.BlockedCall
import com.callblocker.domain.repository.BlockedCallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockedCallsViewModel @Inject constructor(
    private val blockedCallRepository: BlockedCallRepository
) : ViewModel() {

    val blockedCalls: StateFlow<List<BlockedCall>> = blockedCallRepository
        .getAllBlockedCalls()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteBlockedCall(id: Long) {
        viewModelScope.launch {
            blockedCallRepository.deleteBlockedCall(id)
        }
    }

    fun clearAllBlockedCalls() {
        viewModelScope.launch {
            blockedCallRepository.clearAllBlockedCalls()
        }
    }
}
