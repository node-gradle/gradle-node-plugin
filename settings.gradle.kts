plugins {
    id("com.gradle.enterprise") version("3.10.3")
    id("com.gradle.common-custom-user-data-gradle-plugin") version("1.7.2")
    id("com.gradle.enterprise.test-distribution") version "2.3.5"
}

addScanProperty("testAllSupportedGradleVersions")
addScanProperty("testMinimumSupportedGradleVersion")
addScanProperty("testCurrentGradleVersion", "true")
addScanProperty("testSpecificGradleVersion")

val isCI = System.getenv().containsKey("CI")
val isPR = isCI && !System.getenv().containsKey("GRADLE_ENTERPRISE_ACCESS_KEY")

gradleEnterprise {
    buildScan {
        if (!isPR) {
            server = "https://alexandernordlund.gradle-enterprise.cloud/"
        }
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        if (isCI) {
            termsOfServiceAgree = "yes"
        }
        publishAlways()

        isUploadInBackground = !isCI
        obfuscation {
            ipAddresses { addresses -> addresses.map { _ -> "0.0.0.0"} }
        }
    }
}

rootProject.name = "gradle-node-plugin"


fun addScanProperty(name: String, default: String? = null) {
    val property = extra.properties[name] as String?
    if (property != null) {
        if (default == null || default != property) {
            gradleEnterprise.buildScan.value(name, property)
        }
    }
}