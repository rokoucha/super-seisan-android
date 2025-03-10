package net.rokoucha.superseisan.ui.screen.participant

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.rokoucha.superseisan.R
import net.rokoucha.superseisan.data.model.Participant
import net.rokoucha.superseisan.ui.theme.SuperSeisanTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ParticipantViewModel = hiltViewModel()
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
                title = { Text(text = stringResource(R.string.label_participant_screen_title)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.edit(Participant(id = "", name = ""))
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
            items(uiState.participants) { participant ->
                ParticipantListItem(
                    participant = participant,
                    onClick = {
                        viewModel.edit(participant)
                    },
                    onDeleteClick = {
                        viewModel.delete(participant)
                    }
                )
            }
        }

        if (uiState.editingParticipant != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.cancelEditing()
                },
                sheetState = sheetState
            ) {
                ParticipantEditSheetContent(
                    name = uiState.editingParticipant!!.name,
                    onSaveClick = {
                        viewModel.save(uiState.editingParticipant!!.copy(name = it))
                    },
                    onCancelClick = {
                        viewModel.cancelEditing()
                    }
                )
            }
        }
    }
}

@Composable
fun ParticipantListItem(
    participant: Participant,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(text = participant.name)
        },
        trailingContent = {
            ParticipantListItemDropdown(
                onDeleteClick = onDeleteClick
            )
        },
        modifier = modifier
            .clickable(onClick = onClick)
    )
}

@Composable
fun ParticipantListItemDropdown(
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
fun ParticipantEditSheetContent(
    name: String,
    onSaveClick: (value: String) -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by rememberSaveable { mutableStateOf(name) }

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
                onClick = { onSaveClick(text) }
            ) {
                Text(stringResource(R.string.label_save_button))
            }
        }
    }
}

@Preview
@Composable
fun ParticipantScreenPreview() {
    SuperSeisanTheme {
        ParticipantScreen(
            navigateBack = {}
        )
    }
}

@Preview
@Composable
fun ParticipantListItemPreview() {
    SuperSeisanTheme {
        ParticipantListItem(
            participant = Participant(
                id = "1",
                name = "Payer1"
            ),
            onClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ParticipantEditSheetContentPreview() {
    SuperSeisanTheme {
        ParticipantEditSheetContent(
            name = "Payer1",
            onCancelClick = {},
            onSaveClick = {}
        )
    }
}
