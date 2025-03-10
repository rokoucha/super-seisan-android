package net.rokoucha.superseisan.ui.screen.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.rokoucha.superseisan.R
import net.rokoucha.superseisan.data.model.Currency
import net.rokoucha.superseisan.data.model.Item
import net.rokoucha.superseisan.data.model.Participant
import net.rokoucha.superseisan.ui.theme.SuperSeisanTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navigateBack: () -> Unit,
    onEditParticipantClick: (settlementId: String) -> Unit,
    onEditCurrencyClick: (settlementId: String) -> Unit,
    onResultClick: (settlementId: String, participantId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                actions = {
                    TopAppBarActionDropdownMenu(
                        onEditCurrencyClick = { onEditCurrencyClick(uiState.settlement.id) },
                        onEditParticipantClick = { onEditParticipantClick(uiState.settlement.id) }
                    )
                },
                title = { Text(uiState.settlement.name) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.editItem(
                        Item(
                            id = "",
                            name = "",
                            payer = null,
                            price = 0.0,
                            currency = null,
                            quantity = 1,
                            benefited = uiState.settlement.participants
                        )
                    )
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
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(innerPadding)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.label_participant_section_headline),
                    style = MaterialTheme.typography.headlineMedium
                )
                if (uiState.settlement.participants.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.settlement.participants) { participant ->
                            ParticipantCard(
                                participant = participant,
                                onClick = {
                                    onEditParticipantClick(uiState.settlement.id)
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.label_participant_section_empty_message),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            if (uiState.settlement.currencies.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_currency_section_headline),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.settlement.currencies.toList()) { currency ->
                            CurrencyCard(
                                currency = currency,
                                onClick = {
                                    onEditCurrencyClick(uiState.settlement.id)
                                }
                            )
                        }
                    }
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.label_payment_item_section_headline),
                    style = MaterialTheme.typography.headlineMedium
                )
                if (uiState.settlement.items.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.settlement.items) { item ->
                            PaymentItemCard(
                                item = item,
                                onClick = {
                                    viewModel.editItem(item)
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.label_payment_item_empty_message),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            if (uiState.result.details.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_result_section_headline),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.result.details) {
                            ResultItemCard(
                                name = it.participant.name,
                                paymentAmount = it.differences,
                                onClick = {
                                    onResultClick(uiState.settlement.id, it.participant.id)
                                }
                            )
                        }
                        item {
                            ResultItemCard(
                                name = stringResource(R.string.label_result_surplus),
                                paymentAmount = uiState.result.surplus,
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }

        if (uiState.editingItem != null) {
            PaymentItemEditor(
                item = uiState.editingItem!!,
                currencies = uiState.settlement.currencies.toList(),
                participants = uiState.settlement.participants.toList(),
                sheetState = sheetState,
                onDismissRequest = { viewModel.cancelEditing() },
                onSaveClick = { title, payer, price, quantity, currency, benefited ->
                    viewModel.saveItem(
                        Item(
                            id = uiState.editingItem!!.id,
                            name = title,
                            payer = payer,
                            price = price,
                            currency = currency,
                            quantity = quantity,
                            benefited = benefited
                        )
                    )
                },
                onDeleteClick = { viewModel.deleteItem(uiState.editingItem!!) }
            )
        }
    }
}

@Composable
fun TopAppBarActionDropdownMenu(
    onEditCurrencyClick: () -> Unit,
    onEditParticipantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    IconButton(
        onClick = { expanded = true },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(R.string.hint_more_button)
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.label_participant_screen_title)) },
            onClick = {
                onEditParticipantClick()
                expanded = false
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.label_currency_screen_title)) },
            onClick = {
                onEditCurrencyClick()
                expanded = false
            }
        )
    }
}

@Composable
fun ParticipantCard(
    participant: Participant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = participant.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun CurrencyCard(
    currency: Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "1 ${currency.symbol}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = currency.rate.toString(),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun PaymentItemCard(
    item: Item,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val payer = item.payer?.name ?: ""
    val currency = item.currency?.symbol ?: stringResource(R.string.label_yen_symbol)

    Card(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "$payer: ${item.price} $currency",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ResultItemCard(
    name: String,
    paymentAmount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.label_price_with_yen, paymentAmount),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview
@Composable
fun PreviewDetailScreen() {
    SuperSeisanTheme {
        DetailScreen(
            navigateBack = {},
            onEditParticipantClick = {},
            onEditCurrencyClick = {},
            onResultClick = { _, _ -> }
        )
    }
}

@Preview
@Composable
fun ParticipantCardPreview() {
    SuperSeisanTheme {
        ParticipantCard(
            Participant(
                id = "",
                name = "あいうえお"
            ),
            onClick = {}
        )
    }
}

@Preview
@Composable
fun PreviewExchangeRateCard() {
    SuperSeisanTheme {
        CurrencyCard(
            Currency(
                id = "",
                symbol = "USD",
                rate = 150.0
            ),
            onClick = {}
        )
    }
}

@Preview
@Composable
fun PreviewPaymentItemCard() {
    SuperSeisanTheme {
        PaymentItemCard(
            item = Item(
                id = "",
                name = "あいうえお",
                payer = Participant(
                    id = "",
                    name = "かきくけこ"
                ),
                price = 100.0,
                currency = null,
                quantity = 1,
                benefited = emptyList()
            ),
            onClick = {}
        )
    }
}

@Preview
@Composable
fun PreviewResultItemCard() {
    SuperSeisanTheme {
        ResultItemCard(
            name = "あいうえお",
            paymentAmount = 100,
            onClick = {}
        )
    }
}
