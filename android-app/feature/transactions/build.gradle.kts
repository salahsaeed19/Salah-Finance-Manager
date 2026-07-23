plugins { id("salah.android.compose.library"); alias(libs.plugins.kotlin.compose) }
android { namespace = "com.salahabusaif.financemanager.feature.transactions" }
dependencies { implementation(platform(libs.compose.bom)); implementation(libs.material3); implementation(libs.compose.ui); implementation(libs.compose.material.icons.extended); implementation(project(":core:designsystem")) }
