package com.moowork.gradle.node.util;

import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;
import org.gradle.process.ExecSpec;

import javax.inject.Inject;
import java.io.File;

public abstract class ProjectApiHelper {

    public static ProjectApiHelper newInstance(Project project) {
        if (BackwardsCompat.enableConfigurationCache()) {
            return project.getObjects().newInstance(DefaultProjectApiHelper.class);
        }
        return new LegacyProjectApiHelper(project);
    }

    public abstract File getBuildDirectory();

    public abstract File file(String path);

    public abstract File file(File file);

    public abstract ExecResult exec(Action<ExecSpec> closure);
}

class DefaultProjectApiHelper extends ProjectApiHelper {

    private final ProjectLayout layout;

    private final ExecOperations execOperations;

    @Inject
    public DefaultProjectApiHelper(ProjectLayout layout, ExecOperations execOperations) {
        this.layout = layout;
        this.execOperations = execOperations;
    }

    @Override
    public File getBuildDirectory() {
        return layout.getBuildDirectory().get().getAsFile();
    }

    @Override
    public File file(String path) {
        return layout.getProjectDirectory().file(path).getAsFile();
    }

    @Override
    public File file(File file) {
        return file(file.getPath());
    }

    @Override
    public ExecResult exec(Action<ExecSpec> closure) {
        return execOperations.exec(closure);
    }
}

class LegacyProjectApiHelper extends ProjectApiHelper {

    private final Project project;

    public LegacyProjectApiHelper(Project project) {
        this.project = project;
    }

    @Override
    public File getBuildDirectory() {
        return project.getBuildDir();
    }

    @Override
    public File file(String path) {
        return project.file(path);
    }

    @Override
    public File file(File file) {
        return project.file(file);
    }


    @Override
    public ExecResult exec(Action<ExecSpec> closure) {
        return project.exec(closure);
    }
}
