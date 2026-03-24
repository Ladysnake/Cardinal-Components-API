dependencies {
    // Need to add the base module to the annotation classpath too, otherwise the mixin obf AP chokes on ComponentProvider
    // (which we add to various classes through interface injection)
    api(project(":cardinal-components-base"))
    annotationProcessor(project(":cardinal-components-base"))
    localRuntime(fabricApi.module("fabric-gametest-api-v1", project.properties["fabric_api_version"].toString()))
    testmodImplementation(rootProject.project(":cardinal-components-base").sourceSets.testmod.get().output)
}

extensions.configure(PublishingExtension::class.java) {
    publications {
        create("relocationWorld", MavenPublication::class.java) {
            artifactId = "cardinal-components-world"
            pom {
                distributionManagement {
                    relocation {
                        artifactId = "cardinal-components-level"
                        message = "Module renamed to match Mojang official names"
                    }
                }
            }
        }
    }
}
