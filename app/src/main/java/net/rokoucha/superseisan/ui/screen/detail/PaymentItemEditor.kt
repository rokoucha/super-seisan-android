package net.rokoucha.superseisan.ui.screen.detail

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.rokoucha.superseisan.R
import net.rokoucha.superseisan.data.model.Currency
import net.rokoucha.superseisan.data.model.Item
import net.rokoucha.superseisan.data.model.Participant
import net.rokoucha.superseisan.ui.theme.SuperSeisanTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentItemEditor(
    item: Item,
    currencies: List<Currency>,
    participants: List<Participant>,
    onDismissRequest: () -> Unit,
    onSaveClick: (title: String, payer: Participant?, price: Double, quantity: Int, currency: Currency?, benefited: List<Participant>) -> Unit,
    onDeleteClick: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        PaymentItemEditorContent(
            item = item,
            currencies = currencies,
            participants = participants,
            onCloseClick = onDismissRequest,
            onSaveClick = onSaveClick,
            onDeleteClick = onDeleteClick,
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Composable
fun PaymentItemEditorContent(
    item: Item,
    currencies: List<Currency>,
    participants: List<Participant>,
    onCloseClick: () -> Unit,
    onSaveClick: (title: String, payer: Participant?, price: Double, quantity: Int, currency: Currency?, benefited: List<Participant>) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf(item.name) }
    var payer by remember { mutableStateOf(item.payer) }
    var price by rememberSaveable { mutableStateOf(if (item.price != 0.0) item.price.toString() else "") }
    var quantity by rememberSaveable { mutableStateOf(item.quantity.toString()) }
    var currency by remember { mutableStateOf(item.currency) }
    // participantsからbenefitedを除いたもの
    var exempts = remember { participants.filter { it !in item.benefited }.toMutableStateList() }

    var showPayerSelectDialogue by rememberSaveable { mutableStateOf(false) }
    var showExchangeRateSelectDialogue by rememberSaveable { mutableStateOf(false) }
    var showExemptSelectDialogue by rememberSaveable { mutableStateOf(false) }

    val payerIsInvalid = payer == null && (name.isNotEmpty() || price.isNotEmpty())
    val priceIsInvalid = price != "" && price.toDoubleOrNull() == null
    val quantityIsInvalid = quantity != "" && quantity.toIntOrNull() == null

    val priceDouble = price.toDoubleOrNull()
    val quantityInt = quantity.toIntOrNull()

    val currencyLabel = currency?.symbol ?: stringResource(R.string.label_yen_symbol)
    val total =
        if (priceDouble != null && quantityInt != null) {
            priceDouble * (if (priceDouble < 1.0) 1.0 else quantityInt.toDouble())
        } else 0.0

    val savable =
        name.isNotEmpty() && payer != null && priceDouble != null && quantityInt != null && exempts.size < participants.size

    Surface {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
        ) {
            Row(
                modifier = Modifier.padding(start = 4.dp, end = 16.dp)
            ) {
                IconButton(
                    onClick = onCloseClick
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.hint_close_button)
                    )

                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    enabled = savable,
                    onClick = {
                        onSaveClick(
                            name,
                            payer,
                            price.toDouble(),
                            quantity.toInt(),
                            currency,
                            participants.toSet().subtract(exempts).toList()
                        )
                    }
                ) {
                    Text(stringResource(R.string.label_save_button))
                }
                if (item.id.isNotEmpty()) {
                    PaymentItemEditorDropdownMenu(
                        onDeleteClick = onDeleteClick
                    )
                }
            }
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(stringResource(R.string.label_name_input)) },
                modifier = Modifier
                    .padding(start = 56.dp, end = 16.dp)
                    .fillMaxWidth()
            )
            HorizontalDivider()
            ListItem(
                leadingContent = {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = stringResource(R.string.hint_payer_section)
                    )
                },
                headlineContent = {
                    Text(
                        payer?.name ?: stringResource(R.string.label_payer_input),
                        color = if (payerIsInvalid) MaterialTheme.colorScheme.error else Color.Black,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                modifier = Modifier
                    .clickable(onClick = {
                        if (participants.isEmpty()) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.label_payer_edit_participant_is_empty_message),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@clickable
                        }
                        showPayerSelectDialogue = true
                    })
            )
            HorizontalDivider()
            ListItem(
                leadingContent = {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                },
                headlineContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = price,
                            onValueChange = { price = it },
                            isError = priceIsInvalid,
                            placeholder = { Text(stringResource(R.string.label_price_input)) },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.weight(0.1f))
                        OutlinedButton(
                            onClick = {
                                showExchangeRateSelectDialogue = true
                            },
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                currencyLabel,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            )
            HorizontalDivider()
            Column {
                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.hint_quantity_section)
                        )
                    },
                    headlineContent = {
                        TextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            isError = quantityIsInvalid,
                            placeholder = { Text(stringResource(R.string.label_quantity_input)) },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
                ListItem(
                    leadingContent = {
                        Text(stringResource(R.string.label_quantity_input_label))
                    },
                    headlineContent = {
                        Text("$total $currencyLabel")
                    }
                )
            }
            HorizontalDivider()
            LazyColumn {
                itemsIndexed(exempts) { i, exempt ->
                    ListItem(
                        leadingContent = {
                            if (i == 0) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = stringResource(R.string.hint_exempts_section)
                                )
                            } else {
                                Spacer(modifier = Modifier.width(24.dp))
                            }
                        },
                        headlineContent = {
                            Text(exempt.name)
                        },
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    exempts.removeAt(i)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(R.string.hint_remove_button)
                                )
                            }
                        }
                    )
                }
                if (exempts.size < participants.size) {
                    item {
                        ListItem(
                            leadingContent = {
                                if (exempts.isEmpty()) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = stringResource(R.string.hint_exempts_section)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(24.dp))
                                }
                            },
                            headlineContent = {
                                Text(
                                    stringResource(R.string.label_add_exempt_button),
                                    color = Color.Black
                                )
                            },
                            modifier = Modifier
                                .clickable(onClick = { showExemptSelectDialogue = true })
                        )
                    }
                }
            }
        }

        if (showPayerSelectDialogue) {
            PayerSelectDialogue(
                currentPayer = payer,
                participants = participants,
                onSelect = {
                    payer = it
                    showPayerSelectDialogue = false
                },
                onDismissRequest = { showPayerSelectDialogue = false }
            )
        }

        if (showExchangeRateSelectDialogue) {
            ExchangeRateSelectDialogue(
                currentCurrency = currency,
                currencies = currencies,
                onSelect = {
                    currency = it
                    showExchangeRateSelectDialogue = false
                },
                onDismissRequest = { showExchangeRateSelectDialogue = false }
            )
        }

        if (showExemptSelectDialogue) {
            ExemptSelectDialogue(
                exempts = participants.toSet().subtract(exempts).toList(),
                onSelect = {
                    exempts.add(it)
                    showExemptSelectDialogue = false
                },
                onDismissRequest = { showExemptSelectDialogue = false }
            )
        }
    }
}

