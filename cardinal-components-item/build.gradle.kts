dependencies {
    api(project(path = ":cardinal-components-base", configuration = "namedElements"))

    // FIXME workaround for https://github.com/FabricMC/fabric-loom/issues/1334
    val fabricApiVersion: String = providers.gradleProperty("fabric_api_version").get()
    compileOnly(fabricApi.module("fabric-registry-sync-v0", fabricApiVersion))
}

loom {
    accessWidenerPath = project.file("src/main/resources/cardinal-components-item.accesswidener")
}
