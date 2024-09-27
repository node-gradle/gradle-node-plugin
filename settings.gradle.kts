plugins {
    id("com.gradle.develocity") version("3.18.1")
    id("com.gradle.common-custom-user-data-gradle-plugin") version("2.0.2")
}

val isCI = System.getenv().containsKey("CI")
val isPR = isCI && System.getenv().containsKey("GRADLE_ENTERPRISE_ACCESS_KEY")

val publishAlwaysIf = System.getProperties()["user.name"] == "deepy"

develocity {
    server.set("https://alexandernordlund.gradle-enterprise.cloud/")
    buildScan {
        publishing {
            onlyIf { it.isAuthenticated }
        }
        uploadInBackground.set(!isCI)

        capture {
            fileFingerprints.set(publishAlwaysIf || isPR)
        }

        obfuscation {
            ipAddresses { addresses -> addresses.map { _ -> "0.0.0.0"} }
            if (!isCI) {
                externalProcessName { processName -> "non-build-process" }
            }
        }
    }
}


rootProject.name = "gradle-node-plugin"
