# Cardinal-Components-API
Fabric Component API for Entity-Component-System code in Minecraft.

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
Components are provided by various objects through the `ComponentProvider` interface. To interact with those,
you need to register your component type, using `ComponentRegistry.registerIfAbsent`. The resulting `ComponentType`
instance is used as a key for component providers.

Minimal code example:
```java
interface IntComponent {
    ComponentType<IntComponent> TYPE = ComponentRegistry.registerIfAbsent(new Identifier("mymod:int"), IntComponent.class);
    int getValue();
}
class RandomIntComponent implements IntComponent {
    private int value = (int) (Math.random() * 20);
    @Override public int getValue() { return this.value; }
    @Override public void fromTag(CompoundTag tag) { this.value = tag.getInt("value"); }
    @Override public CompoundTag toTag(CompoundTag tag) { tag.putInt("value", this.value); }
    @Override public IntComponent newInstance() { return new RandomIntComponent(); }
    @Override public boolean isComponentEqual(Component other) {
        return other instanceof IntComponent && this.value == ((IntComponent)other).value;
    }
}
```
All that is left is to actually attach that component to one or more component providers.
Cardinal Components API offers component provider implementations for a few vanilla types:

### Entities

Components can be added to entities of any type (modded or vanilla) by registering an `EntityComponentCallback`.
Entity components are saved automatically with the entity, but synchronization is manual.

### Item Stacks

Components can be added to stacks of any item (modded or vanilla) by registering an `ItemComponentCallback`.
Item stack components are saved and synchronized automatically.

**Changes to Vanilla**
- `ItemStack` equality: stack equality methods `areTagsEqual` and `isEqualIgnoreDamage` check component equality.
If you have issues when attaching components to item stacks, it usually means you forgot to implement a proper
`equals` check on your component.

### Blocks

Blocks actually implement the `BlockComponentProvider` interface instead of the regular `ComponentProvider`.
Custom blocks may re-implement that interface themselves to provide components independently of the presence of
a BlockEntity. Usually the block simply proxies its Block Entity, however the Block Entity does not need to 
implement `BlockComponentProvider` if the block already has a custom implementation. Block components can be
slightly less convenient to provide as they require their own implementations, but several utility classes
are available to help.


## Example Mod
An example mod for the API is available in this repository, under `src/testmod`.