package subprojects.release.publishing

import jetbrains.buildServer.configs.kotlin.v10.*
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import subprojects.build.core.*
import java.lang.RuntimeException

class PublishMavenBuild(private val publishingEntry: PublishingEntry) : BuildType({
    id("KtorPublishMavenBuild_${publishingEntry.name}".toExtId())
    name = "Publish ${publishingEntry.name} to Maven"

    dependencies {
        val buildId = publishingEntry.build?.id ?: throw RuntimeException("Build ID not found for entry ${publishingEntry.name}")
        artifacts(buildId) {
            buildRule = lastSuccessful()
            artifactRules = stripReportArtifacts(artifactRules)
        }
    }
})

fun stripReportArtifacts(artifacts: String): String {
    return artifacts.replace("$junitReportArtifact\n", "")
        .replace("$memoryReportArtifact\n", "")
}
