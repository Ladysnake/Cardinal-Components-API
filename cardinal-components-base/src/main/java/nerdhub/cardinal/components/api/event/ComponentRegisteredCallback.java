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

import dev.onyxstudios.cca.api.v3.component.Component;
import nerdhub.cardinal.components.api.ComponentType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * The callback interface for receiving component registration events.
 *
 * @see nerdhub.cardinal.components.api.ComponentRegistry#registerIfAbsent(Identifier, Class)
 */
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "3.0.0")
public interface ComponentRegisteredCallback {
    Event<ComponentRegisteredCallback> EVENT = EventFactory.createArrayBacked(ComponentRegisteredCallback.class,
        listeners -> (Identifier id, Class<? extends Component> componentClass, ComponentType<?> type) -> {
            for (ComponentRegisteredCallback listener : listeners) {
                listener.onComponentRegistered(id, componentClass, type);
            }
        });

    /**
     * Called when a new {@link ComponentType} is registered.
     *
     * @param id             the identifier with which the component type was registered
     * @param componentClass the declared type of the components
     * @param type           the {@code ComponentType} instantiated by the registration call
     */
    void onComponentRegistered(Identifier id, Class<? extends Component> componentClass, ComponentType<?> type);
}
