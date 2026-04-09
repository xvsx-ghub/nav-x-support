rootProject.name = "NavXSupportApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }

    versionCatalogs {
        create("cameraLibs") {
            from(files("camera/libs.versions.toml"))
        }
    }

    versionCatalogs {
        create("drawingPadLibs") {
            from(files("drawing-pad/libs.versions.toml"))
        }
    }
}

include(":composeApp")
include(":qr-code-scanner")
include(":camera")
include(":drawing-pad")
