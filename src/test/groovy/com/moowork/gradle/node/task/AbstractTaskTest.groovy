package com.moowork.gradle.node.task

import com.moowork.gradle.AbstractProjectTest
import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.util.PlatformHelper
import org.gradle.api.Action
import org.gradle.api.internal.ProcessOperations
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

import java.lang.reflect.Field

abstract class AbstractTaskTest
		extends AbstractProjectTest
{
	def ExecResult execResult

	def ExecSpec execSpec

	def Properties props

	def NodeExtension ext

	def setup()
	{
		this.props = new Properties()
		PlatformHelper.INSTANCE = new PlatformHelper( this.props )

		this.execResult = Mock( ExecResult )

		this.project.apply plugin: 'com.github.node-gradle.node'
		this.ext = NodeExtension.get( this.project )

		mockExec()
	}

	private void mockExec()
	{
		// Create mock to track exec calls
		ProcessOperations processOperations = Spy(this.project.getProcessOperations())
		processOperations.exec(_ as Action<ExecSpec>) >> { Action<ExecSpec> action ->
			action.execute(this.execSpec)
			return this.execResult
		}
		// Gradle does not allow us to easily inject our own services; manually override the ProcessOperations service
		Field processOperationsField = this.project.getClass().getDeclaredFields()
				.findAll { it.name ==~ /\w+processOperations\w+/ }
				.tap { assert it.size() == 1 }
				.first()
		processOperationsField.setAccessible(true)
		processOperationsField.set(this.project, processOperations)
	}

	protected containsPath( final Map<String, ?> env ) {
		return env['PATH'] != null || env['Path'] != null;
	}
}
