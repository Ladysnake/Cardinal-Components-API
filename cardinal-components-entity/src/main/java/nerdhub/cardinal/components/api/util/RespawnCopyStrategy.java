package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.event.PlayerCopyCallback;
import net.minecraft.nbt.CompoundTag;

/**
 * Represents a strategy to copy a component from a player to another.
 *
 * <p> Copy strategies can be registered using {@link EntityComponents#registerRespawnCopyStrat(ComponentType, RespawnCopyStrategy)}.
 *
 * @param <C> the type of components handled by this strategy
 * @see PlayerCopyCallback
 * @see EntityComponents
 */
@FunctionalInterface
public interface RespawnCopyStrategy<C extends Component> {
    void copyForRespawn(C from, C to, boolean lossless, boolean keepInventory);

    /**
     * Always copy a component no matter the cause of respawn.
     * This strategy is relevant for persistent metadata such as stats.
     */
    RespawnCopyStrategy<?> ALWAYS_COPY = (from, to, lossless, keepInventory) -> copy(from, to);

    /**
     * Copy a component whenever the player's inventory would be copied.
     * This strategy is relevant for any data storage tied to items or experience.
     */
    RespawnCopyStrategy<?> INVENTORY = (from, to, lossless, keepInventory) -> {
        if (lossless || keepInventory) {
            copy(from, to);
        }
    };

    /**
     * Copy a component only when the entire data is transferred from a player to the other.
     * This strategy is the default.
     */
    RespawnCopyStrategy<?> LOSSLESS_ONLY = (from, to, lossless, keepInventory) -> {
        if (lossless) {
            copy(from, to);
        }
    };

    /**
     * Never copy a component no matter the cause of respawn.
     * This strategy can be used when {@code RespawnCopyStrategy} does not offer enough context,
     * in which case {@link PlayerCopyCallback} may be used directly.
     */
    RespawnCopyStrategy<?> NEVER_COPY = (from, to, lossless, keepInventory) -> {};

    static <C extends Component> void copy(C from, C to) {
        to.fromTag(from.toTag(new CompoundTag()));
    }
}
