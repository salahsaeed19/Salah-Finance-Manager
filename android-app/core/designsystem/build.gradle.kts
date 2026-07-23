plugins {
    id("salah.android.compose.library")
    alias(libs.plugins.kotlin.compose)
}

android { namespace = "com.salahabusaif.financemanager.core.designsystem" }

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(project(":core:model"))
    implementation(project(":core:money"))
    debugImplementation(libs.compose.ui.tooling)
}
