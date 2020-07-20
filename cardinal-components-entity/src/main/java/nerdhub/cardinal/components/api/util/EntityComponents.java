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
package nerdhub.cardinal.components.api.util;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.internal.entity.CardinalEntityInternals;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import org.jetbrains.annotations.ApiStatus;

public final class EntityComponents {

    /**
     * Set the respawn copy strategy used for components of a given type.
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
     * @see #getRespawnCopyStrategy(ComponentType)
     */
    public static <C extends Component> void setRespawnCopyStrategy(ComponentType<C> type, RespawnCopyStrategy<? super C> strategy) {
        CardinalEntityInternals.registerRespawnCopyStrat(type, strategy);
    }

    public static <C extends Component> RespawnCopyStrategy<C> getRespawnCopyStrategy(ComponentType<C> type) {
        return CardinalEntityInternals.getRespawnCopyStrat(type);
    }

    @ApiStatus.Experimental
    public static <C extends Component> RespawnCopyStrategy<C> getRespawnCopyStrategy(ComponentKey<C> type) {
        return CardinalEntityInternals.getRespawnCopyStrat(type);
    }
}
