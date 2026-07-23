plugins { id("salah.jvm.library") }

dependencies { implementation(project(":core:model")) }

dependencies { testImplementation(libs.junit) }
