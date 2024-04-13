dependencies {
    api(project(path = ":cardinal-components-base", configuration = "namedElements"))
}

loom {
    accessWidenerPath = project.file("src/main/resources/cardinal-components-item.accesswidener")
}
