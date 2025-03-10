package net.rokoucha.superseisan.ui.screen.currency

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
import net.rokoucha.superseisan.data.model.Currency
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val repository: SuperSeisanRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val settlementId = savedStateHandle.get<String>("settlementId")!!

    private val _currencies = repository.getCurrenciesStream(settlementId)
    private val _editingCurrency = MutableStateFlow<Currency?>(null)

    val uiState: StateFlow<CurrencyUiState> =
        combine(_currencies, _editingCurrency) { currencies, editingCurrency ->
            CurrencyUiState(
                currencies,
                editingCurrency
            )
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = CurrencyUiState(emptyList())
            )

    fun edit(currency: Currency) {
        _editingCurrency.value = currency
    }

    fun cancelEditing() {
        _editingCurrency.value = null
    }

    fun save(currency: Currency) = viewModelScope.launch {
        if (currency.id.isNotEmpty()) {
            repository.updateCurrency(settlementId, currency)
        } else {
            repository.addCurrency(settlementId, currency.symbol, currency.rate)
        }
        cancelEditing()
    }

    fun delete(currency: Currency) = viewModelScope.launch {
        repository.deleteCurrency(settlementId, currency.id)
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class CurrencyUiState(
    val currencies: List<Currency> = emptyList(),
    val editingCurrency: Currency? = null
)
