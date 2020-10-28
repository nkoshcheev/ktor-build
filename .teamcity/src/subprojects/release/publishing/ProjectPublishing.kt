package subprojects.release.publishing

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import subprojects.*
import subprojects.build.core.*

data class PublishingEntry(val name: String, val build: BuildType?, val tasks: List<String>)

object ProjectPublishing : Project({
    id("ProjectPublishing")
    name = "Publishing"
    description = "Publish artifacts to repositories"

    val publishingEntries = listOf(
        PublishingEntry("JVM", generatedBuilds["${linux.name}${java11.name}"], listOf("publishJvmPublicationToMavenRepository","publishKotlinMultiplatformPublicationToMavenRepository")),
        PublishingEntry("JavaScript", generatedBuilds[js.name], listOf("")),
        PublishingEntry("Windows", generatedBuilds[windows.name], listOf("")),
        PublishingEntry("Linux", generatedBuilds[linux.name], listOf("")),
        PublishingEntry("macOS", generatedBuilds[macOS.name], listOf(""))
    )

    val allBuilds = publishingEntries.map(::PublishMavenBuild)

    allBuilds.forEach(::buildType)

    buildType {
        createCompositeBuild("KtorPublish_All", "Publish All", VCSCore, allBuilds)
    }
})
