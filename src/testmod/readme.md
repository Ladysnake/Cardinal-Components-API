This test mod adds a new component type called Vita. Two entities can hold it: Vita Zombies and Players.
Players do not naturally have vita, vita zombies are the only source. Hitting a vita holder with a
vita stick will transfer some of it to the stick. Subsequently right clicking the stick will transfer
the collected vita to the player. Vita sticks also display the player's current (synchronized) vita in their tooltip.
When shift-right-clicking a non-empty vita stick, a sliver of vita will be released in the world, and a title
will be displayed to every player within it.

Code map:
- `nerdhub.cardinal.componentstest`: root package
    - `vita`: component implementation classes
        - `Vita`: the interface defining Vita behaviour
        - `BaseVita`: base implementation
        - `EntityVita`: entity-aware extension of `BaseVita` with custom behaviour
        - `PlayerVita`: synchronized extension of `EntityVita`
        - `WorldVita`: world-aware, synchronized extension of `BaseVita` with custom behaviour
    - `CardinalComponentsTest`: registration + callbacks
    - `VitalityStickItem`: a custom item that attaches a `BaseVita` instance to its item stacks
    - `VitalityZombieEntity`: a custom entity that attaches an `EntityVita` instance to itself

3 different ways of initializing components are shown. All use a `ComponentCallback`;
the player vita uses a regular lambda, the stick item vita has the item itself implement the `ItemComponentCallback`
interface, and the zombie vita uses a method reference to a `VitalityZombieEntity` instance method.
As such, the various `initComponents` methods are just one way of structuring the code and can be just as
easily replaced with lambdas.

Adding custom items and entities is never required to use components. The ones in this test mod are merely examples of what a typical mod could do.
