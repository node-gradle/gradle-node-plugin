package com.github.gradle

import org.gradle.util.GradleVersion

class GradleVersionsForTest {
    private static final GradleVersion MINIMUM_SUPPORTED_GRADLE_VERSION = GradleVersion.version("6.1")
    private static final GradleVersion CURRENT_GRADLE_VERSION = GradleVersion.current()
    private static final GradleVersion GRADLE_7_VERSION = GradleVersion.version("7.0")
    // Contains changes to configuration cache
    private static final GradleVersion GRADLE_74_VERSION = GradleVersion.version("7.4")
    private static final GradleVersion[] GRADLE_VERSIONS =
            [MINIMUM_SUPPORTED_GRADLE_VERSION, CURRENT_GRADLE_VERSION, GRADLE_7_VERSION,
    GRADLE_74_VERSION]

    static GradleVersion[] computeCandidateGradleVersions() {
        def versions = [CURRENT_GRADLE_VERSION]
        if (System.getProperty("testAllSupportedGradleVersions").equals("true")) {
            return GRADLE_VERSIONS
        }
        if (System.getProperty("testMinimumSupportedGradleVersion").equals("true")) {
            versions.add(MINIMUM_SUPPORTED_GRADLE_VERSION)
        }
        if (System.getProperty("testCurrentGradleVersion").equals("false")) {
            versions.remove(CURRENT_GRADLE_VERSION)
        }
        if (System.getProperty("testSpecificGradleVersion") != "false") {
            versions.add(GradleVersion.version(System.getProperty("testSpecificGradleVersion")))
        }

        return versions
    }
}
