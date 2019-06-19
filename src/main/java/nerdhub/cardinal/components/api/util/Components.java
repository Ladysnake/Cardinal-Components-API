package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.SidedContainerCompound;
import nerdhub.cardinal.components.api.provider.ComponentProvider;
import nerdhub.cardinal.components.api.provider.SidedProviderCompound;
import nerdhub.cardinal.components.impl.*;

import java.util.function.Supplier;

public final class Components {
    private Components() { throw new AssertionError(); }

    private static final ComponentProvider EMPTY_PROVIDER = new EmptyComponentProvider();
    private static final SidedProviderCompound EMPTY_SIDED_PROVIDER = new EmptySidedProviderCompound();

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

    public static SidedProviderCompound emptySidedProvider() {
        return EMPTY_SIDED_PROVIDER;
    }

    public static SidedProviderCompound wrappingSidedProvider(SidedContainerCompound backing) {
        return new DirectSidedProviderCompound(backing);
    }

    /* SidedCompoundContainer factory methods */

    public static SidedContainerCompound suppliedSidedContainer(Supplier<ComponentContainer> factory) {
        return new SuppliedSidedContainerCompound(factory);
    }
}
