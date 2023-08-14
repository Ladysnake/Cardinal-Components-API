dependencies {
    // Need to add the base module to the annotation classpath too, otherwise the mixin obf AP chokes on ComponentProvider
    // (which we add to various classes through interface injection)
    api(project(path = ":cardinal-components-base", configuration = "namedElements"))
    annotationProcessor(project(path = ":cardinal-components-base", configuration = "namedElements"))
    testmodImplementation(rootProject.project(":cardinal-components-base").sourceSets.testmod.get().output)
}
