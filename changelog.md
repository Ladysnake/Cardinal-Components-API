------------------------------------------------------
Version 2.3.5-alt
------------------------------------------------------
- This release is available on the [Ladysnake bintray](https://bintray.com/beta/#/ladysnake/libs/Cardinal-Components-API?tab=overview)

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
