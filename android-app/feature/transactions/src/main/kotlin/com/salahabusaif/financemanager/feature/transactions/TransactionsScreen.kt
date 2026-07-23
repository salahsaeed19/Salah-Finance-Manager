package com.salahabusaif.financemanager.feature.transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.salahabusaif.financemanager.core.designsystem.EmptyState
import com.salahabusaif.financemanager.core.designsystem.FinanceSpacing
import com.salahabusaif.financemanager.core.designsystem.R

@Composable
fun TransactionsScreen() = Column(Modifier.fillMaxSize().padding(FinanceSpacing.md)) {
    EmptyState(
        stringResource(R.string.no_transactions),
        stringResource(R.string.no_transactions_detail),
        Icons.AutoMirrored.Filled.ReceiptLong,
    )
}
