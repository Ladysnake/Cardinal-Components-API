/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
