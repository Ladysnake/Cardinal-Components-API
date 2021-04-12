------------------------------------------------------
Version 3.0.0
------------------------------------------------------
**The breaking changes are here !**

## 3.0.0-nightly.21w14a
- Updated to 21w14a
- Removed the classes and methods deprecated in 2.8.0
    - Users should migrate their BE-less block components to Fabric API's API Lookup API

## 3.0.0 General Changelog

Additions
- Ticking components now work on any `BlockEntity`, not just those that already tick in vanilla

Changes
- Every class and method deprecated in 2.7.0 has been **completely removed**
- Every class remaining in the `nerdhub.component.*` packages has been moved to a `dev.onyxstudios.cca.*` package
- Replaced specific factory interfaces with a generic variant (eg. `ComponentFactory`)

------------------------------------------------------
Version 2.8.0
------------------------------------------------------
Additions
- Added helper methods in `BlockComponents` to expose block components through Fabric API API-API API

Changes
- Methods and classes in `cardinal-components-block` which purpose was to access components on regular blocks have been
  scheduled for removal as they are now superseded by API²

------------------------------------------------------
Version 2.7.13
------------------------------------------------------
Fixes
- Fixed a crash when registering scoreboard components
- Fixed a serialization issue when an item stack got initialized with an empty component container

------------------------------------------------------
Version 2.7.12
------------------------------------------------------
Fixes
- Fixed a potential issue with `ItemComponent`'s tag invalidation

------------------------------------------------------
Version 2.7.11
------------------------------------------------------
Additions
- Added the missing helper method `ItemComponent#putUuid`

Fixes
- Fixed dynamic entity component registration
- Fixed a crash with the scoreboard plugin when a save file has scoreboard teams

------------------------------------------------------
Version 2.7.10
------------------------------------------------------
Additions
- Added `ItemComponent`, an experimental component implementation that stores all its data in the stack's tag
- Added `ScoreboardComponentFactoryV2`, giving access to the server holding the scoreboard (if any)
- Added `TeamComponentFactoryV2`, giving access to the scoreboard holding the team and its server (if any)

Changes
- Promoted `ComponentPacketWriter` and `PlayerSyncPredicate` to stable API
- Further optimized ItemStack components
- A copy of the MIT license is now bundled with every CCA jar

------------------------------------------------------
Version 2.7.9
------------------------------------------------------
- Mcdev annotations are no longer a transitive dependency

------------------------------------------------------
Version 2.7.8
------------------------------------------------------
Changes
- Promoted `ComponentKey#sync(provider, writer)`, `ComponentKey#sync(provider, writer, predicate)`,
 `ComponentKey#isProvidedBy`, and `ComponentKey#getNullable` to stable API

Fixes
- Fixed backward compatibility with SyncedComponent

------------------------------------------------------
Version 2.7.7
------------------------------------------------------
Fixes
- Fixed NBT deserialization not respecting component registration order

------------------------------------------------------
Version 2.7.6
------------------------------------------------------
Changes
- Optimized ItemStack component initialization
- Promoted `ComponentKey#syncWith` to (experimental) public API
- Promoted `ClientTickingComponent`, `ServerTickingComponent` and `CommonTickingComponent` to stable API

------------------------------------------------------
Version 2.7.5
------------------------------------------------------
Additions
- Added the universal `cardinal-components` entrypoint key
    - This new key can be used for any CCA registration initializer (replaces eg. `cardinal-components-entity`)

------------------------------------------------------
Version 2.7.4
------------------------------------------------------
Additions
- Added a `TransientComponent` utility interface, for components that do not store any data
- Added a registration overload to `ItemComponentFactoryRegistry#registerFor` that takes an item directly

Fixes
- Fixed potential concurrency issues with `ComponentContainer.Factory.Builder`

------------------------------------------------------
Version 2.7.3
------------------------------------------------------
- Updated to 1.16.4

Changes
- `cardinal-components-item` will now verify that components attached to `ItemStack`s redefine `equals`
    - This behaviour can be disabled by adding `-Dcca.debug.noverifyequals=true` to your VM options

Fixes
- Fixed cardinal-components-block crashing on dedicated servers

------------------------------------------------------
Version 2.7.2
------------------------------------------------------
- Fixed components attached to a block entity not stacking with the superclass' components

------------------------------------------------------
Version 2.7.1
------------------------------------------------------
- Remove the `ScheduledForRemoval` annotation from the legacy `Component` interface
    - Should fix unstable API warnings in IDEA
- Fixed the new `AutoSyncedComponent` interface for level components

------------------------------------------------------
Version 2.7.0
------------------------------------------------------
**Deprecated most classes from the nerdhub.component.\* packages.**
Those classes will be **removed** during the MC 1.17 update.

Additions
- Implemented the new synchronization in `cardinal-components-level`
    - Added `LevelComponents#sync`, replacing `ComponentKey#sync` for components attached to `WorldProperties`
