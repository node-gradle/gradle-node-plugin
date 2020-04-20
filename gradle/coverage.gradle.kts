apply(plugin = "jacoco")

tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }
}
