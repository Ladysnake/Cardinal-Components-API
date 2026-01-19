dependencies {
    // Need to add the base module to the annotation classpath too, otherwise the mixin obf AP chokes on ComponentProvider
    // (which we add to various classes through interface injection)
    api(project(":cardinal-components-base"))
    annotationProcessor(project(":cardinal-components-base"))
    localRuntime(fabricApi.module("fabric-gametest-api-v1", project.properties["fabric_api_version"].toString()))
    testmodImplementation(rootProject.project(":cardinal-components-base").sourceSets.testmod.get().output)
}
