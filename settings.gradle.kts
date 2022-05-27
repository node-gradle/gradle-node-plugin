plugins {
    id("com.gradle.enterprise") version("3.10.1")
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        if (System.getenv().containsKey("CI")) {
            termsOfServiceAgree = "yes"
        }
    }
}

rootProject.name = "gradle-node-plugin"
