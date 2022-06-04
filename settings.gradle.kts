plugins {
    id("com.gradle.enterprise") version("3.10.1")
}

addScanProperty("testAllSupportedGradleVersions")
addScanProperty("testMinimumSupportedGradleVersion")
addScanProperty("testCurrentGradleVersion", "true")
addScanProperty("testSpecificGradleVersion")

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        if (System.getenv().containsKey("CI")) {
            termsOfServiceAgree = "yes"
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