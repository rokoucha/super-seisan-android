package net.rokoucha.superseisan.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.rokoucha.superseisan.ui.screen.currency.CurrencyScreen
import net.rokoucha.superseisan.ui.screen.detail.DetailScreen
import net.rokoucha.superseisan.ui.screen.landing.LandingScreen
import net.rokoucha.superseisan.ui.screen.participant.ParticipantScreen
import net.rokoucha.superseisan.ui.screen.result.ResultScreen
import net.rokoucha.superseisan.ui.theme.SuperSeisanTheme

enum class SuperSeisanScreen {
    Landing,
    Detail,
    Payers,
    Exchanges,
    Result
}

@Composable
fun SuperSeisanApp() {
    val navController = rememberNavController()

    SuperSeisanTheme {
        NavHost(
            navController = navController,
            startDestination = SuperSeisanScreen.Landing.name
        ) {
            composable(route = SuperSeisanScreen.Landing.name) {
                LandingScreen(
                    onSettlementCreated = { settlementId ->
                        navController.navigate("${SuperSeisanScreen.Detail.name}/$settlementId")
                    },
                    onItemClick = { settlementId ->
                        navController.navigate("${SuperSeisanScreen.Detail.name}/$settlementId")
                    }
                )
            }

            composable(route = "${SuperSeisanScreen.Detail.name}/{settlementId}") {
                DetailScreen(
                    navigateBack = { navController.popBackStack() },
                    onEditParticipantClick = { settlementId ->
                        navController.navigate("${SuperSeisanScreen.Payers.name}/$settlementId")
                    },
                    onEditCurrencyClick = { settlementId ->
                        navController.navigate("${SuperSeisanScreen.Exchanges.name}/$settlementId")
                    },
                    onResultClick = { settlementId, participantId ->
                        navController.navigate("${SuperSeisanScreen.Result.name}/$settlementId/$participantId")
                    }
                )
            }

            composable(route = "${SuperSeisanScreen.Payers.name}/{settlementId}") {
                ParticipantScreen(
                    navigateBack = { navController.popBackStack() },
                )
            }

            composable(route = "${SuperSeisanScreen.Exchanges.name}/{settlementId}") {
                CurrencyScreen(
                    navigateBack = { navController.popBackStack() },
                )
            }

            composable(route = "${SuperSeisanScreen.Result.name}/{settlementId}/{participantId}") {
                ResultScreen(
                    navigateBack = { navController.popBackStack() },
                )
            }
        }
    }
}
