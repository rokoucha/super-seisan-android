package net.rokoucha.superseisan.ui.screen.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import net.rokoucha.superseisan.data.SuperSeisanRepository
import net.rokoucha.superseisan.data.model.Participant
import net.rokoucha.superseisan.data.model.SettlementResultDetail
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val repository: SuperSeisanRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val settlementId: String = savedStateHandle.get<String>("settlementId")!!
    private val participantId: String = savedStateHandle.get<String>("participantId")!!

    private val _settlement = repository.getSettlementStream(settlementId)
    private val _settlementResult = repository.calculateSettlementResult(_settlement)

    val uiState: StateFlow<ResultUiState> =
        combine(_settlement, _settlementResult) { _, settlementResult ->
            val detail = settlementResult.details.find { it.participant.id == participantId }
            if (detail == null) return@combine ResultUiState()

            ResultUiState(detail)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = ResultUiState()
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class ResultUiState(
    val detail: SettlementResultDetail = SettlementResultDetail(
        Participant("", ""),
        0,
        0,
        0,
        emptyList()
    )
)
