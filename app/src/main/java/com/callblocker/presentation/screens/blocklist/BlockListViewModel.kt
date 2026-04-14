package com.callblocker.presentation.screens.blocklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callblocker.domain.model.BlockedNumber
import com.callblocker.domain.repository.BlockedNumberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockListViewModel @Inject constructor(
    private val blockedNumberRepository: BlockedNumberRepository
) : ViewModel() {

    val blockedNumbers: StateFlow<List<BlockedNumber>> = blockedNumberRepository
        .getAllBlockedNumbers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addBlockedNumber(phoneNumber: String, label: String?, isPrefix: Boolean = false) {
        viewModelScope.launch {
            blockedNumberRepository.addBlockedNumber(
                BlockedNumber(
                    phoneNumber = phoneNumber,
                    label = label?.takeIf { it.isNotBlank() },
                    isPrefix = isPrefix
                )
            )
        }
    }

    fun deleteBlockedNumber(id: Long) {
        viewModelScope.launch {
            blockedNumberRepository.deleteBlockedNumber(id)
        }
    }
}
