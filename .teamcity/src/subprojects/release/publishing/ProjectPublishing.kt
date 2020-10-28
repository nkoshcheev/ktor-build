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

    val publishingEntries = listOf(
        PublishingData(
            "JVM", buildData("${linux.name}${java11.name}"),
            listOf(
                "publishJvmPublicationToMavenRepository",
                "publishKotlinMultiplatformPublicationToMavenRepository",
                "publishMetadataPublicationToMavenRepository"
            )
        ),
        PublishingData(
            "JavaScript", buildData(js.name),
            listOf(
                "publishJsPublicationToMavenRepository",
                "publishMetadataPublicationToMavenRepository"
            )
        ),
        PublishingData(
            "Windows", buildData(windows.name),
            listOf(
                "publishMingwX64PublicationToMavenRepository",
                "publishMetadataPublicationToMavenRepository"
            )
        ),
        PublishingData(
            "Linux", buildData(linux.name),
            listOf(
                "publishLinuxX64PublicationToMavenRepository",
                "publishMetadataPublicationToMavenRepository"
            )
        ),
        PublishingData(
            "macOS", buildData(macOS.name),
            listOf(
                "publishIosArm32PublicationToMavenRepository",
                "publishIosArm64PublicationToMavenRepository",
                "publishIosX64PublicationToMavenRepository",
                "publishMacosX64PublicationToMavenRepository",
                "publishTvosArm64PublicationToMavenRepository",
                "publishTvosX64PublicationToMavenRepository",
                "publishWatchosArm32PublicationToMavenRepository",
                "publishWatchosArm64PublicationToMavenRepository",
                "publishWatchosX86PublicationToMavenRepository",
                "publishMetadataPublicationToMavenRepository"
            )
        )
    )

    val allBuilds = publishingEntries.map(::PublishMavenBuild)

    allBuilds.forEach(::buildType)

    buildType {
        createCompositeBuild("KtorPublish_All", "Publish All", VCSCore, allBuilds)
    }
})

private fun buildData(buildConfiguration: String): BuildData {
    return generatedBuilds[buildConfiguration]
        ?: throw RuntimeException("Cannot find build data for $buildConfiguration")
}