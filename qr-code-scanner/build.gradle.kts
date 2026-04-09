plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.2.20" // Kotlin
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" // Compose plugin
    id("com.android.library") version "8.13.0" // Android Gradle Plugin
    id("org.jetbrains.compose") version "1.9.0" // Compose Multiplatform
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }

    androidTarget()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.20")

                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:2.9.4")
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
            }
        }

        val androidMain by getting {
            dependencies {
                implementation("com.google.accompanist:accompanist-permissions:0.37.3")
                implementation("androidx.camera:camera-camera2:1.5.0")
                implementation("androidx.camera:camera-lifecycle:1.5.0")
                implementation("androidx.camera:camera-view:1.5.0")
                implementation("com.google.mlkit:barcode-scanning:17.3.0")
            }
        }
    }
}

android {
    namespace = "org.sample.library"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release")
        create("alpha")
        create("beta")
    }
}