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

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.LevelComponentFactory;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.WorldProperties;
import org.jetbrains.annotations.ApiStatus;

/**
 * The callback interface for receiving component initialization events
 * during {@code LevelProperty} construction.
 *
 * <p> The element that is interested in attaching components
 * to levels implements this interface, and is registered
 * with the event, using {@link Event#register(Object)}.
 * When a {@code LevelProperties} object is constructed, the callback's
 * {@link #initComponents} method is invoked.
 */
@FunctionalInterface    // TODO rename the interface when yarn updates
public interface LevelComponentCallback extends ComponentCallback<WorldProperties, Component> {

    Event<LevelComponentCallback> EVENT = EventFactory.createArrayBacked(LevelComponentCallback.class,
        (callbacks) -> (level, components) -> {
            for (LevelComponentCallback callback : callbacks) {
                callback.initComponents(level, components);
            }
        });

    @ApiStatus.Experimental
    static <C extends Component> void register(ComponentType<C> type, LevelComponentFactory<C> factory) {
        EVENT.register((level, components) -> {
            Component c = factory.createForSave(level);
            if (c != null) {
                components.put(type, c);
            }
        });
    }

    /**
     * Initialize components for the given level properties object.
     * Components that are added to the given container will be available
     * on the level as soon as all callbacks have been invoked.
     *
     * @param level      the level being constructed
     * @param components the level's component container
     */
    @Override
    void initComponents(WorldProperties level, ComponentContainer<Component> components);
}
