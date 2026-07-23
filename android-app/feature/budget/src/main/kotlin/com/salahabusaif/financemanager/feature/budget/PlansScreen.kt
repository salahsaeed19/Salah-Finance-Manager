package com.salahabusaif.financemanager.feature.budget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.salahabusaif.financemanager.core.designsystem.EmptyState
import com.salahabusaif.financemanager.core.designsystem.FinanceSpacing
import com.salahabusaif.financemanager.core.designsystem.R

@Composable
fun PlansScreen() = Column(Modifier.fillMaxSize().padding(FinanceSpacing.md)) {
    EmptyState(
        stringResource(R.string.no_plan),
        stringResource(R.string.no_plan_detail),
        Icons.Default.Savings,
    )
}