@Composable
fun PaymentItemEditorDropdownMenu(
    onDeleteClick: () -> Unit,
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
            text = { Text(stringResource(R.string.label_delete_button)) },
            onClick = {
                onDeleteClick()
                expanded = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayerSelectDialogue(
    currentPayer: Participant?,
    participants: List<Participant>,
    onSelect: (payer: Participant) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                LazyColumn {
                    items(participants) { participant ->
                        ListItem(
                            leadingContent = {
                                RadioButton(
                                    selected = participant == currentPayer,
                                    onClick = { onSelect(participant) }
                                )
                            },
                            headlineContent = {
                                Text(participant.name)
                            },
                            modifier = Modifier
                                .clickable(onClick = { onSelect(participant) })
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRateSelectDialogue(
    currentCurrency: Currency?,
    currencies: List<Currency>,
    onSelect: (currency: Currency?) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                LazyColumn {
                    item {
                        ListItem(
                            leadingContent = {
                                RadioButton(
                                    selected = currentCurrency == null,
                                    onClick = { onSelect(null) }
                                )
                            },
                            headlineContent = {
                                Text(stringResource(R.string.label_yen_currency_code))
                            },
                            modifier = Modifier
                                .clickable(onClick = { onSelect(null) })
                        )
                    }
                    items(currencies) { currency ->
                        ListItem(
                            leadingContent = {
                                RadioButton(
                                    selected = currentCurrency == currency,
                                    onClick = { onSelect(currency) }
                                )
                            },
                            headlineContent = {
                                Text(currency.symbol)
                            },
                            modifier = Modifier
                                .clickable(onClick = { onSelect(currency) })
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExemptSelectDialogue(
    exempts: List<Participant>,
    onSelect: (exempt: Participant) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                LazyColumn {
                    items(exempts) { exempt ->
                        ListItem(
                            leadingContent = {
                                RadioButton(
                                    selected = false,
                                    onClick = { onSelect(exempt) }
                                )
                            },
                            headlineContent = {
                                Text(exempt.name)
                            },
                            modifier = Modifier
                                .clickable(onClick = { onSelect(exempt) })
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PaymentItemEditorPreview() {
    SuperSeisanTheme {
        PaymentItemEditor(
            item = Item(
                id = "id",
                name = "Item",
                payer = Participant("id", "Payer"),
                price = 100.0,
                quantity = 1,
                currency = Currency("id", "USD", 150.0),
                benefited = listOf(Participant("id", "Benefited1"), Participant("id", "Benefited2"))
            ),
            currencies = listOf(Currency("id", "USD", 150.0)),
            participants = listOf(
                Participant("id", "Payer"),
                Participant("id", "Benefited1"),
                Participant("id", "Benefited2")
            ),
            onDismissRequest = {},
            onSaveClick = { _, _, _, _, _, _ -> },
            onDeleteClick = {},
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentItemEditorContentPreview() {
    SuperSeisanTheme {
        PaymentItemEditorContent(
            item = Item(
                id = "id",
                name = "Item",
                payer = Participant("id", "Payer"),
                price = 100.0,
                quantity = 1,
                currency = Currency("id", "USD", 150.0),
                benefited = listOf(Participant("id", "Benefited1"), Participant("id", "Benefited2"))
            ),
            currencies = listOf(Currency("id", "USD", 150.0)),
            participants = listOf(
                Participant("id", "Payer"),
                Participant("id", "Benefited1"),
                Participant("id", "Benefited2")
            ),
            onCloseClick = {},
            onSaveClick = { _, _, _, _, _, _ -> },
            onDeleteClick = {}
        )
    }
}

@Preview
@Composable
fun PayerSelectDialoguePreview() {
    SuperSeisanTheme {
        PayerSelectDialogue(
            currentPayer = null,
            participants = listOf(Participant("id", "Payer1"), Participant("id", "Payer2")),
            onSelect = {},
            onDismissRequest = {}
        )
    }
}

@Preview
@Composable
fun ExchangeRateSelectDialoguePreview() {
    SuperSeisanTheme {
        ExchangeRateSelectDialogue(
            currentCurrency = null,
            currencies = listOf(Currency("id", "USD", 150.0), Currency("id", "EUR", 160.0)),
            onSelect = {},
            onDismissRequest = {}
        )
    }
}

@Preview
@Composable
fun ExemptSelectDialoguePreview() {
    SuperSeisanTheme {
        ExemptSelectDialogue(
            exempts = listOf(Participant("id", "Exempt1"), Participant("id", "Exempt2")),
            onSelect = {},
            onDismissRequest = {}
        )
    }
}
