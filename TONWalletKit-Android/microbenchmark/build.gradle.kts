plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.benchmark)
}

android {
    namespace = "io.ton.walletkit.microbenchmark"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
    }

    // Non-debuggable build required; the flag lives in src/androidTest/AndroidManifest.xml.
    testBuildType = "release"
    buildTypes {
        release {
            isDefault = true
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
    }
}

dependencies {
    androidTestImplementation(project(":api"))
    androidTestImplementation(libs.androidxBenchmarkJunit4)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidxTestExt)
    androidTestImplementation(libs.androidxTestRunner)
}
