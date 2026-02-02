plugins {
    id("com.gradle.develocity") version("4.3.2")
    id("com.gradle.common-custom-user-data-gradle-plugin") version("2.3")
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.9.0")
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
            resourceUsage.set(isCI)
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
