package com.salahabusaif.financemanager.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import com.salahabusaif.financemanager.core.model.AppTheme

object FinanceColor {
    val Primary = Color(0xFF7C4DFF)
    val PrimaryDark = Color(0xFF5B2FD6)
    val PrimaryLight = Color(0xFFB49CFF)
    val Background = Color(0xFFF7F7FA)
    val SurfaceVariant = Color(0xFFF0EFF5)
    val Success = Color(0xFF21B573)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFE5484D)
}

@Composable
fun FinanceTheme(theme: AppTheme, content: @Composable () -> Unit) {
    val colors = when (theme) {
        AppTheme.DARK -> darkColorScheme(primary = FinanceColor.PrimaryLight, secondary = Color(0xFFFFA07A))
        AppTheme.SYSTEM -> if (isSystemInDarkTheme()) darkColorScheme(primary = FinanceColor.PrimaryLight) else lightColorScheme(
            primary = FinanceColor.Primary,
            secondary = Color(0xFFFF7043),
            background = FinanceColor.Background,
            surfaceVariant = FinanceColor.SurfaceVariant,
            error = FinanceColor.Error,
        )
        AppTheme.LIGHT -> lightColorScheme(
            primary = FinanceColor.Primary,
            secondary = Color(0xFFFF7043),
            background = FinanceColor.Background,
            surfaceVariant = FinanceColor.SurfaceVariant,
            error = FinanceColor.Error,
        )
    }
    MaterialTheme(colorScheme = colors, content = content)
}
