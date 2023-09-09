import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17

plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
}

kotlin {
    compilerOptions {
        jvmTarget.set(JVM_17)
        freeCompilerArgs.add("-Xjvm-default=all")
        freeCompilerArgs.add("-Xlambdas=indy")
    }
}

dependencies {
    api(project(path = ":cardinal-components-base", configuration = "namedElements"))
    api(project(path = ":cardinal-components-entity", configuration = "namedElements"))
    modImplementation("net.fabricmc:fabric-language-kotlin:1.10.10+kotlin.1.9.10")
    modApi("io.github.natanfudge:kotlinx-serialization-minecraft:2.0.0+1.20.1")
    testmodImplementation(rootProject.project(":cardinal-components-base").sourceSets.testmod.get().output)
}
