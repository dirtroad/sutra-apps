pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_NOT_SUPPORTED)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SutraApps"
include ':app'