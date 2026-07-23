plugins { id("salah.android.compose.library"); alias(libs.plugins.kotlin.compose) }
android { namespace = "com.salahabusaif.financemanager.feature.accounts" }
dependencies { implementation(platform(libs.compose.bom)); implementation(libs.material3); implementation(libs.compose.ui); implementation(project(":core:designsystem")) }
