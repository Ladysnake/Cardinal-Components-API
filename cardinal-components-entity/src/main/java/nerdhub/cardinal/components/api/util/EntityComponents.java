package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.internal.CardinalEntityInternals;

public final class EntityComponents {

    /**
     * Register a respawn copy strategy for components of a given type.
     *
     * <p> When a player is cloned as part of the respawn process, its components are copied using
     * a {@link RespawnCopyStrategy}. By default, the strategy used is {@link RespawnCopyStrategy#LOSSLESS_ONLY}.
     * Calling this method allows one to customize the copy process.
     *
     * @param type     the representation of the registered type
     * @param strategy a copy strategy to use when copying components between player instances
     * @param <C>      the type of components affected
     *
     * @see nerdhub.cardinal.components.api.event.PlayerCopyCallback
     * @see #getRespawnCopyStrat(ComponentType)
     */
    public static <C extends Component> void registerRespawnCopyStrat(ComponentType<C> type, RespawnCopyStrategy<C> strategy) {
        CardinalEntityInternals.registerRespawnCopyStrat(type, strategy);
    }

    public static <C extends Component> RespawnCopyStrategy<C> getRespawnCopyStrat(ComponentType<C> type) {
        return CardinalEntityInternals.getRespawnCopyStrat(type);
    }
}
