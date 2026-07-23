plugins {
    id("salah.android.library")
    alias(libs.plugins.ksp)
}

android { namespace = "com.salahabusaif.financemanager.core.database" }

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    testImplementation(libs.junit)
    testImplementation(libs.room.testing)
}
