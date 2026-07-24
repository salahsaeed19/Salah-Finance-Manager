import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.spotless)
}

android {
    namespace = "com.salahabusaif.financemanager"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.salahabusaif.financemanager"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "0.5.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true; buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint { abortOnError = true }

    sourceSets.getByName("main").assets.srcDir(layout.buildDirectory.dir("generated/privateOwnerProfile/assets"))
}

val privateOwnerProfile = rootProject.projectDir.parentFile.resolve("private-config/salah-profile.properties")
val copyPrivateOwnerProfile = tasks.register<Copy>("copyPrivateOwnerProfile") {
    onlyIf { privateOwnerProfile.isFile }
    from(privateOwnerProfile)
    into(layout.buildDirectory.dir("generated/privateOwnerProfile/assets"))
    rename { "owner-profile.properties" }
}

tasks.named("preBuild").configure { dependsOn(copyPrivateOwnerProfile) }

tasks.withType<KotlinCompile>().configureEach { compilerOptions.jvmTarget.set(JvmTarget.JVM_17) }

spotless {
    kotlin {
        target("**/*.kt")
        ktlint()
    }
    format("misc") {
        target("**/*.gradle.kts", "**/*.md", "**/*.xml", "**/*.toml", "**/*.yml")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.work.runtime)
    implementation(libs.work.hilt)
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:accounts"))
    implementation(project(":feature:people"))
    implementation(project(":feature:transactions"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:budget"))
    ksp(libs.hilt.compiler)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