- Implemented the new (serverside) ticking API in `cardinal-components-level` and `cardinal-components-scoreboard`
- Added a `CommonTickingComponent` interface, implementing both Client and Server variants

Changes
- Item components now use lazily initialization
- The `ComponentContainer` and `ComponentProvider` interfaces are no longer experimental
- Refactored the sync API again, making it more flexible
    - Deprecated the old AutoSyncedComponent interface
- Moved ticking interfaces to a separate package
    - Deprecated the old ticking component interfaces

Fixes
- Fixed invalid metadata in the fabric.mod.json
- Fixed some hypothetical bugs with dropped items not merging

------------------------------------------------------
Version 2.6.0
------------------------------------------------------
- Added the Ticking Components experimental feature
- Made cardinal-components-block possibly more compatible with future versions of Immersive Portals
- Documented more methods with annotations

------------------------------------------------------
Version 2.5.4
------------------------------------------------------
- ComponentContainer iteration order is now the same as the factory registration order
    - This means mods have some (limited) control over the order in which components are de-serialized/synchronized
- Removed the experimental tag from more methods
- `ComponentContainer.Factory#createContainer` now accepts `null` arguments (but will NPE if a component factory does not expect it)

------------------------------------------------------
Version 2.5.3
------------------------------------------------------
- Fixed a crash in cardinal-components-chunk when `cardinal-components-world` was not installed

------------------------------------------------------
Version 2.5.2
------------------------------------------------------
- Added a `syncOp` parameter to `AutoSyncedComponent` methods, making the interface more flexible
    - The new parameter can be ignored, or it can be used to implement multiple types of sync without custom packets

------------------------------------------------------
Version 2.5.1
------------------------------------------------------
- Basic V3 interfaces are no longer experimental
    - This is a good time to start using them!
- Overhauled component synchronization for V3 API
    - There is no longer specialized synchronized component interfaces for each type of provider
    - This is a *breaking change* for Team and Block V3 (experimental) APIs
- Added dedicated static registration methods for player components in `cardinal-components-entity`
- V3 `ComponentContainer`s are no longer generic
- Added a dedicated `ComponentContainer` factory interface replacing use of `Function`,
  and moved the existing builder to it

------------------------------------------------------
Version 2.5.0
------------------------------------------------------
**This release breaks compatibility between modules of different versions.**
- Updated to 1.16.2

