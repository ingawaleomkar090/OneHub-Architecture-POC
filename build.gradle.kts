plugins {
	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.android.library) apply false
	alias(libs.plugins.kotlin.compose) apply false
	alias(libs.plugins.hilt) apply false
	alias(libs.plugins.ksp) apply false
	alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}

subprojects {
	plugins.withId("com.android.library") {
		extensions.configure<com.android.build.api.dsl.LibraryExtension> {
			defaultConfig {
				missingDimensionStrategy("environment", "dev")
			}
			lint {
				warningsAsErrors = false
				abortOnError = false
				checkDependencies = true
				xmlReport = true
				htmlReport = true
				xmlOutput = file("${rootProject.projectDir}/reports/lint/${project.name}-lint-report.xml")
				htmlOutput = file("${rootProject.projectDir}/reports/lint/${project.name}-lint-report.html")
			}
		}
	}

	plugins.withId("com.android.application") {
		extensions.configure<com.android.build.api.dsl.ApplicationExtension> {
			lint {
				warningsAsErrors = false
				abortOnError = false
				checkDependencies = true
				xmlReport = true
				htmlReport = true
				xmlOutput = file("${rootProject.projectDir}/reports/lint/app-lint-report.xml")
				htmlOutput = file("${rootProject.projectDir}/reports/lint/app-lint-report.html")
			}
		}
	}
}