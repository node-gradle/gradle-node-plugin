package com.github.gradle.buildlogic

import groovy.json.JsonSlurper
import org.semver4j.Semver

// From https://github.com/gradle/test-retry-gradle-plugin
class GradleVersionData {

    static List<String> getNightlyVersions() {
        def releaseNightly = getLatestReleaseNightly()
        releaseNightly ? [releaseNightly] + getLatestNightly() : [getLatestNightly()]
    }

    private static String getLatestNightly() {
        new JsonSlurper().parse(new URL("https://services.gradle.org/versions/nightly")).version
    }

    private static String getLatestReleaseNightly() {
        new JsonSlurper().parse(new URL("https://services.gradle.org/versions/release-nightly")).version
    }

    static List<String> getReleasedVersions() {
        new JsonSlurper().parse(new URL("https://services.gradle.org/versions/all"))
            .findAll { !it.nightly && !it.snapshot } // filter out snapshots and nightlies
            .findAll { !it.rcFor || it.activeRc } // filter out inactive rcs
            .findAll { !it.milestoneFor } // filter out milestones
            .collect { Semver.parse(it.version as String) }
            .findAll { it != null }
            .findAll { it.isGreaterThanOrEqualTo("6.9.0") } // Only test specific versions
            .inject([] as List<Map.Entry<String, Semver>>) { releasesToTest, version -> // only test against latest patch versions
                if (!releasesToTest.any { it.major == version.major && it.minor == version.minor }) {
                    releasesToTest + version
                } else {
                    releasesToTest
                }
            }
            .collect { it.toString() }
    }

}
