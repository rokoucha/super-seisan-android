package net.rokoucha.superseisan.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.rokoucha.superseisan.data.SuperSeisanRepository
import net.rokoucha.superseisan.data.model.Item
import net.rokoucha.superseisan.data.model.Settlement
import net.rokoucha.superseisan.data.model.SettlementResult
import java.time.OffsetDateTime
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: SuperSeisanRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val settlementId: String = savedStateHandle.get<String>("settlementId")!!

    private val _settlement = repository.getSettlementStream(settlementId)
    private val _settlementResult = repository.calculateSettlementResult(_settlement)
    private val _editingItem = MutableStateFlow<Item?>(null)

    val uiState: StateFlow<DetailUiState> =
        combine(
            _settlement,
            _settlementResult,
            _editingItem
        ) { settlement, settlementResult, editingItem ->
            DetailUiState(settlement, settlementResult, editingItem)
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = DetailUiState()
            )

    fun editItem(item: Item) {
        _editingItem.value = item
    }

    fun cancelEditing() {
        _editingItem.value = null
    }

    fun saveItem(item: Item) = viewModelScope.launch {
        if (item.id.isNotEmpty()) {
            repository.updateItem(settlementId, item)
        } else {
            repository.addItem(settlementId, item)
        }
        cancelEditing()
    }

    fun deleteItem(item: Item) = viewModelScope.launch {
        repository.deleteItem(settlementId, item.id)
        cancelEditing()
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class DetailUiState(
    val settlement: Settlement = Settlement(
        id = "",
        name = "",
        items = emptyList(),
        participants = emptyList(),
        currencies = emptyList(),
        updatedAt = OffsetDateTime.MIN,
        lastAccessedAt = OffsetDateTime.MIN
    ),
    val result: SettlementResult = SettlementResult(
        0,
        emptyList()
    ),
    val editingItem: Item? = null
)
