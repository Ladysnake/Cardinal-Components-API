dependencies {
    api(project(path = ":cardinal-components-base", configuration = "namedElements"))
    annotationProcessor(project(path = ":cardinal-components-base", configuration = "namedElements"))
    testmodImplementation(rootProject.project(":cardinal-components-base").sourceSets.testmod.get().output)
    modLocalImplementation(fabricApi.module("fabric-object-builder-api-v1", project.properties["fabric_api_version"] as String))
    modLocalImplementation(fabricApi.module("fabric-entity-events-v1", project.properties["fabric_api_version"] as String))
}
