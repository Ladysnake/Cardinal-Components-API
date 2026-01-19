dependencies {
    api(project(":cardinal-components-base"))
}

loom {
    accessWidenerPath = project.file("src/main/resources/cardinal-components-item.accesswidener")
}
