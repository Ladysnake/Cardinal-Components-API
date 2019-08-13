package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.nbt.CompoundTag;

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

    static <C extends Component> void copy(C from, C to) {
        to.fromTag(from.toTag(new CompoundTag()));
    }
}
