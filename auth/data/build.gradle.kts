plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.hilt)
	alias(libs.plugins.ksp)
	alias(libs.plugins.kotlin.android)
}

android {
	namespace = "com.catalent.auth.data"
	compileSdk = 36

	defaultConfig {
		minSdk = 32

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	kotlinOptions {
		jvmTarget = "17"
	}
}

dependencies {
	// Core
	implementation(projects.auth.domain)
	implementation(projects.core)

	// Salesforce
	implementation(libs.salesforce.mobile.sync)

	// Coroutines
	implementation(libs.kotlinx.coroutines.core)

	// Hilt
	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)
}