package com.moowork.gradle.node;

import com.moowork.gradle.node.npm.NpmInstallTask;
import com.moowork.gradle.node.npm.NpmSetupTask;
import com.moowork.gradle.node.npm.NpmTask;
import com.moowork.gradle.node.npm.NpxTask;
import com.moowork.gradle.node.task.NodeTask;
import com.moowork.gradle.node.task.SetupTask;
import com.moowork.gradle.node.variant.VariantBuilder;
import com.moowork.gradle.node.yarn.YarnInstallTask;
import com.moowork.gradle.node.yarn.YarnSetupTask;
import com.moowork.gradle.node.yarn.YarnTask;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.Plugin;
import org.gradle.api.Project;


public class NodePlugin implements Plugin<Project> {

	public static final String NODE_GROUP = "Node";

	private Project project;
	private NodeExtension config;
	private SetupTask setupTask;
	private NpmSetupTask npmSetupTask;
	private YarnSetupTask yarnSetupTask;

	@Override
	public void apply(final Project project) {
		this.project = project;
		this.config = NodeExtension.create(this.project);

		addGlobalTypes();
		addTasks();
		addNpmRule();
		addYarnRule();

		this.project.afterEvaluate(it -> {
			NodePlugin.this.config.setVariant(new VariantBuilder(NodePlugin.this.config).build());
			configureSetupTask();
			configureNpmSetupTask();
			configureYarnSetupTask();
		});
	}

	private void addGlobalTypes() {
		addGlobalTaskType(NodeTask.class);
		addGlobalTaskType(NpmTask.class);
		addGlobalTaskType(NpxTask.class);
		addGlobalTaskType(YarnTask.class);
	}

	private void addTasks() {
		this.project.getTasks().create(NpmInstallTask.NAME, NpmInstallTask.class);
		this.project.getTasks().create(YarnInstallTask.NAME, YarnInstallTask.class);
		this.setupTask = this.project.getTasks().create(SetupTask.NAME, SetupTask.class);
		this.npmSetupTask = this.project.getTasks().create(NpmSetupTask.NAME, NpmSetupTask.class);
		this.yarnSetupTask = this.project.getTasks().create(YarnSetupTask.NAME, YarnSetupTask.class);
	}

	private void addGlobalTaskType(Class type) {
		this.project.getExtensions().getExtraProperties().set(type.getSimpleName(), type);
	}

	private void addNpmRule() {
		// note this rule also makes it possible to specify e.g. "dependsOn npm_install"
		this.project.getTasks().addRule("Pattern: \"npm_<command>\": Executes an NPM command.", taskName -> {
			if (taskName.startsWith("npm_")) {
				NpmTask npmTask = NodePlugin.this.project.getTasks().create(taskName, NpmTask.class);

				String[] tokens = DefaultGroovyMethods.tail(taskName.split("_"));// all except first
				npmTask.setNpmCommand(tokens);

				if (DefaultGroovyMethods.head(tokens).equalsIgnoreCase("run")) {
					npmTask.dependsOn(NpmInstallTask.NAME);
				}
			}
		});
	}

	private void addYarnRule() {
		// note this rule also makes it possible to specify e.g. "dependsOn yarn_install"
		this.project.getTasks().addRule("Pattern: \"yarn_<command>\": Executes an Yarn command.", taskName -> {
			if (taskName.startsWith("yarn_")) {
				YarnTask yarnTask = NodePlugin.this.project.getTasks().create(taskName, YarnTask.class);
				String[] tokens = DefaultGroovyMethods.tail(taskName.split("_"));// all except first
				yarnTask.setYarnCommand(tokens);

				if (DefaultGroovyMethods.head(tokens).equalsIgnoreCase("run")) {
					yarnTask.dependsOn(YarnInstallTask.NAME);
				}
			}
		});
	}

	private void configureSetupTask() {
		this.setupTask.setEnabled(this.config.getDownload());
	}

	private void configureNpmSetupTask() {
		this.npmSetupTask.configureVersion(this.config.getNpmVersion());
	}

	private void configureYarnSetupTask() {
		this.yarnSetupTask.configureVersion(this.config.getYarnVersion());
	}
}
