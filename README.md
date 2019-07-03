# Cardinal-Components-API
A components API for Fabric that is easy, modular, and fast.

## Adding the API to your buildscript (loom 0.2.4):
```gradle
repositories {
    maven {
        name = "NerdHubMC"
        url = "https://maven.abusedmaster.xyz"
    }
}

dependencies {
    // Replace modImplementation with modApi if you expose components in your own API
    modImplementation "com.github.NerdHubMC:Cardinal-Components-API:<VERSION>"
    // Includes Cardinal Components API as a Jar-in-Jar dependency (optional)
    include "com.github.NerdHubMC:Cardinal-Components-API:<VERSION>"
}
```

You can find the current version of the API in the [releases](https://github.com/NerdHubMC/Cardinal-Components-API/releases) tab of the repository on Github.

## Usage

To get started, you only need 2 things: an interface extending `Component`, and a class implementing this interface.

Minimal code example:
```java
interface IntComponent extends Component {
    int getValue();
}

class RandomIntComponent implements IntComponent {
    private int value = (int) (Math.random() * 20);
    @Override public int getValue() { return this.value; }
    @Override public void fromTag(CompoundTag tag) { this.value = tag.getInt("value"); }
    @Override public CompoundTag toTag(CompoundTag tag) { tag.putInt("value", this.value); }
}
```
All that is left is to actually use that component.

Components are provided by various objects through the `ComponentProvider` interface. 
To interact with those, you need to register your component type, using `ComponentRegistry.registerIfAbsent`;
the resulting `ComponentType` instance is used as a key for component providers.
```java
public static final ComponentType<IntComponent> MAGIK = ComponentRegistry.registerIfAbsent(new Identifier("mymod:magik"), IntComponent.class);

public static void useMagik(ComponentProvider provider) {
    int magik = MAGIK.get(provider).getValue();
    // ...
}
```
*Note: a component class can be reused for several component types*

Cardinal Components API offers component provider implementations for a few vanilla types, each in its own module:

### Entities

Components can be added to entities of any type (modded or vanilla) by registering an `EntityComponentCallback`.
Entity components are saved automatically with the entity. Synchronization must be done either manually or with
help of the [`SyncedComponent`](https://github.com/Pyrofab/Cardinal-Components-API/blob/refactor%2Fmodularization/cardinal-components-base/src/main/java/nerdhub/cardinal/components/api/component/extension/SyncedComponent.java) 
and [`EntitySyncedComponent`](https://github.com/Pyrofab/Cardinal-Components-API/blob/refactor%2Fmodularization/cardinal-components-entity/src/main/java/nerdhub/cardinal/components/api/util/component/sync/EntitySyncedComponent.java) interfaces.

**Example:**
```java
EntityComponentCallback.EVENT.register(PlayerEntity.class, (player, components) -> components.put(MAGIK, new RandomIntComponent()));
```

### Item Stacks

Components can be added to stacks of any item (modded or vanilla) by registering an `ItemComponentCallback`.
Item stack components are saved and synchronized automatically.

**Notes:**
- `ItemStack` equality: stack equality methods `areTagsEqual` and `isEqualIgnoreDamage` check component equality.
If you have issues when attaching components to item stacks, it usually means you forgot to implement a proper
`equals` check on your component.
- Empty `ItemStack`: empty item stacks never expose any components, no matter what was originally attached to them.

**Example:**
```java
ItemComponentCallback.EVENT.register(Items.DIAMOND_PICKAXE, (stack, components) -> components.put(MAGIK, new RandomIntComponent()));
```

### Blocks

Blocks actually implement the `BlockComponentProvider` interface instead of the regular `ComponentProvider`.
Custom blocks may re-implement that interface themselves to provide components independently of the presence of
a BlockEntity. Usually the block simply proxies its Block Entity, however the Block Entity does not need to 
implement `BlockComponentProvider` if the block already has a custom implementation. Block components can be
slightly less convenient to provide as they require their own implementations, but several utility classes
are available to help.

Components are entirely compatible with [LibBlockAttributes](https://github.com/AlexIIL/LibBlockAttributes)' attributes.
Since `Component` is an interface, any attribute instance can easily implement it. Conversely, making an `Attribute`
for an existing `Component` is as simple as calling `Attributes.create(MyComponent.class)`.


## Example Mod
An example mod for the API is available in this repository, under `src/testmod`.
Its code is outlined in a secondary [readme](https://github.com/Pyrofab/Cardinal-Components-API/blob/v2/src/testmod/readme.md).
