package net.rokoucha.superseisan.ui.screen.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.rokoucha.superseisan.R
import net.rokoucha.superseisan.ui.theme.SuperSeisanTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                title = {
                    Text(
                        stringResource(
                            R.string.label_result_screen_title,
                            uiState.detail.participant.name
                        )
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Surface(
            modifier = Modifier.padding(innerPadding)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "計",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ResultDetailCard(
                            name = stringResource(R.string.label_expenditures),
                            amount = uiState.detail.expenditures
                        )
                        ResultDetailCard(
                            name = stringResource(R.string.label_payments),
                            amount = uiState.detail.payments
                        )
                        ResultDetailCard(
                            name = stringResource(R.string.label_differences),
                            amount = uiState.detail.differences
                        )
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_result_items_headline),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    LazyColumn {
                        items(uiState.detail.items) {
                            ResultListItem(
                                name = it.first,
                                payer = "",
                                price = it.second
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )
        content()
    }
}

@Composable
fun ResultDetailCard(
    name: String,
    amount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = stringResource(R.string.label_price_with_yen, amount),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ResultListItem(
    name: String,
    payer: String,
    price: Int,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = payer,
                style = MaterialTheme.typography.bodySmall
            )
        },
        trailingContent = {
            Text(
                text = stringResource(R.string.label_price_with_yen, price),
                style = MaterialTheme.typography.titleLarge
            )
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun PreviewResultScreen() {
    SuperSeisanTheme {
        ResultScreen(
            navigateBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewResultSection() {
    SuperSeisanTheme {
        ResultSection(
            title = "Title"
        ) {
            Text("contents1")
            Text("contents2")
            Text("contents3")
        }
    }
}

@Preview
@Composable
fun PreviewResultDetailCard() {
    SuperSeisanTheme {
        ResultDetailCard(
            name = "支出計",
            amount = 1000
        )
    }
}

@Preview
@Composable
fun PreviewResultListItem() {
    SuperSeisanTheme {
        ResultListItem(
            name = "Item1",
            payer = "Payer1",
            price = 100
        )
    }
}