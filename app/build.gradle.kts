import java.util.Properties

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.compose)
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

android {
	namespace = "com.catalent.onehub"
	compileSdk = 36

	defaultConfig {
		applicationId = "com.catalent.onehub"
		minSdk = 32
		targetSdk = 36
		versionCode = 1
		versionName = "1.0.0"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

		// default BuildConfig fields
		buildConfigField("String", "SALESFORCE_LOGIN_HOST", "\"login.salesforce.com\"")

		// Salesforce secrets injected as resources
		resValue("string", "remoteAccessConsumerKey", sfConsumerKey)
		resValue("string", "oauthRedirectURI", sfRedirectUri)
	}

	buildTypes {
		debug {
			applicationIdSuffix = ".debug"
			versionNameSuffix = "-debug"
			isDebuggable = true
			isMinifyEnabled = false
			buildConfigField("boolean", "ENABLE_CONSOLE_LOGGING", "true")
			buildConfigField("String", "SALESFORCE_LOGIN_HOST", "\"test.salesforce.com\"")
		}
		release {
			isDebuggable = false
			isMinifyEnabled = true
			isShrinkResources = true
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
			buildConfigField("boolean", "ENABLE_CONSOLE_LOGGING", "false")
			buildConfigField("String", "SALESFORCE_LOGIN_HOST", "\"login.salesforce.com\"")
			signingConfig = signingConfigs.getByName("debug")
		}
	}

	flavorDimensions += "environment"

	productFlavors {
		create("dev") {
			dimension = "environment"
			applicationIdSuffix = ".dev"
			versionNameSuffix = "-dev"
			buildConfigField("String", "SALESFORCE_LOGIN_HOST", "\"test.salesforce.com\"")
			buildConfigField("boolean", "ENABLE_CONSOLE_LOGGING", "true")
			resValue("string", "app_name", "OneHub Dev")
		}
		create("staging") {
			dimension = "environment"
			applicationIdSuffix = ".staging"
			versionNameSuffix = "-staging"
			buildConfigField("String", "SALESFORCE_LOGIN_HOST", "\"test.salesforce.com\"")
			buildConfigField("boolean", "ENABLE_CONSOLE_LOGGING", "true")
			resValue("string", "app_name", "OneHub Staging")
		}
		create("prod") {
			dimension = "environment"
			buildConfigField("String", "SALESFORCE_LOGIN_HOST", "\"login.salesforce.com\"")
			buildConfigField("boolean", "ENABLE_CONSOLE_LOGGING", "false")
			resValue("string", "app_name", "OneHub")
		}
	}

	buildFeatures {
		compose = true
		buildConfig = true
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}

	kotlinOptions { jvmTarget = "17" }

	lint {
		// errors that should break the build
		error += setOf(
			"HardcodedText",         // no hardcoded strings in layouts
			"UnusedResources",       // no dead resources
			"PrivateResource",       // no accessing private resources
		)

		// warnings — review but don't block
		warning += setOf(
			"ObsoleteSdkInt",        // minSdk checks that are always true
			"MissingTranslation",    // missing string translations
		)

		// ignore — intentional
		disable += setOf(
			"GoogleAppIndexingWarning", // no app indexing needed
		)

		checkReleaseBuilds = true
		abortOnError = false         // ← change to true before prod release
		warningsAsErrors = false
		checkDependencies = true
		htmlReport = true
		xmlReport = true
		htmlOutput = file("${rootProject.projectDir}/reports/lint/app-lint-report.html")
		xmlOutput = file("${rootProject.projectDir}/reports/lint/app-lint-report.xml")
	}
}

dependencies {
	// Core Android
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.lifecycle.runtime.compose)
	implementation(libs.androidx.activity.compose)
	implementation(libs.androidx.biometric)

	// Compose
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.compose.material3)
	implementation(libs.androidx.compose.material)
	implementation(libs.androidx.compose.material.icons.extended)
	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.ui.test.manifest)

	// Material
	implementation(libs.android.material)

	// Hilt
	implementation(libs.hilt.android)
	ksp(libs.hilt.compiler)
	implementation(libs.hilt.navigation.compose)
	implementation(libs.androidx.hilt.work)
	ksp(libs.androidx.hilt.compiler)

	// WorkManager
	implementation(libs.androidx.work.runtime.ktx)

	// Salesforce
	implementation(libs.salesforce.mobile.sync)

	// Internal modules
	implementation(projects.core)

	implementation(projects.auth.domain)
	implementation(projects.auth.ui)
	runtimeOnly(projects.auth.data)

	implementation(projects.shipment.domain)
	implementation(projects.shipment.ui)
	runtimeOnly(projects.shipment.data)

	// Test
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}