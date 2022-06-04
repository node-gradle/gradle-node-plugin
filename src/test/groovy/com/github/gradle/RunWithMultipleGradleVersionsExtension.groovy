package com.github.gradle


import org.gradle.util.GradleVersion
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo

import java.lang.reflect.Parameter

class RunWithMultipleGradleVersionsExtension extends AbstractAnnotationDrivenExtension<RunWithMultipleGradleVersions> {
    private GradleVersion gradleVersion

    @Override
    void visitSpecAnnotation(RunWithMultipleGradleVersions annotation, SpecInfo spec) {
        executeFeaturesWithAllGradleVersions(spec)
        makeGradleVersionAvailableInTheSetupMethod(spec)
    }

    protected void executeFeaturesWithAllGradleVersions(SpecInfo spec) {
        def bottomSpec = spec.getBottomSpec()
        def gradleVersions = GradleVersionsForTest.computeCandidateGradleVersions()
        for (FeatureInfo feature : bottomSpec.getFeatures()) {
            feature.setReportIterations(true)
            feature.setIterationNameProvider({ "${gradleVersion}" })
            feature.addInterceptor({ IMethodInvocation invocation ->
                for (GradleVersion gradleVersion : gradleVersions) {
                    println("Running with ${gradleVersion}")
                    this.gradleVersion = gradleVersion
                    invocation.proceed()
                }
            })
        }
    }

    protected void makeGradleVersionAvailableInTheSetupMethod(SpecInfo spec) {
        for (MethodInfo setupMethod : spec.getSetupMethods()) {
            setupMethod.addInterceptor({ IMethodInvocation invocation ->
                enrichParameters(invocation, gradleVersion)
                invocation.proceed()
            })
        }
    }

    private static void enrichParameters(IMethodInvocation invocation, GradleVersion gradleVersion) {
        // Inspired from http://spockframework.org/spock/docs/1.3/extensions.html#_injecting_method_parameters
        Map<Parameter, Integer> parameters = [:]
        invocation.method.reflection.parameters.eachWithIndex { parameter, i ->
            parameters << [(parameter): i]
        }
        parameters = parameters.findAll { GradleVersion.equals it.key.type }
        // enlarge arguments array if necessary
        def lastGradleVersionParameterIndex = parameters*.value.max()
        lastGradleVersionParameterIndex = lastGradleVersionParameterIndex == null ?
                0 :
                lastGradleVersionParameterIndex + 1
        if (invocation.arguments.length < lastGradleVersionParameterIndex) {
            def newArguments = new Object[lastGradleVersionParameterIndex]
            System.arraycopy(invocation.arguments, 0, newArguments, 0, invocation.arguments.length)
            invocation.arguments = newArguments
        }

        parameters.each { parameter, i ->
            invocation.arguments[i] = gradleVersion
        }
    }
}
