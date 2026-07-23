plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories { google(); mavenCentral(); gradlePluginPortal() }

dependencies {
    implementation("com.android.tools.build:gradle:8.8.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
}

gradlePlugin {
    plugins {
        register("salahAndroidLibrary") {
            id = "salah.android.library"
            implementationClass = "SalahAndroidLibraryPlugin"
        }
        register("salahAndroidComposeLibrary") {
            id = "salah.android.compose.library"
            implementationClass = "SalahComposeLibraryPlugin"
        }
        register("salahJvmLibrary") {
            id = "salah.jvm.library"
            implementationClass = "SalahJvmLibraryPlugin"
        }
    }
}
