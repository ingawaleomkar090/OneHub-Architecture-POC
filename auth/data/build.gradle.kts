import java.util.Properties

plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.hilt)
	alias(libs.plugins.ksp)
	alias(libs.plugins.kotlin.android)
}

val localProperties = Properties().apply {
	val localPropertiesFile = rootProject.file("local.properties")
	if (localPropertiesFile.exists()) {
		localPropertiesFile.inputStream().use { load(it) }
	}
}

val sfConsumerKey = localProperties.getProperty("salesforce.consumer.key")
	?: System.getenv("SALESFORCE_CONSUMER_KEY")
	?: ""
val sfRedirectUri = localProperties.getProperty("salesforce.redirect.uri")
	?: System.getenv("SALESFORCE_REDIRECT_URI")
	?: ""
val sfRedirectAuthUri = localProperties.getProperty("salesforce.redirect.authuri")
	?: System.getenv("SALESFORCE_REDIRECT_AUTHURI")
	?: ""
val sfBaseCommunityUrl = localProperties.getProperty("salesforce.base.url")
	?: System.getenv("SALESFORCE_COMMUNITY_BASE_URL")
	?: "login.salesforce.com"

android {
	namespace = "com.catalent.auth.data"
	compileSdk = 36

	buildFeatures {
		buildConfig = true
	}

	defaultConfig {
		minSdk = 32

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		consumerProguardFiles("consumer-rules.pro")

		buildConfigField("String", "SALESFORCE_CONSUMER_KEY", "\"$sfConsumerKey\"")
		buildConfigField("String", "SALESFORCE_REDIRECT_URI", "\"$sfRedirectUri\"")
		buildConfigField("String", "SALESFORCE_REDIRECT_AUTHURI", "\"$sfRedirectAuthUri\"")
		buildConfigField("String", "SALESFORCE_COMMUNITY_BASE_URL", "\"$sfBaseCommunityUrl\"")
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
	buildFeatures {
		buildConfig = true
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
