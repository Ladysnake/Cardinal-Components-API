/*
 * Cardinal-Components-API
 * Copyright (C) 2019 GlassPane
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.event.PlayerCopyCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.GameRules;

/**
 * Represents a strategy to copy a component from a player to another.
 *
 * <p> Copy strategies can be registered using {@link EntityComponents#setRespawnCopyStrategy(ComponentType, RespawnCopyStrategy)}.
 *
 * @param <C> the type of components handled by this strategy
 * @see PlayerCopyCallback
 * @see EntityComponents
 */
@FunctionalInterface
public interface RespawnCopyStrategy<C extends Component> {
    /**
     * Copy data from a component to another as part of a player respawn.
     *
     * @param from          the component to copy data from
     * @param to            the component to copy data to
     * @param lossless      {@code true} if the player is copied exactly, such as when coming back from the End
     * @param keepInventory {@code true} if the player's inventory and XP are kept, such as when
     *                      {@link GameRules#KEEP_INVENTORY} is enabled or the player is in spectator mode
     */
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
