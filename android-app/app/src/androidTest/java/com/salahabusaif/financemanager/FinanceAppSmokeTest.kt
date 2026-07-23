package com.salahabusaif.financemanager

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FinanceAppSmokeTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun allDestinationsAndQuickActionsAreReachable() {
        listOf("home", "transactions", "people", "plans", "settings").forEach { destination ->
            composeRule.onNodeWithTag("nav_$destination").assertIsDisplayed().performClick()
        }

        composeRule.onNodeWithTag("nav_home").performClick()
        composeRule.onNodeWithTag("quick_actions_fab").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("quick_action_sheet").assertIsDisplayed()
    }
}
