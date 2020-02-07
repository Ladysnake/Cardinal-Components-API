/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2020 OnyxStudios
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
package nerdhub.cardinal.components.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * The callback interface for receiving player data copy events during
 * {@link ServerPlayerEntity#copyFrom(ServerPlayerEntity, boolean) player cloning}.
 */
@FunctionalInterface
public interface PlayerCopyCallback {
    Event<PlayerCopyCallback> EVENT = EventFactory.createArrayBacked(PlayerCopyCallback.class, (listeners) ->
            (original, clone, lossless) -> {
                for (PlayerCopyCallback callback : listeners) {
                    callback.copyData(original, clone, lossless);
                }
            }
    );

    /**
     * Copy mod data from a player to its clone.
     *
     * <p> When using an end portal from the End to the Overworld, a lossless copy will be made.
     * In such cases, all data should be copied exactly between the original and the clone.
     * Some data may also need exact copying when the {@link net.minecraft.world.GameRules#KEEP_INVENTORY keepInventory gamerule}
     * is enabled, which needs to be checked independently using {@code clone.world.getGameRules()}.
     * Otherwise, it is safe to simply ignore any data that should be reset with each death.
     *
     * <p> The {@code clone} is usually repositioned after this method has been called,
     * invalidating position and dimension changes. The dimension of the {@code original}
     * player itself has also been invalidated before this method is called, but is accessible
     * through {@code original.world.dimension}.
     *
     * @param original the player that is being cloned
     * @param clone    the clone of the {@code original} player
     * @param lossless {@code true} if all the data should be copied exactly, {@code false} otherwise.
     */
    void copyData(ServerPlayerEntity original, ServerPlayerEntity clone, boolean lossless);

}
