pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SalahFinanceManager"
includeBuild("build-logic")

include(":app")
include(":core:common", ":core:model", ":core:money", ":core:ledger")
include(":core:database", ":core:data", ":core:designsystem", ":core:ui", ":core:files", ":core:security", ":core:testing")
include(":feature:dashboard", ":feature:accounts", ":feature:people", ":feature:transactions", ":feature:import", ":feature:review", ":feature:reconciliation", ":feature:budget", ":feature:savings", ":feature:reports", ":feature:settings")
