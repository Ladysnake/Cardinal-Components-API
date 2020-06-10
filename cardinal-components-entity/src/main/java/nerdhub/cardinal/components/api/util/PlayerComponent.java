package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import net.minecraft.world.GameRules;

public interface PlayerComponent<C extends Component> extends CopyableComponent<C> {
    /**
     * Copy data from a component to another as part of a player respawn.
     *
     * @param original      the component to copy data from
     * @param lossless      {@code true} if the player is copied exactly, such as when coming back from the End
     * @param keepInventory {@code true} if the player's inventory and XP are kept, such as when
     *                      {@link GameRules#KEEP_INVENTORY} is enabled or the player is in spectator mode
     * @implNote the default implementation copies a component only when the entire data is transferred from a player to the other
     */
    default void copyForRespawn(C original, boolean lossless, boolean keepInventory) {
        if (lossless) {
            this.copyFrom(original);
        }
    }
}
