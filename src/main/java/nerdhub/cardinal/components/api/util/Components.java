package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.provider.ComponentProvider;
import nerdhub.cardinal.components.api.provider.SidedComponentProvider;
import nerdhub.cardinal.components.impl.EmptyComponentProvider;
import nerdhub.cardinal.components.impl.EmptySidedComponentProvider;
import nerdhub.cardinal.components.impl.FallBackComponentProvider;

public final class Components {
    private Components() { throw new AssertionError(); }

    public static final ComponentProvider EMPTY_PROVIDER = new EmptyComponentProvider();
    public static final SidedComponentProvider EMPTY_SIDED_PROVIDER = new EmptySidedComponentProvider();

    /* ComponentProvider factory methods */

    public static ComponentProvider emptyProvider() {
        return EMPTY_PROVIDER;
    }

    public static ComponentProvider wrappingProvider(ComponentContainer backing) {
        return new SimpleComponentProvider(backing);
    }

    public static ComponentProvider fallBackProvider(ComponentProvider main, ComponentProvider fallback) {
        return new FallBackComponentProvider(main, fallback);
    }

    /* ComponentContainer factory methods */

    public static ComponentContainer indexedContainer() {
        return new IndexedComponentContainer();
    }

    /* SidedCompoundProvider factory methods */

    public static SidedCompoundProvider emptySidedCompound() {
        return EMPTY_SIDED_PROVIDER;
    }

    public static SidedCompoundProvider wrappingSidedCompound(SidedComponentContainer backing) {
        return new DirectSidedCompoundProvider(backing);
    }

    /* SidedCompoundContainer factory methods */

    public static SidedCompoundContainer lazySidedContainer(Supplier<ComponentContainer> factory) {
        return new LazySidedContainer(factory);
    }
}
