package com.redv6.callblocker.presentation.screens.blockedcalls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.redv6.callblocker.domain.model.BlockedCall
import com.redv6.callblocker.domain.repository.BlockedCallRepository
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
