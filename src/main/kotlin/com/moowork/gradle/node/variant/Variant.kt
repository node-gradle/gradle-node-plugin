package com.moowork.gradle.node.variant

import java.io.File

class Variant(
        val isWindows: Boolean,

        val nodeExec: String,
        val nodeDir: File,
        val nodeBinDir: File,

        val npmExec: String,
        val npmDir: File,
        val npmBinDir: File,
        val npmScriptFile: String,

        val npxExec: String,
        val npxScriptFile: String,

        val yarnExec: String,
        val yarnDir: File,
        val yarnBinDir: File,

        val archiveDependency: String,
        val exeDependency: String?
)
