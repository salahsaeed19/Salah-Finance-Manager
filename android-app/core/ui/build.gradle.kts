plugins {
    id("salah.android.compose.library")
    alias(libs.plugins.kotlin.compose)
}

android { namespace = "com.salahabusaif.financemanager.core.ui" }

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(project(":core:designsystem"))
}
