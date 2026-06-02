plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.hilt)
	alias(libs.plugins.ksp)
}

android {
	namespace = "com.catalent.auth.ui"
	compileSdk = 37

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
}

kotlin {
	compilerOptions {
		jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
	}
}

dependencies {
	// Modules
	implementation(projects.auth.domain)
	implementation(projects.core)

	// Hilt
	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)
	implementation(libs.hilt.navigation.compose)

	// Lifecycle
	implementation(libs.androidx.lifecycle.runtime.ktx)

	// Salesforce
	implementation(libs.salesforce.mobile.sync)

	// Compose
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.compose.ui.tooling.preview)
	debugImplementation(libs.androidx.compose.ui.tooling)

	// Test
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
}