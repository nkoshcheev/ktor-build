package subprojects.release.publishing

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import subprojects.*
import subprojects.build.core.*

data class BuildData(val id: Id, val artifacts: String)
data class PublishingData(val buildName: String, val buildData: BuildData, val gradleTasks: List<String>)

object ProjectPublishing : Project({
    id("ProjectPublishing")
    name = "Publishing"
    description = "Publish artifacts to repositories"

    vcsRoot(VCSCore)

    val publishingEntries = listOf(
        PublishingData("JVM", generatedBuilds["${linux.name}${java11.name}"]!!, listOf("publishJvmPublicationToMavenRepository","publishKotlinMultiplatformPublicationToMavenRepository")),
        PublishingData("JavaScript", generatedBuilds[js.name]!!, listOf("")),
        PublishingData("Windows", generatedBuilds[windows.name]!!, listOf("")),
        PublishingData("Linux", generatedBuilds[linux.name]!!, listOf("")),
        PublishingData("macOS", generatedBuilds[macOS.name]!!, listOf(""))
    )

    val allBuilds = publishingEntries.map(::PublishMavenBuild)

    allBuilds.forEach(::buildType)

    buildType {
        createCompositeBuild("KtorPublish_All", "Publish All", VCSCore, allBuilds)
    }
})
