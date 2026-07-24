package com.salahabusaif.financemanager.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.salahabusaif.financemanager.core.designsystem.FinanceSectionHeader
import com.salahabusaif.financemanager.core.designsystem.FinanceSpacing
import com.salahabusaif.financemanager.core.designsystem.R
import com.salahabusaif.financemanager.core.data.profile.OwnerProfile
import com.salahabusaif.financemanager.core.model.AppLanguage
import com.salahabusaif.financemanager.core.model.AppTheme

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val preferences by viewModel.preferences.collectAsState()
    val ownerProfile by viewModel.ownerProfile.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(FinanceSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FinanceSpacing.lg),
    ) {
        SettingsSection(title = stringResource(R.string.language)) {
            Choice(
                stringResource(R.string.arabic),
                preferences.language == AppLanguage.ARABIC,
            ) { viewModel.setLanguage(AppLanguage.ARABIC) }
            HorizontalDivider()
            Choice(
                stringResource(R.string.english),
                preferences.language == AppLanguage.ENGLISH,
            ) { viewModel.setLanguage(AppLanguage.ENGLISH) }
        }
        SettingsSection(title = stringResource(R.string.theme)) {
            Choice(stringResource(R.string.system), preferences.theme == AppTheme.SYSTEM) {
                viewModel.setTheme(AppTheme.SYSTEM)
            }
            HorizontalDivider()
            Choice(stringResource(R.string.light), preferences.theme == AppTheme.LIGHT) {
                viewModel.setTheme(AppTheme.LIGHT)
            }
            HorizontalDivider()
            Choice(stringResource(R.string.dark), preferences.theme == AppTheme.DARK) {
                viewModel.setTheme(AppTheme.DARK)
            }
        }
        SettingsSection(title = stringResource(R.string.privacy)) {
            SettingRow(
                title = stringResource(R.string.hide_amounts),
                detail = stringResource(R.string.hide_amounts_detail),
                control = {
                    Switch(
                        checked = preferences.hideAmounts,
                        onCheckedChange = viewModel::setHideAmounts,
                    )
                },
                onClick = { viewModel.setHideAmounts(!preferences.hideAmounts) },
            )
        }
        OwnerProfileSection(ownerProfile, viewModel::updateOwnerProfile)
        SettingsSection(title = stringResource(R.string.security)) {
            InformationalRow(stringResource(R.string.security_detail))
        }
        SettingsSection(title = stringResource(R.string.data)) {
            InformationalRow(stringResource(R.string.data_detail))
        }
        Text(
            "${stringResource(R.string.app_version)} ${stringResource(R.string.app_version_value)}",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OwnerProfileSection(profile: OwnerProfile, onSave: (OwnerProfile) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    LaunchedEffect(profile) {
        name = profile.fullName
        phone = profile.phoneNumber
        reference = profile.bankOfPalestineReference
    }
    SettingsSection(title = stringResource(R.string.owner_profile)) {
        Column(Modifier.padding(FinanceSpacing.md), verticalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) {
            Text(stringResource(R.string.owner_profile_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.personal_owner), style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.account_holder)) }, singleLine = true)
            OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.phone_identifier)) }, singleLine = true)
            OutlinedTextField(reference, { reference = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.bank_reference)) }, singleLine = true)
            Button(onClick = { onSave(OwnerProfile(name, phone, reference)) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.save_profile))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FinanceSpacing.sm)) {
        FinanceSectionHeader(title)
        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(
                com.salahabusaif.financemanager.core.designsystem.FinanceRadius.large,
            ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            content()
        }
    }
}

@Composable
private fun Choice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = FinanceSpacing.sm, vertical = FinanceSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, modifier = Modifier.padding(start = FinanceSpacing.sm), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SettingRow(
    title: String,
    detail: String,
    control: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(FinanceSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(FinanceSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(FinanceSpacing.xs)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        control()
    }
}

@Composable
private fun InformationalRow(detail: String) = Text(
    detail,
    modifier = Modifier.padding(FinanceSpacing.md),
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
)
