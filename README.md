# Cardinal-Components-API
Component API for data-driven development

## adding the API to your buildscript:
```gradle
repositories {
    maven {
        name = "NerdHubMC"
        url = "https://maven.abusedmaster.xyz"
    }
}

dependencies {
    modCompile "com.github.NerdHubMC:Cardinal-Component-API:<VERSION>"
}
```

## Usage
The cardinal component API provides basic structure for data-driven content.
To expose a component simply implement `ComponentProvider` on your class.
These component providers are currently defined:
- Blocks (usually the block simply proxies it's Block Entity, however the Block Entity does not need to implement ComponentProvider)

Components are accessed by invoking `ComponentProvider#getComponent()` with the correspongind `ComponentType`.
A `ComponentType` is obtained by calling `ComponentRegistry.get()`, this instance should be stored as it acts as key for future component accesses.
<br/>Using the component registry ensures that all `ComponentType`s of a kind share the same object instance.


## Planned:
- Item component system