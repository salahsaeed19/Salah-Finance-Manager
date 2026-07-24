package com.salahabusaif.financemanager

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.salahabusaif.financemanager.core.designsystem.FinanceFloatingActionButton
import com.salahabusaif.financemanager.core.designsystem.FinanceScaffold
import com.salahabusaif.financemanager.core.designsystem.FinanceTheme
import com.salahabusaif.financemanager.core.designsystem.QuickActionSheet
import com.salahabusaif.financemanager.core.designsystem.R
import com.salahabusaif.financemanager.feature.accounts.AccountsScreen
import com.salahabusaif.financemanager.feature.budget.PlansScreen
import com.salahabusaif.financemanager.feature.dashboard.DashboardScreen
import com.salahabusaif.financemanager.feature.people.PeopleScreen
import com.salahabusaif.financemanager.feature.settings.SettingsScreen
import com.salahabusaif.financemanager.feature.settings.SettingsViewModel
import com.salahabusaif.financemanager.feature.transactions.TransactionsScreen

private enum class AppDestination(
    val label: Int,
) {
    HOME(
        R.string.nav_home,
    ),
    TRANSACTIONS(
        R.string.nav_transactions,
    ),
    PEOPLE(R.string.nav_people),
    PLANS(R.string.nav_plans),
    SETTINGS(R.string.nav_settings),
}

@Composable
fun financeApp(settingsViewModel: SettingsViewModel = hiltViewModel()) {
    val preferences by settingsViewModel.preferences.collectAsState()
    var destination by rememberSaveable { mutableStateOf(AppDestination.HOME) }
    var showActions by remember { mutableStateOf(false) }
    var showAccounts by remember { mutableStateOf(false) }
    var actionNotice by remember { mutableStateOf(false) }
    val snackbars = remember { SnackbarHostState() }
    val futureActionMessage = stringResource(R.string.future_action_message)
    LaunchedEffect(actionNotice) {
        if (actionNotice) {
            snackbars.showSnackbar(futureActionMessage)
            actionNotice = false
        }
    }
    FinanceTheme(preferences.theme) {
        FinanceScaffold(
            title = stringResource(if (showAccounts) R.string.my_accounts else destination.label),
            bottomBar = {
                financeBottomBar(destination) {
                    destination = it
                    showAccounts = false
                }
            },
            floatingActionButton = {
                if (destination != AppDestination.SETTINGS) {
                    FinanceFloatingActionButton { showActions = true }
                }
            },
        ) { padding ->
            androidx.compose.material3.Scaffold(snackbarHost = { SnackbarHost(snackbars) }) { snackbarPadding ->
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxSize().padding(padding).padding(snackbarPadding),
                ) {
                    if (showAccounts) {
                        AccountsScreen(onBack = { showAccounts = false })
                    } else {
                        when (destination) {
                            AppDestination.HOME -> DashboardScreen(onOpenAccounts = { showAccounts = true })
                            AppDestination.TRANSACTIONS -> TransactionsScreen(onCreateAccount = { showAccounts = true })
                            AppDestination.PEOPLE -> PeopleScreen()
                            AppDestination.PLANS -> PlansScreen()
                            AppDestination.SETTINGS -> SettingsScreen(settingsViewModel)
                        }
                    }
                }
            }
        }
        if (showActions) {
            QuickActionSheet(onDismiss = { showActions = false }) { action ->
                showActions = false
                if (action == com.salahabusaif.financemanager.core.designsystem.QuickAction.ADD_TRANSACTION) {
                    destination = AppDestination.TRANSACTIONS
                } else if (action == com.salahabusaif.financemanager.core.designsystem.QuickAction.ADD_SAVINGS) {
                    showAccounts = true
                } else {
                    actionNotice = true
                }
            }
        }
    }
}

@Composable
private fun financeBottomBar(
    selected: AppDestination,
    onSelect: (AppDestination) -> Unit,
) = NavigationBar(Modifier.testTag("bottom_navigation")) {
    AppDestination.entries.forEach { destination ->
        NavigationBarItem(
            selected = selected == destination,
            onClick = { onSelect(destination) },
            label = {
                Text(
                    text = stringResource(destination.label),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center,
                    style =
                        androidx.compose.material3.MaterialTheme.typography.labelSmall
                            .copy(fontSize = 10.sp),
                )
            },
            icon = { Icon(iconFor(destination), contentDescription = stringResource(destination.label)) },
            alwaysShowLabel = true,
            colors =
                NavigationBarItemDefaults.colors(
                    selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                ),
            modifier = Modifier.testTag("nav_${destination.name.lowercase()}"),
        )
    }
}

private fun iconFor(destination: AppDestination) =
    when (destination) {
        AppDestination.HOME -> Icons.Default.Home
        AppDestination.TRANSACTIONS -> Icons.AutoMirrored.Filled.ReceiptLong
        AppDestination.PEOPLE -> Icons.Default.People
        AppDestination.PLANS -> Icons.Default.Savings
        AppDestination.SETTINGS -> Icons.Default.Settings
    }
