This test mod adds a new component type called Vita. Two entities can hold it: Vita Zombies and Players.
Players do not naturally have vita, vita zombies are the only source. Hitting a vita holder with a
vita stick will transfer some of it to the stick. Subsequently right clicking the stick will transfer
the collected vita to the player. Vita sticks also display the player's current (synchronized) vita in their tooltip.

Code map:
- `nerdhub.cardinal.componentstest`: root package
    - `vita`: component implementation classes
        - `Vita`: the interface defining Vita behaviour
        - `BaseVita`: base implementation
        - `EntityVita`: entity-aware extension of `BaseVita` with custom behaviour
        - `PlayerVita`: synchronized extension of `EntityVita`
    - `CardinalComponentsTest`: registration + callbacks
    - `VitalityStickItem`: a custom item that attaches a `BaseVita` instance to its item stacks
    - `VitalityZombieEntity`: a custom entity that attaches an `EntityVita` instance to itself
