package com.github.gradle.node.util

import org.jetbrains.annotations.NotNull

//Extends PlatformHelper to override just how it access system props to make it more testable
//without breaking configuration caching requirements
//see https://github.com/node-gradle/gradle-node-plugin/issues/209
class TestablePlatformHelper extends PlatformHelper {

    private Properties props;

    TestablePlatformHelper(Properties props) {
        this.props = props
    }

    @Override
    String getSystemProperty(@NotNull String name) {
        return props.getProperty(name)
    }
}
