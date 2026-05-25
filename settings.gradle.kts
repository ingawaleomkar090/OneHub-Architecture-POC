pluginManagement {
//	includeBuild("build-logic") // future use for convention plugins
	repositories {
		google {
			content {
				includeGroupByRegex("com\\.android.*")
				includeGroupByRegex("com\\.google.*")
				includeGroupByRegex("androidx.*")
			}
		}
		mavenCentral()
		gradlePluginPortal()
	}
}
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		google()
		mavenCentral()
	}
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "CatalentOneHub"
include(":app")
include(":auth:data")
include(":auth:ui")
include(":auth:domain")

include(":core")
include(":shipment:data")
include(":shipment:ui")
include(":shipment:domain")
