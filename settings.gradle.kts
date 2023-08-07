plugins {
    id("com.gradle.enterprise") version("3.14.1")
    id("com.gradle.common-custom-user-data-gradle-plugin") version("1.11.1")
}

val isCI = System.getenv().containsKey("CI")
val isPR = isCI && System.getenv().containsKey("GRADLE_ENTERPRISE_ACCESS_KEY")

val publishAlwaysIf = System.getProperties()["user.name"] == "deepy"

gradleEnterprise {
    buildScan {
        if (publishAlwaysIf || isPR) {
            server = "https://alexandernordlund.gradle-enterprise.cloud/"
        }
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        if (isCI) {
            termsOfServiceAgree = "yes"
        }
        publishAlwaysIf(publishAlwaysIf)

        capture {
            isTaskInputFiles = publishAlwaysIf || isPR
        }
        isUploadInBackground = !isCI
        obfuscation {
            ipAddresses { addresses -> addresses.map { _ -> "0.0.0.0"} }
        }
    }
}

rootProject.name = "gradle-node-plugin"
