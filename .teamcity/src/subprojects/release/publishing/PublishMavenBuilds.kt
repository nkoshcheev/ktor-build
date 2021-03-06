package subprojects.release.publishing

import jetbrains.buildServer.configs.kotlin.v10.*
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildSteps
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.*
import subprojects.*
import subprojects.build.*
import subprojects.build.core.*
import subprojects.release.*

object PublishJvmToMaven : BuildType({
    id("KtorPublishJvmToMavenBuild")
    name = "Publish JVM to Maven"
    vcs {
        root(VCSCore)
    }
    steps {
        publishToMaven(
            listOf(
                "publishJvmPublicationToMavenRepository",
                "publishKotlinMultiplatformPublicationToMavenRepository",
                "publishMetadataPublicationToMavenRepository"
            )
        )
    }
    dependencies {
        snapshot(jvmBuild!!) {
        }
    }
    requirements {
        require(linux.agentString)
    }
})

object PublishJSToMaven : BuildType({
    id("KtorPublishJSToMavenBuild")
    name = "Publish JS to Maven"
    vcs {
        root(VCSCore)
    }
    steps {
        publishToMaven(
            listOf(
                "publishJsPublicationToMavenRepository",
                "publishMetadataPublicationToMavenRepository"
            )
        )
    }
    dependencies {
        snapshot(jsBuild!!) {
        }
    }
    requirements {
        require(linux.agentString)
    }
})

object PublishWindowsNativeToMaven : BuildType({
    id("KtorPublishWindowsNativeToMavenBuild")
    name = "Publish Windows Native to Maven"
    vcs {
        root(VCSCore)
    }
    steps {
        publishToMaven(
            listOf(
                "publishMingwX64PublicationToMavenRepository",
                "publishMetadataPublicationToMavenRepository"
            )
        )
    }
    dependencies {
        snapshot(nativeWindowsBuild!!) {
        }
    }
    requirements {
        require(windows.agentString)
    }
})

object PublishLinuxNativeToMaven : BuildType({
    id("KtorPublishLinuxNativeToMavenBuild")
    name = "Publish Linux Native to Maven"
    vcs {
        root(VCSCore)
    }
    steps {
        publishToMaven(
            listOf(
                "publishLinuxX64PublicationToMavenRepository",
                "publishMetadataPublicationToMavenRepository"
            )
        )
    }
    dependencies {
        snapshot(nativeLinuxBuild!!) {
        }
        snapshot(PublishWindowsNativeToMaven) {
        }
    }
    requirements {
        require(linux.agentString)
    }
})

object PublishMacOSNativeToMaven : BuildType({
    id("KtorPublishMacOSNativeToMavenBuild")
    name = "Publish Mac Native to Maven"
    vcs {
        root(VCSCore)
    }
    steps {
        publishToMaven(
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
    }
    dependencies {
        snapshot(nativeMacOSBuild!!) {
        }
        snapshot(PublishWindowsNativeToMaven) {
        }
    }
    requirements {
        require(macOS.agentString)
    }
})

fun BuildSteps.prepareKeyFile() {
    script {
        name = "Prepare gnupg"
        scriptContent =
            """#!/bin/bash
set -eux pipefail
mkdir -p %env.SIGN_KEY_LOCATION%
cd "%env.SIGN_KEY_LOCATION%"
export HOME=${'$'}(pwd)
export GPG_TTY=${'$'}(tty)
line1='-----BEGIN PGP PRIVATE KEY BLOCK-----'
line_last='-----END PGP PRIVATE KEY BLOCK-----'
cat >keyfile <<EOT
%env.SIGN_KEY_PRIVATE%
EOT
key=${'$'}(cat ./keyfile | grep -o -P '(?<=-----BEGIN PGP PRIVATE KEY BLOCK-----).*(?=-----END PGP PRIVATE KEY BLOCK-----)' | tr ' ' '\n')
echo "${'$'}line1" > ./keyfinal
echo "${'$'}key\n" >> ./keyfinal
echo "${'$'}line_last" >> ./keyfinal
rm -rf .gnupg
gpg --allow-secret-key-import --batch --import keyfinal
rm -v keyfinal
cat >keyfile <<EOT
%env.SIGN_KEY_PUBLIC%
EOT
gpg --batch --import keyfile
rm -v keyfile"""
            .trimIndent()
        workingDir = "."
    }
}

fun BuildSteps.cleanupKeyFile() {
    script {
        name = "Cleanup"
        executionMode = BuildStep.ExecutionMode.ALWAYS
        scriptContent = """
cd .
rm -rf .gnupg
                        """.trimIndent()
        workingDir = "."
    }
}

private fun BuildSteps.publishToMaven(gradleTasks: List<String>) {
    prepareKeyFile()
    gradle {
        name = "Parallel assemble"
        tasks = gradleTasks.joinToString(" ") + " --i"
    }
    cleanupKeyFile()
}


