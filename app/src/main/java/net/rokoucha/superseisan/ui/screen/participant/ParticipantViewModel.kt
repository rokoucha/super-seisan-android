package net.rokoucha.superseisan.ui.screen.participant

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
import net.rokoucha.superseisan.data.model.Participant
import javax.inject.Inject

@HiltViewModel
class ParticipantViewModel @Inject constructor(
    private val repository: SuperSeisanRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val settlementId = savedStateHandle.get<String>("settlementId")!!

    private val _participants = repository.getParticipantsStream(settlementId)
    private val _editingParticipant = MutableStateFlow<Participant?>(null)

    val uiState: StateFlow<ParticipantUiState> =
        combine(
            _participants,
            _editingParticipant
        ) { participants, editingParticipant ->
            ParticipantUiState(
                participants,
                editingParticipant
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = ParticipantUiState()
        )

    fun edit(participant: Participant) {
        _editingParticipant.value = participant
    }

    fun cancelEditing() {
        _editingParticipant.value = null
    }

    fun save(participant: Participant) = viewModelScope.launch {
        if (participant.id.isNotEmpty()) {
            repository.updateParticipant(settlementId, participant)
        } else {
            repository.addParticipant(settlementId, participant.name)
        }
        cancelEditing()
    }

    fun delete(participant: Participant) = viewModelScope.launch {
        repository.deleteParticipant(settlementId, participant.id)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class ParticipantUiState(
    val participants: List<Participant> = emptyList(),
    val editingParticipant: Participant? = null
)
