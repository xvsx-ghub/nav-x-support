import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.serialization)
}

val major = libs.versions.major.get().toInt()
val minor = libs.versions.minor.get().toInt()
val patch = libs.versions.patch.get().toInt()
val build = libs.versions.build.get().toInt()
val appName = "Nav X Support app"
val appVersionCode = major * 10000000 + minor * 100000 + patch * 1000 + build
val appVersionName = "$major.$minor.$patch.$build"
val gitHashProvider = providers.exec {
    commandLine("git", "rev-parse", "--short", "HEAD")
}.standardOutput.asText.map { it.trim() }.orElse("unknown")

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            // Required when using NativeSQLiteDriver
            linkerOpts.add("-lsqlite3")
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.accompanist.permissions)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.bundles.projectNavigation)
                implementation(libs.bundles.dependencyInjection)
                implementation(libs.bundles.localDataStore)
                implementation(libs.bundles.database)
                implementation(libs.bundles.httpComon)
                implementation(libs.bundles.stomp)
                implementation(libs.ui.backhandler)
                implementation(project(":qr-code-scanner"))
                implementation(project(":camera"))
                implementation(project(":drawing-pad"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.bundles.httpAndroid)
                implementation(libs.androidx.work.runtime.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.androidx.lifecycle.service)
                implementation(libs.androidx.lifecycle.process)
            }
        }
        val iosMain by creating {
            dependencies {
                implementation(libs.bundles.httpIos)
                implementation(compose.ui)
            }
        }
    }
}
android {
    namespace = "com.wiswm.nav.support"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    val signingProperties = Properties().apply {
        load(file("signing.properties").inputStream())
    }
    defaultConfig {
        applicationId = "com.wiswm.nav.support"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = appVersionCode
        versionName = appVersionName
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("config") {
            keyAlias = signingProperties["KEY_ALIAS"] as String
            keyPassword = signingProperties["KEY_PASSWORD"] as String
            storeFile = file(signingProperties["STORE_FILE"] as String)
            storePassword = signingProperties["STORE_PASSWORD"] as String
        }
    }
    buildTypes {
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.txt")
            signingConfig = signingConfigs.getByName("config")
        }
        create("alpha") {
            isDebuggable = true
            applicationIdSuffix = ".alpha"
            versionNameSuffix = gitHashProvider.map { ":$it-alpha" }.get()
            signingConfig = signingConfigs.getByName("config")
        }
        create("beta") {
            isDebuggable = true
            applicationIdSuffix = ".beta"
            versionNameSuffix = gitHashProvider.map { ":$it-beta" }.get()
            signingConfig = signingConfigs.getByName("config")
        }
    }
    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "${appName} ${appVersionName} ${this.name}.apk"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
dependencies {
    debugImplementation(compose.uiTooling)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}
room {
    schemaDirectory("$projectDir/schemas")
}