plugins { id("salah.jvm.library") }

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:money"))
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
}
