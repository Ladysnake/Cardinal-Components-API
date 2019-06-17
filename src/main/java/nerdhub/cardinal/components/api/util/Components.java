package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.provider.ComponentProvider;
import nerdhub.cardinal.components.api.provider.SidedComponentProvider;
import nerdhub.cardinal.components.impl.EmptyComponentProvider;
import nerdhub.cardinal.components.impl.EmptySidedComponentProvider;

public final class Components {
    // Suppresses default constructor, ensuring non-instantiability.
    private Components() { throw new AssertionError(); }

    public static final ComponentProvider EMPTY_PROVIDER = new EmptyComponentProvider();
    public static final SidedComponentProvider EMPTY_SIDED_PROVIDER = new EmptySidedComponentProvider();

}
