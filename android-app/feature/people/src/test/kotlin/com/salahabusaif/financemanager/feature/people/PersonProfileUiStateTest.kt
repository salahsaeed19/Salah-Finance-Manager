package com.salahabusaif.financemanager.feature.people

import com.salahabusaif.financemanager.core.ledger.PersonOperation
import com.salahabusaif.financemanager.core.ledger.PersonOperationType
import com.salahabusaif.financemanager.core.model.CurrencyCode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonProfileUiStateTest {
    @Test
    fun `initial loading never renders the empty activity state`() {
        assertFalse(PersonProfileUiState(isInitialLoading = true).shouldShowEmptyRecentActivity)
    }

    @Test
    fun `stable empty activity list renders the empty state only after loading`() {
        assertTrue(PersonProfileUiState().shouldShowEmptyRecentActivity)
    }

    @Test
    fun `non empty activity remains non empty across an identical refresh`() {
        val activity = PersonOperation(
            id = "operation-1",
            transactionId = "transaction-1",
            type = PersonOperationType.DEPOSIT,
            currency = CurrencyCode.ILS,
            amountMinor = 100,
            commissionMinor = 0,
            fundsHeldChargedMinor = 0,
            financialAccountId = "account-1",
            beneficiaryName = null,
            dueDate = null,
            notes = null,
            occurredAt = 1,
        )

        val rendered = PersonProfileUiState(recentActivities = listOf(activity))
        val refreshed = rendered.copy(recentActivities = listOf(activity))

        assertFalse(rendered.shouldShowEmptyRecentActivity)
        assertFalse(refreshed.shouldShowEmptyRecentActivity)
    }
}
