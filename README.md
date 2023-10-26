## **The CCA Maven is moving!**

**As Jfrog has ended their free service for OSS projects, we had to move the maven repository before the 1st of July 2023.
See below for the new maven instructions - you will have to update your buildscripts with the new URL to fix dependency resolution failures.**

<h1>
    <picture>
        <source media="(prefers-color-scheme: dark)" srcset="banner-white.svg">
        <img src="banner.svg" alt="Cardinal Components API">
    </picture> 
</h1>

A components API for Quilt and Fabric that is easy, modular, and extremely fast.

*Cardinal Components API is a library for Minecraft mods to create data
components that can be attached to various providers. Those components
provide a standardized interface for mods to interact with otherwise opaque
objects and behaviours, thereby helping both mod creation and compatibility.* [^1]


**TL;DR: It allows you to attach data to things**

### An example

Let's say you want to make to add mana to your mod. You would most likely want to have a number stored on the player,
which gets saved alongside it. You may want to display a mana bar on the client, which will require synchronization.
You may want your mana to refill slowly over time, which will require ticking.
And then you may want to have something like mana batteries in the world,
which would require re-implementing all that on a custom block or entity.

**Cardinal Components API takes care of it all.**

*Detailed information is available in [the website's **wiki**](https://ladysnake.org/wiki/cardinal-components-api/landing)*.
The information below is a condensed form of the latter.  
If you have questions or need help with this library, you can also join the [Ladysnake Discord](https://discord.ladysnake.org).

## Features\*

- 🔗 Attach your components to a variety of vanilla classes
- 🧩 Implement once, plug anywhere - your data will be saved automatically
- 📤 Synchronize data with a single helper interface
- 👥 Choose how your components are copied when a player respawns
- ⏲️ Tick your components alongside their target
- 🛠️ Fine-tune everything so that it fits your needs
- ☄️ And enjoy the blazing speed of ASM-generated extensions

*\*Non-exhaustive, refer to the wiki and javadoc for the full list.*

## Adding the API to your buildscript:

**Upgrade information: versions 4.1.0 onwards of Cardinal Components API use the `org.ladysnake.cardinal-components-api` (lowercase) maven group instead of `io.github.onyxstudios.Cardinal-Components-API`**

Latest versions of Cardinal Components API are available on the Ladysnake maven:
```gradle
repositories {
    maven {
        name = 'Ladysnake Mods'
        url = 'https://maven.ladysnake.org/releases'
    }
}

dependencies {
    // Adds a dependency on the base cardinal components module (required by every other module)
    // Replace modImplementation with modApi if you expose components in your own API
    modImplementation "org.ladysnake.cardinal-components-api:cardinal-components-base:<VERSION>"
    // Adds a dependency on a specific module
    modImplementation "org.ladysnake.cardinal-components-api:<MODULE>:<VERSION>"
    // Includes Cardinal Components API as a Jar-in-Jar dependency (optional)
    include "org.ladysnake.cardinal-components-api:cardinal-components-base:<VERSION>"
    include "org.ladysnake.cardinal-components-api:<MODULE>:<VERSION>"
}
```

Check out **https://ladysnake.org/wiki/cardinal-components-api/dev-install** for up-to-date buildscript samples
with `build.gradle`, `build.gradle.kts`, and  `libs.versions.toml`.

You can find the current version of the API in the [**releases**](https://github.com/Ladysnake/Cardinal-Components-API/releases) tab of the repository on Github.

Cardinal Components API is split into several modules. To depend on the all-encompassing master jar, use the dependency string
`org.ladysnake.cardinal-components-api:cardinal-components-api:<VERSION>`.
That artifact brings every module to your dev env, but you often do not need all of them for a project.
Also note that the maven version of the fat jar is actually empty, so you will have to require users to install it from curseforge or modrinth if you do not bundle all required modules.

**[[List of individual module names and descriptions]](https://ladysnake.org/wiki/cardinal-components-api/landing#modules)**

Example:
```gradle
// Adds an API dependency on the base cardinal components module (required by every other module)
modApi "org.ladysnake.cardinal-components-api:cardinal-components-base:<VERSION>"
// Adds an implementation dependency on the entity module
modImplementation "org.ladysnake.cardinal-components-api:cardinal-components-entity:<VERSION>"
```

## Basic Usage

To get started, you only need a class implementing `Component`.
It is recommended to have it split into an interface and an implementation,
so that internals get properly encapsulated and so that the component itself can be used
as an API by other mods.

**Minimal code example:**
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
*Note: a component class can be reused for several component types*

If you want your component to be **automatically synchronized with watching clients**,
you can also add the [`AutoSyncedComponent`](./cardinal-components-base/src/main/java/org/ladysnake/cca/api/v3/component/sync/AutoSyncedComponent.java)
interface to your implementation:

```java
class SyncedIntComponent implements IntComponent, AutoSyncedComponent {
    private int value;
    private final Entity provider;  // or World, or whatever you are attaching to

    public SyncedIntComponent(Entity provider) { this.provider = provider; }

    public void setValue(int value) {
        this.value = value;
        MyComponents.MAGIK.sync(this.provider); // assuming MAGIK is the right key for this component
    }
    // implement everything else
}
```

**[[More information on component synchronization]](https://ladysnake.org/wiki/cardinal-components-api/synchronization)**

If you want your component to **tick alongside its provider**, you can add the
[`ServerTickingComponent`](./cardinal-components-base/src/main/java/prg/ladysnake/cca/api/v3/component/tick/ServerTickingComponent.java)
or [`ClientTickingComponent`](./cardinal-components-base/src/main/java/org/ladysnake/cca/api/v3/component/tick/ClientTickingComponent.java)
(or both) to your *component interface* (here, `IntComponent`). If you'd rather add the ticking interface to a single
component subclass, **you have to use one of the specific methods provided in the individual modules**
(here something of the form `registry.beginRegistration(IntComponent.KEY).impl(IncrementingIntComponent.class).end(IncrementingIntComponent::new)`).

```java
class IncrementingIntComponent implements IntComponent, ServerTickingComponent {
    private int value;
    @Override public void serverTick() { this.value++; }
    // implement everything else
}
```

*Serverside ticking is implemented for all providers except item stacks.
 Clientside ticking is only implemented for entities, block entities, and worlds.*

If you want your component to **be notified of its provider being loaded and unloaded**, typically for advanced setup or cleanup,
you can add the [`ServerLoadAwareComponent`](./cardinal-components-base/src/main/java/org/ladysnake/cca/api/v3/component/load/ServerLoadAwareComponent.java)
or [`ClientLoadAwareComponent`](./cardinal-components-base/src/main/java/org/ladysnake/cca/api/v3/component/load/ClientLoadAwareComponent.java)
(or both) and their "Unload" variants to your *component interface* (here, `IntComponent`). Just like with ticking,
if you'd rather add the (un)load-aware interface to a single component subclass,
**you have to use one of the specific methods provided in the individual modules**.

```java
class IncrementingIntComponent implements IntComponent, ServerLoadAwareComponent {
    private int value;
    @Override public void loadServerside() { this.value++; }
    // implement everything else
}
```

*Serverside load and unload is implemented for entities, block entities, chunks, worlds, and scoreboards.
Clientside load and unload is only implemented for entities, block entities, and chunks. This is an experimental feature,
any feedback welcome.*

The next step is to choose an identifier for your component, and to declare it as a custom property in your mod's metadata:

**quilt.mod.json** (if you use [Quilt](https://quiltmc.org))
```json
{
    "schema_version": 1,
    "quilt_loader": {
        "id": "mymod"
    },
    "cardinal-components": [
        "mymod:magik"
    ]
}
```

**fabric.mod.json** (if you use [Fabric](https://fabricmc.net))
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
The most common providers are [entities](https://ladysnake.org/wiki/cardinal-components-api/modules/entity),
[item stacks](https://ladysnake.org/wiki/cardinal-components-api/modules/entity),
[worlds](https://ladysnake.org/wiki/cardinal-components-api/modules/world)
and [chunks](https://ladysnake.org/wiki/cardinal-components-api/modules/chunk),
but more are available.
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

Do not forget to declare your component initializer as an entrypoint in your mod's metadata:

**quilt.mod.json** (if you use Quilt)

```json
{
    "quilt_loader": {
        "entrypoints": {
            "cardinal-components": "a.b.c.MyComponents"
        },
    }
}
```

**fabric.mod.json** (if you use Fabric)
```json
{
    "entrypoints": {
        "cardinal-components": [
            "a.b.c.MyComponents"
        ]
    },
}
```

**[[More information on component registration]](https://ladysnake.org/wiki/cardinal-components-api/registration)**

Now, all that is left is to actually use that component. You can access individual instances of your component by using the dedicated getters on your `ComponentKey`:

```java
public static void useMagik(Entity provider) { // anything will work, as long as a module allows it!
    // Retrieve a provided component
    int magik = provider.getComponent(MAGIK).getValue();
    // Or, if the object is not guaranteed to provide that component:
    int magik = MAGIK.maybeGet(provider).map(IntComponent::getValue).orElse(0);
    // ...
}
```

## Test Mod
A test mod for the API is available in this repository, under `src/testmod`. It makes uses of most features from the API.
Its code is outlined in a secondary [readme](./src/testmod/readme.md).

[^1]: this description has been made exaggeratedly convoluted for comedic effects.