# Cardinal-Components-API
A components API for Fabric that is easy, modular, and extremely fast.  
*Cardinal Components API is a library for Minecraft mods to create data
components that can be attached to various providers. Those components
provide a standardized interface for mods to interact with otherwise opaque
objects and behaviours, thereby helping both mod creation and compatibility.*


**TL;DR: It allows you to attach data to things**


Detailed information is available in this repository's [wiki](https://github.com/OnyxStudios/Cardinal-Components-API/wiki).
The information below is a condensed form of the latter.

## Features
- Attach your components to a variety of vanilla classes
- Implement once, have them work (and be saved) everywhere
- Synchronize your components with a single helper interface
- Easily configure how your components are copied when a player respawns
- Customize everything so that it fits your needs
- All while enjoying the speed of ASM-generated extensions

## Adding the API to your buildscript:
Latest versions of Cardinal Components API are available on Bintray:
```gradle
repositories {
    maven {
        name = "Ladysnake Libs"
        url = "https://dl.bintray.com/ladysnake/libs"
    }
}

dependencies {
    // Replace modImplementation with modApi if you expose components in your own API
    modImplementation "io.github.onyxstudios.Cardinal-Components-API:<MODULE>:<VERSION>"
    // Includes Cardinal Components API as a Jar-in-Jar dependency (optional)
    include "io.github.onyxstudios.Cardinal-Components-API:<MODULE>:<VERSION>"
}
```

You can find the current version of the API in the [releases](https://github.com/OnyxStudios/Cardinal-Components-API/releases) tab of the repository on Github.

Cardinal Components API is split into several modules. To depend on the all-encompassing master jar, use the dependency string
`io.github.onyxstudios:Cardinal-Components-API:<VERSION>`. That artifact bundles every module, but you often do not need all of them for a project. **Individual module names and descriptions can be found [[here]](https://github.com/OnyxStudios/Cardinal-Components-API/wiki#modules)**.

Example:
```gradle
// Adds an API dependency on the base cardinal components module (required by every other module)
modApi "io.github.onyxstudios.Cardinal-Components-API:cardinal-components-base:<VERSION>"
// Adds an implementation dependency on the entity module
modImplementation "io.github.onyxstudios.Cardinal-Components-API:cardinal-components-entity:<VERSION>"
```

## Basic Usage

To get started, you only need a class implementing `Component`. It is recommended to have it split into an interface and an implementation, so that internals get properly encapsulated and so that the component itself can be used as an API by other mods.

Minimal code example:
```java
public interface IntComponent extends ComponentV3 {
    int getValue();
}

class RandomIntComponent implements IntComponent {
    private int value = (int) (Math.random() * 20);
    @Override public int getValue() { return this.value; }
    @Override public void readFromNbt(CompoundTag tag) { this.value = tag.getInt("value"); }
    @Override public void writeToNbt(CompoundTag tag) { tag.putInt("value", this.value); }
}
```
If you want your component to be **automatically synchronized with watching clients**, you can also add the [`AutoSyncedComponent`](https://github.com/OnyxStudios/Cardinal-Components-API/blob/master/cardinal-components-base/src/main/java/dev/onyxstudios/cca/api/v3/component/AutoSyncedComponent.java) interface to your implementation **[[More Info]](https://github.com/OnyxStudios/Cardinal-Components-API/wiki/Synchronizing-components)**.

The next step is to choose an identifier for your component, and to declare it in your `fabric.mod.json`'s custom properties:
```json
{
    "schemaVersion": 1,
    "id": "mymod",

    "custom": {
        "cardinal-components": [
            "mymod:magik"
        ]
    }
}
```

Components can be provided by objects of various classes, depending on which modules you installed.
The most common providers are [entities](https://github.com/OnyxStudios/Cardinal-Components-API/wiki/Cardinal-Components-Entity), [item stacks](https://github.com/OnyxStudios/Cardinal-Components-API/wiki/Cardinal-Components-Item), [worlds](https://github.com/OnyxStudios/Cardinal-Components-API/wiki/Cardinal-Components-World) and [chunks](https://github.com/OnyxStudios/Cardinal-Components-API/wiki/Cardinal-Components-Chunk), but more are available.
To interact with them, you need to **register a component key**, using `ComponentRegistryV3#getOrCreate`;
the resulting `ComponentKey` instance has the query methods you need. You will also need to **attach your
component** to some providers (here, to players and worlds):

```java
public final class MyComponents implements EntityComponentInitializer, WorldComponentInitializer {
    public static final ComponentKey<IntComponent> MAGIK = 
        ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("mymod:magik"), IntComponent.class);
        
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // Add the component to every PlayerEntity instance, and copy it on respawn with keepInventory
        registry.registerForPlayers(MAGIK, player -> new RandomIntComponent(), RespawnCopyStrategy.INVENTORY);
    }
    
    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        // Add the component to every World instance
        registry.register(MAGIK, world -> new RandomIntComponent());
    }    
}
```

Do not forget to declare your component initializer as an entrypoint in your `fabric.mod.json`:
```json
{
    "entrypoints": {
        "cardinal-components-entity": [
            "a.b.c.MyComponents"
        ],
        "cardinal-components-world": [
            "a.b.c.MyComponents"
        ]
    },
}
```

**[[More information on component registration]](https://github.com/OnyxStudios/Cardinal-Components-API/wiki/Registering-and-using-a-component)**

Now, all that is left is to actually use that component. You can access individual instances of your component by using the dedicated getters on your `ComponentKey`:

```java
public static void useMagik(Entity provider) { // anything will work, as long as a module allows it!
    // Retrieve a provided component
    int magik = MAGIK.get(provider).getValue();
    // Or, if the object is not guaranteed to provide that component:
    int magik = MAGIK.maybeGet(provider).map(IntComponent::getValue).orElse(0);
    // ...
}
```
*Note: a component class can be reused for several component types*

## Test Mod
A test mod for the API is available in this repository, under `src/testmod`. It makes uses of most features from the API.
Its code is outlined in a secondary [readme](https://github.com/OnyxStudios/Cardinal-Components-API/blob/master/src/testmod/readme.md).
