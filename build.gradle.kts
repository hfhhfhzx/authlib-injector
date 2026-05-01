import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.gradleup.shadow)
    alias(libs.plugins.palantir.git.version)
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.asm)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.release = 21
    options.isDeprecation = true
}

val gitVersion: groovy.lang.Closure<String> by extra
val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra

val buildNumber = System.getenv("AI_BUILD_NUMBER")
val gitInfo = versionDetails()
version = System.getenv("AI_VERSION_NUMBER") ?: "snapshot"
if (!gitInfo.isCleanTag) version = "$version.dirty"

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "yushijinhun",
            "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
            "Automatic-Module-Name" to "moe.yushi.authlibinjector",
            "Premain-Class" to "moe.yushi.authlibinjector.Premain",
            "Agent-Class" to "moe.yushi.authlibinjector.Premain",
            "Can-Retransform-Classes" to true,
            "Can-Redefine-Classes" to true,
            "Git-Commit" to gitInfo.gitHashFull,
            "Git-IsClean" to gitInfo.isCleanTag,
            "Build-Number" to (buildNumber ?: "-1")
        )
    }
}

tasks.processResources {
    from("AGPLv3-with-authlib-injector-exception.txt") {
        rename("AGPLv3-with-authlib-injector-exception.txt", "authlib-injector.txt")
        into("META-INF/licenses")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set(null)

    exclude("META-INF/maven/**")
    exclude("module-info.class")

    relocate("org.objectweb.asm", "moe.yushi.authlibinjector.internal.org.objectweb.asm")
}

defaultTasks("clean", "shadowJar")
