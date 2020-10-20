package com.github.gradle

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.regex.Pattern

@RunWithMultipleGradleVersions
abstract class AbstractIntegTest extends Specification {
    @Rule
    final TemporaryFolder temporaryFolder = new TemporaryFolder()
    File projectDir
    File buildFile
    GradleVersion gradleVersion = null

    def setup(GradleVersion gradleVersion) {
        this.gradleVersion = gradleVersion
        projectDir = temporaryFolder.root.toPath().toRealPath().toFile()
        buildFile = createFile('build.gradle')
    }

    protected final GradleRunner newRunner(final String... args) {
        return GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(args)
                .withPluginClasspath()
                .forwardOutput()
                .withGradleVersion(gradleVersion.version)
    }

    protected final BuildResult build(final String... args) {
        return newRunner(args).build()
    }

    protected final BuildResult buildWithEnvironment(Map<String, String> env, final String... args) {
        return newRunner(args)
                .withEnvironment(env)
                .build()
    }

    protected final BuildResult buildAndFail(final String... args) {
        return newRunner(args).buildAndFail()
    }

    protected final BuildTask buildTask(final String task) {
        return build(task).task(':' + task)
    }

    protected final File createFile(final String name) {
        return new File(temporaryFolder.getRoot(), name)
    }

    protected final void writeFile(final String name, final String text) {
        File file = createFile(name)
        file.parentFile.mkdirs()
        file << text
    }

    protected final void writePackageJson(final String text) {
        writeFile('package.json', text)
    }

    protected final void writeEmptyPackageJson() {
        writePackageJson(""" {
            "name": "example",
            "dependencies": {}
        }
        """)
    }

    protected final void writeBuild(final String text) {
        buildFile << text
    }

    protected void copyResources(String source, String destination = "") {
        ClassLoader classLoader = getClass().getClassLoader()
        URL resource = classLoader.getResource(source)
        if (resource == null) {
            throw new RuntimeException("Could not find classpath resource: $source")
        }

        File resourceFile = new File(resource.toURI())
        if (resourceFile.file) {
            File destinationFile = file(destination)
            FileUtils.copyFile(resourceFile, destinationFile)
        } else {
            def destinationDir = directory(destination)
            FileUtils.copyDirectory(resourceFile, destinationDir)
        }
    }

    protected final File file(String path, File baseDir = getProjectDir()) {
        def splitted = path.split('/')
        def directory = splitted.size() > 1 ? directory(splitted[0..-2].join('/'), baseDir) : baseDir
        def file = new File(directory, splitted[-1])
        file.createNewFile()
        return file
    }

    protected final File directory(String path, File baseDir = getProjectDir()) {
        return new File(baseDir, path).with {
            mkdirs()
            it
        }
    }

    protected final boolean fileExists(String path) {
        return new File(projectDir, path).exists()
    }

    protected boolean environmentDumpContainsPathVariable(environmentDump) {
        // Sometimes the PATH variable is not defined in Windows Powershell, but the PATHEXT is
        return Pattern.compile("^PATH(?:EXT)?=.+\$", Pattern.MULTILINE).matcher(environmentDump).find()
    }
}
