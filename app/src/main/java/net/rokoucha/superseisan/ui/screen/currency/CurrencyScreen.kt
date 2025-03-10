package net.rokoucha.superseisan.ui.screen.currency

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.rokoucha.superseisan.R
import net.rokoucha.superseisan.data.model.Currency
import net.rokoucha.superseisan.ui.theme.SuperSeisanTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CurrencyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.hint_back_button),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                title = { Text(text = stringResource(R.string.label_currency_screen_title)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.edit(Currency("", "", 0.0))
                },
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Icon(
                    Icons.Filled.Add,
                    stringResource(R.string.hint_add_button)
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {
            items(uiState.currencies) { currency ->
                CurrencyListItem(
                    currency = currency,
                    onClick = {
                        viewModel.edit(currency)
                    },
                    onDeleteClick = {
                        viewModel.delete(currency)
                    }
                )
            }
        }

        if (uiState.editingCurrency != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.cancelEditing()
                },
                sheetState = sheetState
            ) {
                CurrencyEditScreenContent(
                    currency = uiState.editingCurrency!!,
                    onSaveClick = { symbol, rate ->
                        viewModel.save(uiState.editingCurrency!!.copy(symbol = symbol, rate = rate))
                    },
                    onCancelClick = {
                        viewModel.cancelEditing()
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun CurrencyListItem(
    currency: Currency,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = currency.symbol)
                Text(
                    text = stringResource(R.string.label_price_with_yen, currency.rate.toString()),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        trailingContent = {
            CurrencyListItemDropdown(
                onDeleteClick = onDeleteClick
            )
        },
        modifier = modifier
            .clickable(onClick = onClick)
    )
}

@Composable
fun CurrencyListItemDropdown(
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.hint_more_button)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.label_delete_button)) },
                onClick = {
                    onDeleteClick()
                    expanded = false
                }
            )
        }
    }
}


@Composable
fun CurrencyEditScreenContent(
    currency: Currency,
    onSaveClick: (currency: String, rate: Double) -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currencyValue by rememberSaveable { mutableStateOf(currency.symbol) }
    var rateValue by rememberSaveable { mutableStateOf(if (currency.rate == 0.0) "" else currency.rate.toString()) }

    val rateIsInvalid = rateValue != "" && rateValue.toDoubleOrNull() == null

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        TextField(
            value = currencyValue,
            onValueChange = { currencyValue = it },
            label = { Text(stringResource(R.string.label_currency_symbol_input)) },
            modifier = Modifier
                .fillMaxWidth()
        )
        TextField(
            value = rateValue,
            onValueChange = { rateValue = it },
            label = { Text(stringResource(R.string.label_currency_rate_input)) },
            isError = rateIsInvalid,
            modifier = Modifier
                .fillMaxWidth()
        )
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { onCancelClick() }) {
                Text(stringResource(R.string.label_cancel_button))
            }
            TextButton(
                enabled = !rateIsInvalid && currencyValue.isNotEmpty() && rateValue.isNotEmpty(),
                onClick = { onSaveClick(currencyValue, rateValue.toDouble()) }
            ) {
                Text(stringResource(R.string.label_save_button))
            }
        }
    }
}

@Preview
@Composable
fun CurrencyScreenPreview() {
    SuperSeisanTheme {
        CurrencyScreen(
            navigateBack = { }
        )
    }
}

@Preview
@Composable
fun CurrencyListItemPreview() {
    SuperSeisanTheme {
        CurrencyListItem(
            currency = Currency(
                id = "1",
                symbol = "USD",
                rate = 150.0
            ),
            onClick = { },
            onDeleteClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CurrencyEditScreenContentPreview() {
    SuperSeisanTheme {
        CurrencyEditScreenContent(
            currency = Currency(
                id = "1",
                symbol = "USD",
                rate = 150.0
            ),
            onSaveClick = { _, _ -> },
            onCancelClick = { }
        )
    }
}