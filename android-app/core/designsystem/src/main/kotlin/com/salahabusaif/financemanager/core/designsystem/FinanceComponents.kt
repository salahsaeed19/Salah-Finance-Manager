@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.salahabusaif.financemanager.core.designsystem

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.salahabusaif.financemanager.core.model.CurrencyCode
import com.salahabusaif.financemanager.core.money.Money
import com.salahabusaif.financemanager.core.money.MoneyFormatter

@Composable
fun MoneyText(
    money: Money,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    val formatted = MoneyFormatter.format(money, LocalConfiguration.current.locales[0])
    Text(
        text = formatted,
        modifier = modifier.semantics { contentDescription = formatted },
        color = color,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
fun HiddenMoneyText(
    money: Money,
    hidden: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    val hiddenDescription = stringResource(R.string.hide_amount)
    if (hidden) {
        Text(
            text = "••••",
            modifier = modifier.semantics { contentDescription = hiddenDescription },
            color = color,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    } else {
        MoneyText(money, modifier, color)
    }
}

@Composable
fun CurrencyBadge(
    currency: CurrencyCode,
    selected: Boolean = false,
) = Text(
    text = currency.isoCode,
    modifier = Modifier
        .clip(RoundedCornerShape(FinanceRadius.small))
        .background(if (selected) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.12f))
        .padding(horizontal = FinanceSpacing.sm, vertical = FinanceSpacing.xs),
    color = Color.White,
    style = MaterialTheme.typography.labelMedium,
    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
)

@Composable
fun BalanceHeroCard(
    hidden: Boolean,
    onToggleVisibility: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(FinanceRadius.extraLarge),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = FinanceSpacing.xs),
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF4B249C),
                            FinanceColor.PrimaryDark,
                            FinanceColor.Primary,
                        ),
                    ),
                )
                .padding(FinanceSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(FinanceSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    stringResource(R.string.personal_balance),
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelLarge,
                )
                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier
                        .size(FinanceIconSize.touchTarget)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f)),
                ) {
                    Icon(
                        imageVector = if (hidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = stringResource(if (hidden) R.string.show_amount else R.string.hide_amount),
                        tint = Color.White,
                    )
                }
            }
            HiddenMoneyText(
                money = Money(0, CurrencyCode.ILS),
                hidden = hidden,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) {
                CurrencyBadge(CurrencyCode.ILS, selected = true)
                CurrencyBadge(CurrencyCode.USD)
            }
        }
    }
}

@Composable
fun MoneySummaryCard(
    label: String,
    money: Money,
    hidden: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) = Card(
    modifier = modifier,
    shape = RoundedCornerShape(FinanceRadius.large),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
) {
    Column(
        modifier = Modifier.padding(FinanceSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FinanceSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        HiddenMoneyText(money, hidden)
    }
}

@Composable
fun FinanceSectionHeader(title: String) = Text(
    title,
    style = MaterialTheme.typography.titleLarge,
    fontWeight = FontWeight.SemiBold,
)

@Composable
fun PersonAvatar(
    initials: String,
    modifier: Modifier = Modifier,
) = Box(
    modifier = modifier
        .size(FinanceIconSize.touchTarget)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer),
    contentAlignment = Alignment.Center,
) { Text(initials.take(2).uppercase()) }

@Composable
fun EmptyState(
    title: String,
    detail: String,
    icon: ImageVector = Icons.Default.Info,
    modifier: Modifier = Modifier,
) = Column(
    modifier = modifier
        .fillMaxWidth()
        .padding(vertical = FinanceSpacing.xl, horizontal = FinanceSpacing.lg)
        .semantics { contentDescription = title },
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(FinanceSpacing.md),
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    Text(
        detail,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun ErrorState() = EmptyState(stringResource(R.string.error), stringResource(R.string.error))

@Composable
fun LoadingSkeleton() = Text(stringResource(R.string.loading))

@Composable
fun InlineWarning() = Text("⚠ ${stringResource(R.string.warning)}")

@Composable
fun SuccessBanner() = Text("✓ ${stringResource(R.string.success)}")

@Composable
fun ConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) = AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.confirm)) },
    confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.confirm)) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
)

@Composable
fun FinanceFloatingActionButton(onClick: () -> Unit) {
    val quickActions = stringResource(R.string.quick_actions)
    FloatingActionButton(
        onClick = onClick,
        modifier =
            Modifier
                .testTag("quick_actions_fab")
                .semantics { contentDescription = quickActions },
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
    }
}

@Composable
fun FinanceTopAppBar(title: String) = TopAppBar(title = { Text(title) })

@Composable
fun FinanceScaffold(
    title: String,
    bottomBar: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) = Scaffold(
    topBar = { FinanceTopAppBar(title) },
    bottomBar = bottomBar,
    floatingActionButton = floatingActionButton,
    content = content,
)

enum class QuickAction(
    @StringRes val label: Int,
) {
    ADD_TRANSACTION(R.string.add_transaction),
    ADD_PERSON(R.string.add_person),
    ADD_INCOME(R.string.add_income),
    ADD_EXPENSE(R.string.add_expense),
    ADD_LOAN(R.string.add_loan),
    RECORD_REPAYMENT(R.string.record_repayment),
    ADD_SAVINGS(R.string.add_savings_contribution),
    IMPORT_STATEMENT(R.string.import_statement),
}

@Composable
fun QuickActionSheet(
    onDismiss: () -> Unit,
    onAction: (QuickAction) -> Unit,
) = ModalBottomSheet(onDismissRequest = onDismiss) {
    Column(
        modifier = Modifier
            .testTag("quick_action_sheet")
            .fillMaxWidth()
            .padding(horizontal = FinanceSpacing.md, vertical = FinanceSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(FinanceSpacing.xs),
    ) {
        FinanceSectionHeader(stringResource(R.string.quick_actions))
        QuickAction.entries.forEach { action ->
            Text(
                text = stringResource(action.label),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = FinanceIconSize.touchTarget)
                    .clip(RoundedCornerShape(FinanceRadius.medium))
                    .clickable { onAction(action) }
                    .padding(horizontal = FinanceSpacing.md, vertical = FinanceSpacing.sm),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (action != QuickAction.entries.last()) {
                HorizontalDivider()
            }
        }
    }
}
