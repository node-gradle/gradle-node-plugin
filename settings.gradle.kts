plugins {
    id("com.gradle.enterprise") version("3.12.1")
    id("com.gradle.common-custom-user-data-gradle-plugin") version("1.8.2")
}

val isCI = System.getenv().containsKey("CI")

gradleEnterprise {
    buildScan {
        if (!isCI) {
            server = "https://alexandernordlund.gradle-enterprise.cloud/"
        }
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        if (isCI) {
            termsOfServiceAgree = "yes"
        }
        publishAlwaysIf(System.getProperties()["user.name"] == "deepy")

        isUploadInBackground = !isCI
        obfuscation {
            ipAddresses { addresses -> addresses.map { _ -> "0.0.0.0"} }
        }
    }
}

rootProject.name = "gradle-node-plugin"
