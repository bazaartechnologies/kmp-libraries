import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(mycatalog.plugins.kotlin.multiplatform)
    alias(mycatalog.plugins.android.application)
    alias(mycatalog.plugins.compose.multiplatform)
    alias(mycatalog.plugins.compose.compiler)
    alias(mycatalog.plugins.kotlin.serialization)
    alias(mycatalog.plugins.native.couroutine)
}

kotlin {
    val frameworkName = "KmpAppCommon"
    val xcFramework = XCFramework(frameworkName)

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = frameworkName
            binaryOption("bundleId", group.toString())
            binaryOption("bundleVersion", version.toString())
            isStatic = true
            freeCompilerArgs += listOf("-g")
            debuggable = true
            xcFramework.add(this)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(mycatalog.androidx.compose.ui.tooling.preview)
            implementation(mycatalog.androidx.activity.compose)
            implementation(mycatalog.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(mycatalog.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(project(":network"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(mycatalog.ktor.client.core)
            implementation(mycatalog.ktor.client.content.negotiation)
            implementation(mycatalog.ktor.serialization.kotlinx.json)

            implementation(mycatalog.coil.compose)
            implementation(mycatalog.coil.network.ktor)
            implementation(mycatalog.koin.core)
            implementation(mycatalog.koin.compose.viewmodel)
            implementation(mycatalog.navigation.compose)
            implementation(mycatalog.shared.viewmodel)

        }
    }
}

dependencies {
    implementation(mycatalog.appcompat)
    implementation(mycatalog.androidx.core)
    debugImplementation(mycatalog.androidx.compose.ui.tooling)
}

android {
    namespace = "com.tech.bazaar.kmp.app"
    compileSdk = 35
    defaultConfig {
        minSdk = 21
        targetSdk = 35
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
