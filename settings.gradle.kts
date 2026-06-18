pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://api.xposed.info/")
        // LSPosed LibXposed API repository
        maven("https://jitpack.io")
    }
}

rootProject.name = "HyperOShape"
include(":Hook")
