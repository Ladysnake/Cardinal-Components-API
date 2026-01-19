dependencies {
    api(project(":cardinal-components-base"))
    annotationProcessor(project(":cardinal-components-base"))
    testmodImplementation(rootProject.project(":cardinal-components-base").sourceSets.testmod.get().output)
    localImplementation(fabricApi.module("fabric-object-builder-api-v1", project.properties["fabric_api_version"] as String))
    localImplementation(fabricApi.module("fabric-entity-events-v1", project.properties["fabric_api_version"] as String))
}
