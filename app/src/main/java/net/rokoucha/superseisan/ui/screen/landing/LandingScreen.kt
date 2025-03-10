package net.rokoucha.superseisan.ui.screen.landing

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
import androidx.compose.runtime.LaunchedEffect
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
import net.rokoucha.superseisan.data.model.SettlementListItem
import net.rokoucha.superseisan.ui.theme.SuperSeisanTheme
import java.time.OffsetDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    modifier: Modifier = Modifier,
    onSettlementCreated: (id: String) -> Unit = {},
    onItemClick: (id: String) -> Unit = {},
    viewModel: LandingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text(text = stringResource(R.string.app_name)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBottomSheet = true },
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
            items(uiState.settlements) { settlement ->
                SeisanItem(
                    settlement,
                    onClick = { onItemClick(settlement.id) },
                    onDeleteClick = {
                        viewModel.deleteSettlement(settlement.id)
                    }
                )
            }
        }

        LaunchedEffect(uiState.newTaskId) {
            if (uiState.newTaskId.isNotEmpty()) {
                onSettlementCreated(uiState.newTaskId)
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState
            ) {
                AddSettlementSheetContent(
                    onCreateClick = { name ->
                        viewModel.addSettlement(name)
                        showBottomSheet = false
                    },
                    onCancelClick = {
                        showBottomSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun SeisanItem(
    settlement: SettlementListItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(settlement.name)
        },
        supportingContent = {
            Text(
                settlement.participants.joinToString { it.name }
            )
        },
        trailingContent = {
            SeisanItemDropdown(
                onDeleteClick = onDeleteClick
            )
        },
        modifier = modifier
            .clickable(onClick = onClick)
    )
}

@Composable
fun SeisanItemDropdown(
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
fun AddSettlementSheetContent(
    onCreateClick: (name: String) -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(stringResource(R.string.label_name_input)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.padding(8.dp)
        ) {
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = { onCancelClick() }
            ) {
                Text(stringResource(R.string.label_cancel_button))
            }
            TextButton(
                enabled = text.isNotEmpty(),
                onClick = { onCreateClick(text) }
            ) {
                Text(stringResource(R.string.label_create_button))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LandingScreenPreview() {
    SuperSeisanTheme {
        LandingScreen()
    }
}

@Preview
@Composable
fun SeisanItemPreview() {
    SuperSeisanTheme {
        SeisanItem(
            settlement = SettlementListItem(
                "1",
                "Settlement 1",
                emptyList(),
                OffsetDateTime.now()
            ),
            onClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddSettlementSheetContentPreview() {
    SuperSeisanTheme {
        AddSettlementSheetContent(
            onCreateClick = {},
            onCancelClick = {}
        )
    }
}
