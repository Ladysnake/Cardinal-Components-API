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
