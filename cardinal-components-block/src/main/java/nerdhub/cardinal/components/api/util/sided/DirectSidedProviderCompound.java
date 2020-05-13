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
package nerdhub.cardinal.components.api.util.sided;

import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.util.provider.FallBackComponentProvider;
import nerdhub.cardinal.components.util.provider.SimpleComponentProvider;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

/**
 * A sided compound provider that holds direct references to every side's provider.
 */
public final class DirectSidedProviderCompound implements SidedProviderCompound {
    private final SidedContainerCompound backing;

    private final ComponentProvider coreProvider;
    private final ComponentProvider northProvider;
    private final ComponentProvider southProvider;
    private final ComponentProvider eastProvider;
    private final ComponentProvider westProvider;
    private final ComponentProvider upProvider;
    private final ComponentProvider downProvider;

    public DirectSidedProviderCompound(SidedContainerCompound backing) {
        this.backing = backing;
        this.coreProvider   = new SimpleComponentProvider(backing.get(null));
        this.northProvider  = createSideProvider(Direction.NORTH);
        this.southProvider  = createSideProvider(Direction.SOUTH);
        this.eastProvider   = createSideProvider(Direction.EAST);
        this.westProvider   = createSideProvider(Direction.WEST);
        this.upProvider     = createSideProvider(Direction.UP);
        this.downProvider   = createSideProvider(Direction.DOWN);
    }

    private ComponentProvider createSideProvider(Direction side) {
        return new FallBackComponentProvider(new SimpleComponentProvider(backing.get(side)), coreProvider);
    }

    @Override
    public ComponentProvider getComponents(@Nullable Direction side) {
        if (side == null) {
            return coreProvider;
        }
        switch (side) {
            case NORTH: return northProvider;
            case SOUTH: return southProvider;
            case EAST:  return eastProvider;
            case WEST:  return westProvider;
            case UP:    return upProvider;
            case DOWN:  return downProvider;
            default: throw new IllegalStateException("Unrecognized direction: " + side);
        }
    }
}
