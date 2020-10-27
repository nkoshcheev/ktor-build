package subprojects.release.publishing

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import subprojects.*

data class PublishingTarget(val name: String, val id: String, val artifacts: String)

val publishingTargets = listOf(
    PublishingTarget("JVM", "", ""),
    PublishingTarget("JavaScript", "", ""),
    PublishingTarget("Windows", "", ""),
    PublishingTarget("Linux", "", ""),
    PublishingTarget("macOS", "", ""))

object ProjectPublishing : Project({
    id("ProjectPublishing")
    name = "Publishing"
    description = "Publish artifacts to repositories"

    val allBuilds = publishingTargets.map(::PublishingBuild)

    allBuilds.forEach(::buildType)

    // TODO: Refactor this as it's repeated across multiple places
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
