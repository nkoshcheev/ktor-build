package subprojects.release.publishing

import jetbrains.buildServer.configs.kotlin.v10.*
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import subprojects.*

class PublishingBuild(private val target: PublishingTarget) : BuildType({
    id("KtorPublishing_${target.name}".toExtId())
    name = "Publish to ${target.name}"

    vcs {
        root(VCSCore)
    }

    dependencies {
        artifacts(AbsoluteId(target.id)) {
            buildRule = lastSuccessful()
            artifactRules = target.artifacts
        }
    }
})