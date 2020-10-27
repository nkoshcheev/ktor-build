package subprojects.release.publishing

import jetbrains.buildServer.configs.kotlin.v10.*
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import subprojects.*
import subprojects.build.core.*

class PublishMavenBuild(private val target: PublishingTarget) : BuildType({
    id("KtorPublishMavenBuild_${target.name}".toExtId())
    name = "Publish ${target.name} to Maven"

    vcs {
        root(VCSCore)
    }

    dependencies {
        artifacts(AbsoluteId(target.build?.id.toString())) {
            buildRule = lastSuccessful()
            artifactRules = stripReportArtifacts(artifactRules)
        }
    }
})

fun stripReportArtifacts(artifacts: String): String {
    return artifacts.replace("$junitReportArtifact\n", "")
        .replace("$memoryReportArtifact\n","")
}