Additions
- Added a new [API for block components](https://github.com/OnyxStudios/Cardinal-Components-API/wiki/Cardinal-Components-Block) (see PR #42)
- Added a new API for scoreboard components (see PR #36)
- Added reworked `ComponentProvider` and `ComponentContainer` interfaces to the V3 API
    - Refactored a lot of internal code to use the new types
- Added `ComponentV3` and `ComponentRegistryV3` interfaces for early V3 adopters
- Added a factory builder to the `ComponentContainer` interface for third-party `ComponentProvider` implementations
- Added alternatives to `registerFor` methods taking a predicate, for fine-tuned registration

Changes
- The NBT serialized form of `ComponentContainer` is now more space-efficient.
    - Instances serialized before this update should get converted as they load.
- Removed experimental deprecated method `EntityComponentFactoryRegistry#register` (use `registerFor`)

Fixes
- Fixed item components not being copied by the smithing table
- Removed the `Internal` annotation from new API packages

------------------------------------------------------
Version 2.4.2
------------------------------------------------------
- Fixed regression in `cardinal-components-item` causing crash with Optifine installed
- Marked the `cardinal-components-level` module as incompatible with 1.16.2 and above

------------------------------------------------------
Version 2.4.1
------------------------------------------------------
- Fixed crash when more than 16 components were registered
- Fixed crash when dots were used in static component identifiers

------------------------------------------------------
Version 2.4.0
------------------------------------------------------
**Backwards-compatibility note: while this release is compatible with older applications, 
some classes have been moved to a new module called `cardinal-components-util`. 
If you are missing some types, or if you use the `cardinal-components-block` module, 
you need to add a dependency on that module.**

**Compatibility between modules of different versions has been broken in 2.4.0-nightly.1.16-pre4.**
In case of crashes due to those incompatibilities, Modpack makers and players can add the latest version
of the full library to their mods folder to update every module at once.

- Updated all modules to MC 1.16

Additions
- Cardinal Components now has a [wiki](https://github.com/OnyxStudios/Cardinal-Components-API/wiki/)!
- Added statically declared components (**experimental feature !**)
  - Mods can declare and attach their components using dedicated entrypoints, typically under the nomenclature `XComponentInitializer`
  - Developers can add `-Dcca.debug.asm=true` to their VM options to help debug CCA's issues with static component generation
  - More information is available in the wiki
    [[1]](https://github.com/OnyxStudios/Cardinal-Components-API/wiki/Registering-and-using-a-component#static-registration)
    [[2]](https://github.com/OnyxStudios/Cardinal-Components-API/wiki/Attaching-components#static-registration)
- Added a new module - `cardinal-components-util`
  - Most classes that were not essential to CCA working got moved from `cardinal-components-base` to that module
  - Added `LazyComponentType`, allowing mods to retrieve a `ComponentType` handle before it is registered
- Added `ItemComponentCallbackV2` and `ItemComponentFactoryV2` as alternatives respectively to `ItemComponentCallback` and `ItemComponentFactory`,
  passing the stack's true item as context.
- Added `Dynamic` conversion methods to `NbtSerializable` (defaulted to delegate to nbt serialization)
- Added `PlayerComponent`, an experimental interface replacing `RespawnCopyStrategy`

Changes
- TypeAwareComponent now has a default implementation in most subinterfaces
- It is no longer possible for a `ComponentCallback` to override an existing component
- Networking errors should now be logged before they get swallowed by Netty
- `cardinal-component-item` no longer changes the vanilla networking protocol for `ItemStack`s
- Internal classes and new interfaces are now in the `dev.onyxstudios.cca` package
- Chunk and level components now get initialized at the end of their respective classes' constructor
- Internals and new APIs are now in the `dev.onyxstudios.cca` package instead of `nerdhub.cardinal.components`

Fixes
- Fixed `PlayerSyncCallback` not firing when a player is teleported to another dimensions through commands
- Fixed Cardinal-Components-Item preventing vanilla clients from connecting to modded servers and vice-versa

------------------------------------------------------
Version 2.3.7
------------------------------------------------------
- Fixed Cardinal-Components-Item crashing with Optifine (thanks to ZekerZhayard)

------------------------------------------------------
Version 2.3.6
------------------------------------------------------
- Fixed a random crash when an entity spawned for the first time simultaneously on client and server (#26)

------------------------------------------------------
Version 2.3.5
------------------------------------------------------
- Fixed thrown items with different components being able to merge

------------------------------------------------------
Version 2.3.4
------------------------------------------------------
Ok but this time it's true, item components are really fixed for real
- Item components are now properly copied in every vanilla situation where an item stack is copied
- Component deserialization is slightly faster

------------------------------------------------------
Version 2.3.3
------------------------------------------------------
- Fixed item component initialization for real this time

------------------------------------------------------
Version 2.3.2
------------------------------------------------------
- Fixed component initialization in empty item stacks (eg. during inventory insertion)

------------------------------------------------------
Version 2.3.1
------------------------------------------------------
- Fixed crash with item components when picking up items
- Marked cardinal-components-world as incompatible with 1.16

------------------------------------------------------
Version 2.3.0
------------------------------------------------------
**This update may be incompatible with mods using previous versions of `cardinal-components-item` or `cardinal-components-chunk`**

- You can now make component types without an interface. Do not abuse this if you want to make mod compatibility easier.
- Replaced component cloning with a data copying mechanism between existing component instances (see PR #15).
    - Deprecated `CloneableComponent` and `NativeCloneableComponent` in favour of `CopyableComponent`
- Fixed concurrency issues with component synchronization.

------------------------------------------------------
Version 2.2.0
------------------------------------------------------
- Updated to 1.15
- Added `ComponentRegisteredCallback` to let mods react to component registration
- Fixed crash when using ComponentType#attach with recent versions of Fabric API

------------------------------------------------------
Version 2.1.0
------------------------------------------------------
- Added `ComponentType#attach`, a convenience method to subscribe to component initialization events
- Deprecated `SyncedComponent#markDirty` in favor of `SyncedComponent#sync`, improving semantics

------------------------------------------------------
Version 2.0.3
------------------------------------------------------
- Fixed NPE crash when a player's component is synced too early

------------------------------------------------------
Version 2.0.2
------------------------------------------------------
- Fixed entity components not getting synced with the holder by default
- Fixed generics issues in RespawnCopyStrategy

------------------------------------------------------
Version 2.0.1
------------------------------------------------------
Added facilities to handle player respawning

Fixes:
- Fixed networking initialization causing server crashes
- Fixed item component synchronization failing
- Fixed regression in entity component synchronization

------------------------------------------------------
Version 2.0.0
------------------------------------------------------
- Rewrote the API
- Split the functionality into modules

------------------------------------------------------
Version 1.2.0
------------------------------------------------------
- add utility methods for getting block/item components
- refactor component interfaces

------------------------------------------------------
Version 1.1.3
------------------------------------------------------
- fix itemstack component client desync

------------------------------------------------------
Version 1.1.2
------------------------------------------------------
- fix itemstack deserialization

------------------------------------------------------
Version 1.1.1
------------------------------------------------------
- mixin update

------------------------------------------------------
Version 1.1.0
------------------------------------------------------
- updated component system -> merged item and block components
- added Identifier to component registry

------------------------------------------------------
Version 1.0.0
------------------------------------------------------
- Initial Release
