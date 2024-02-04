# Cardinal Components Test: Vita

## Description

This test mod adds a new component type called Vita. Two entities can hold it: Vita Zombies and Players.
Players do not naturally have vita, vita zombies are the only source. Hitting a vita holder with a
vita stick will transfer some of it to the stick. Subsequently right-clicking the stick will transfer
the collected vita to the player. Vita sticks also display the player's current (synchronized) vita in their tooltip.  

When shift-right-clicking a non-empty vita stick, a sliver of vita will be released in the world, and a title
will be displayed to every player within it. There is a small chance that the released vita slips into the
global fabric, making it available in every world.

A vita condenser is a special block interacting with ambient vita. When placed, it will slowly transfer
vita from the world to the chunk it is placed in. Right clicking a vita condenser displays the amount of
vita available in the chunk.

## Code map

- `org.ladysnake.componenttest`: root package
    - `content`: implementation of the content in the test mod
        - `vita`: component classes
            - `Vita`: the interface defining the vita component
            - `BaseVita`: base implementation
            - `AmbientVita`: abstract synchronized extension of `BaseVita` with custom behaviour
                - `LevelVita`: global implementation of `AmbientVita`
                - `WorldVita`: world-aware implementation of `AmbientVita`
            - `SyncedVita`: synchronized implementation of `BaseVita`
            - `EntityVita`: entity-specific extension of `BaseVita` with custom behaviour
            - `PlayerVita`: player-specific, synchronized extension of `EntityVita` with custom respawn behaviour
        - `CardinalComponentsTest`: mod initialization
        - `TestComponents`: component registration
        - `VitalityCondenser`: a custom block re-implementing `BlockProvider`, interacting with chunk and ambient vita
        - `VitalityStickItem`: a custom item that attaches a `BaseVita` instance to its item stacks
        - `VitalityZombieEntity`: a custom entity that attaches an `EntityVita` instance to itself
    - `tests`: unit test suite using Minecraft's gametest API

## Notes

Most Vita implementations exist to exhibit custom logic and synchronization behaviour.
Synchronization is not actually needed in most cases, the mod's function being here contrived
to test synchronization facilities. This means that a standard mod could very well have one
or two component implementations for all providers.

All component registration is done in `TestComponents`, using some variant of `ComponentFactoryRegistry`.
Most use a regular lambda or constructor reference, although the zombie vita uses a method reference to a `VitalityZombieEntity` instance method instead.

Adding custom items and entities is never required to use components. The ones in this test mod are merely examples of what a typical mod could do.
