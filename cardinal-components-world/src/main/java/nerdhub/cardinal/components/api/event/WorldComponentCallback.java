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

import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.World;

/**
 * The callback interface for receiving component initialization events
 * during world construction. The element that is interested in attaching
 * components to worlds implements this interface, and is
 * registered with the event, using {@link Event#register(Object)}.
 * When a world is constructed, the callback's {@code initComponents} method is invoked.
 */
@FunctionalInterface
public interface WorldComponentCallback extends ComponentCallback<World, Component> {

    Event<WorldComponentCallback> EVENT = EventFactory.createArrayBacked(WorldComponentCallback.class,
            (callbacks) -> (world, components) -> {
                for (WorldComponentCallback callback : callbacks) {
                    callback.initComponents(world, components);
                }
            });

    /**
     * Initialize components for the given world.
     * Components that are added to the given container will be available
     * on the world as soon as all callbacks have been invoked.
     *
     * @param world     the world being constructed
     * @param components the world's component container
     */
    @Override
    void initComponents(World world, ComponentContainer<Component> components);
}
