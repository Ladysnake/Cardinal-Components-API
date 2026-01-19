dependencies {
    // Need to add the base module to the annotation classpath too, otherwise the mixin obf AP chokes on ComponentProvider
    // (which we add to various classes through interface injection)
    api(project(":cardinal-components-base"))
    annotationProcessor(project(":cardinal-components-base"))
    testmodImplementation(rootProject.project(":cardinal-components-base").sourceSets.testmod.get().output)
}
