/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 Ladysnake
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
package org.ladysnake.cca.api.v3.entity;

import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.CopyableComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

@ApiStatus.Experimental
public interface PlayerComponent<C extends Component> extends Component, CopyableComponent<C> {
    /**
     * Check whether component data should be copied as part of a respawn situation.
     *
     * @param lossless      {@code true} if the player is copied exactly, such as when coming back from the End
     * @param keepInventory {@code true} if the player's inventory and XP are kept, such as when
     *                      {@link GameRules#KEEP_INVENTORY} is enabled or the player is in spectator mode
     * @param sameCharacter {@code true} if the player is not switching to an unrelated body.
     *                      Can only be {@code false} with other mods installed.
     * @return {@code true} if {@link #copyForRespawn} should be called for the current respawn situation
     */
    @Contract(pure = true)
    default boolean shouldCopyForRespawn(boolean lossless, boolean keepInventory, boolean sameCharacter) {
        return lossless;
    }

    /**
     * Copy data from a component to another as part of a player respawn.
     *
     * @param original      the component to copy data from
     * @param lossless      {@code true} if the player is copied exactly, such as when coming back from the End
     * @param keepInventory {@code true} if the player's inventory and XP are kept, such as when
     *                      {@link GameRules#KEEP_INVENTORY} is enabled or the player is in spectator mode
     * @param sameCharacter {@code true} if the player is not switching to an unrelated body.
     *                      Can only be {@code false} with other mods installed.
     * @implNote the default implementation delegates to {@link #copyFrom}
     */
    default void copyForRespawn(C original, boolean lossless, boolean keepInventory, boolean sameCharacter) {
        this.copyFrom(original);
    }

    @Override
    default void copyFrom(C other) {
        NbtCompound tag = new NbtCompound();
        other.writeToNbt(tag);
        this.readFromNbt(tag);
    }
}
