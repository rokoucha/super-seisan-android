package net.rokoucha.superseisan.ui.screen.landing

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
import net.rokoucha.superseisan.data.model.SettlementListItem
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(private val repository: SuperSeisanRepository) :
    ViewModel() {
    private val _settlements = repository.getSettlementsStream()
    private val _newTaskId = MutableStateFlow("")

    val uiState: StateFlow<LandingUiState> = combine(
        _settlements, _newTaskId
    ) { settlements, newTaskId ->
        LandingUiState(settlements, newTaskId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = LandingUiState()
    )

    fun addSettlement(name: String) = viewModelScope.launch {
        val id = repository.addSettlement(name)
        _newTaskId.value = id
    }

    fun deleteSettlement(id: String) = viewModelScope.launch {
        repository.deleteSettlement(id)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class LandingUiState(
    val settlements: List<SettlementListItem> = listOf(),
    val newTaskId: String = ""
)
