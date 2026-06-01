plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.catalent.shipment.data"
    compileSdk = 36
    defaultConfig { minSdk = 32 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    // Modules
    implementation(projects.shipment.domain)
    implementation(projects.core)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Salesforce
    implementation(libs.salesforce.mobile.sync)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
}