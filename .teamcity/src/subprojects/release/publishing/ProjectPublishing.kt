package subprojects.release.publishing

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import subprojects.*
import subprojects.build.core.*

data class PublishingTarget(val name: String, val build: BuildType?)

object ProjectPublishing : Project({
    id("ProjectPublishing")
    name = "Publishing"
    description = "Publish artifacts to repositories"

    val publishingTargets = listOf(
        PublishingTarget("JVM", publishingBuilds["$linux$java11"]),
        PublishingTarget("JavaScript", publishingBuilds["$js"]),
        PublishingTarget("Windows", publishingBuilds["$windows"]),
        PublishingTarget("Linux", publishingBuilds["$linux"]),
        PublishingTarget("macOS", publishingBuilds["$macOS"]))

    val allBuilds = publishingTargets.map(::PublishMavenBuild)

    allBuilds.forEach(::buildType)

    buildType {
        id("KtorPublish_All")
        name = "Publish All"
        type = BuildTypeSettings.Type.COMPOSITE

        vcs {
            root(VCSCore)
        }

        dependencies {
            allBuilds.mapNotNull { it.id }.forEach { id ->
                snapshot(id) {
                    onDependencyFailure = FailureAction.FAIL_TO_START
                }
            }
        }
    }
})
