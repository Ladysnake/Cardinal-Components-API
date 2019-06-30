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
package nerdhub.cardinal.components.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CardinalEventsInternals {
    private CardinalEventsInternals() { throw new AssertionError(); }

    public static final Event<ItemComponentCallback> WILDCARD_ITEM_EVENT = createItemComponentsEvent();
    private static final Map<Class<? extends Entity>, Event> ENTITY_EVENTS = new HashMap<>();
    private static final Map<Class<? extends Entity>, FeedbackContainerFactory<Entity, Component>> ENTITY_CONTAINER_FACTORIES = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Entity> Event<EntityComponentCallback<T>> event(Class<T> clazz) {
        Preconditions.checkArgument(Entity.class.isAssignableFrom(clazz), "Entity component events must be registered on entity classes.");
        // This cast keeps the gates of hell shut. This cast keeps the darkness within. This cast ensures the feeble
        // light of the compiler never goes out, for what comes after is only death and destruction.
        // You who sees this code, turn back before it is too late. For no one must witness the horror sealed within.
        // DO NOT REMOVE THIS CAST (https://gist.github.com/Pyrofab/10892e2256ed181855b0809670cfdbbb)
        //noinspection RedundantCast
        return (Event<EntityComponentCallback<T>>) ENTITY_EVENTS.computeIfAbsent(clazz, c ->
                EventFactory.createArrayBacked(EntityComponentCallback.class, callbacks -> (EntityComponentCallback<Entity>) (entity, components) -> {
                    for (EntityComponentCallback callback : callbacks) {
                        callback.initComponents(entity, components);
                    }
                })
        );
    }

    /**
     * Gets a container factory for an entity class, or creates one if none exists.
     * The container factory will populate the container by invoking events for that class
     * and every superclass, in order from least specific (Entity) to most specific ({@code clazz}).
     */
    @SuppressWarnings("unchecked")
    public static FeedbackContainerFactory<Entity, Component> getEntityContainerFactory(Class<? extends Entity> clazz) {
        return ENTITY_CONTAINER_FACTORIES.computeIfAbsent(clazz, cl -> {
            List<Event<EntityComponentCallback<? extends Entity>>> events = new ArrayList<>();
            Class c = clazz;
            while (Entity.class.isAssignableFrom(c)) {
                events.add(EntityComponentCallback.event(c));
                c = c.getSuperclass();
            }
            return new FeedbackContainerFactory<>(Lists.reverse(events).toArray(new Event[0]));
        });
    }

    public static Event<ItemComponentCallback> createItemComponentsEvent() {
        return EventFactory.createArrayBacked(ItemComponentCallback.class,
            (listeners) -> (stack, components) -> {
                for (ItemComponentCallback listener : listeners) {
                    listener.initComponents(stack, components);
                }
            });
    }
}
