@file:Suppress("OPT_IN_USAGE")

import com.deezer.caupain.model.StabilityLevelPolicy
import com.deezer.caupain.model.gradle.GradleStabilityLevel
import com.deezer.caupain.plugin.DependenciesUpdateTask
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.compat.patrouille)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.vanniktech.maven.publish)
    alias(libs.plugins.cashapp.burst)
    alias(libs.plugins.detekt)
    alias(libs.plugins.changelog)
    alias(libs.plugins.caupain)
}

group = "io.github.bishiboosh"
val currentVersion = "1.0.0"

val isSnapshot = project.findProperty("isSnapshot")?.toString().toBoolean()
val isRelease = project.findProperty("isRelease")?.toString().toBoolean()
val versionSuffix = project.findProperty("versionSuffix")?.toString()
val isCI = System.getenv("CI").toBoolean()
version = buildString {
    append(currentVersion)
    if (!isRelease && (isSnapshot || !isCI)) append(".0")
    if (!versionSuffix.isNullOrBlank()) append("-$versionSuffix")
    if (!isRelease && (isSnapshot || !isCI)) append("-SNAPSHOT")
}

dependencies {
    "detektPlugins"(libs.detekt.libraries)
}

changelog {
    version.set(currentVersion)
}

compatPatrouille {
    java(17)
    kotlin(libs.versions.kotlin.get())
}

kotlin {
    explicitApi()
    compilerOptions.freeCompilerArgs.addAll(
        "-Xexpect-actual-classes",
        "-Xwhen-guards"
    )

    sourceSets {
        androidNativeArm32()
        androidNativeArm64()
        androidNativeX64()
        androidNativeX86()

        iosArm64()
        iosSimulatorArm64()
        iosX64()

        js().nodejs()

        jvm()

        linuxArm64()
        linuxX64()

        macosArm64()
        macosX64()

        mingwX64()

        tvosArm64()
        tvosSimulatorArm64()
        tvosX64()

        wasmJs().nodejs()
        wasmWasi().nodejs()

        watchosArm32()
        watchosArm64()
        watchosDeviceArm64()
        watchosSimulatorArm64()
        watchosX64()

        applyDefaultHierarchyTemplate()

        commonMain {
            dependencies {
                api(libs.kotlinx.serialization.core)
                api(libs.kotlinx.io)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kotlin.test.junit)
                implementation(libs.junit)
            }
        }
    }
}

detekt {
    config.from(rootProject.layout.projectDirectory.file("code-quality/detekt-core.yml"))
}

dokka {
    dokkaSourceSets.configureEach {
        documentedVisibilities(VisibilityModifier.Public)
    }
}

caupain {
    gradleStabilityLevel = GradleStabilityLevel.RC
    showVersionReferences = true
}

tasks.withType<DependenciesUpdateTask> {
    selectIf(StabilityLevelPolicy)
}