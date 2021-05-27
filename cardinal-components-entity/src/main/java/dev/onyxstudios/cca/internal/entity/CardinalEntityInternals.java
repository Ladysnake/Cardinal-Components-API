/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2021 OnyxStudios
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
package dev.onyxstudios.cca.internal.entity;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.CopyableComponent;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import net.minecraft.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public final class CardinalEntityInternals {

    public static final RespawnCopyStrategy<Component> DEFAULT_COPY_STRATEGY = CardinalEntityInternals::defaultCopyStrategy;

    private CardinalEntityInternals() { throw new AssertionError(); }

    private static final Map<Class<? extends Entity>, ComponentContainer.Factory<Entity>> entityContainerFactories = new HashMap<>();
    private static final Map<ComponentKey<?>, RespawnCopyStrategy<?>> RESPAWN_COPY_STRATEGIES = new HashMap<>();
    private static final Object factoryMutex = new Object();

    /**
     * Gets a container factory for an entity class, or creates one if none exists.
     * The container factory will populate the container by invoking events for that class
     * and every superclass, in order from least specific (Entity) to most specific ({@code clazz}).
     */
    @SuppressWarnings("unchecked")
    public static ComponentContainer createEntityComponentContainer(Entity entity) {
        Class<? extends Entity> entityClass = entity.getClass();
        ComponentContainer.Factory<Entity> existing = entityContainerFactories.get(entityClass);

        if (existing != null) {
            return existing.createContainer(entity);
        }

        synchronized (factoryMutex) {   // can be called from both client and server thread, see #
            // computeIfAbsent and not put, because the factory may have been generated while waiting
            return entityContainerFactories.computeIfAbsent(entityClass, cl -> {
                Class<? extends Entity> c = cl;
                Class<? extends Entity> parentWithStaticComponents = null;

                while (Entity.class.isAssignableFrom(c)) {
                    if (parentWithStaticComponents == null && StaticEntityComponentPlugin.INSTANCE.requiresStaticFactory(c)) {   // try to find a specialized ASM factory
                        parentWithStaticComponents = c;
                    }
                    c = (Class<? extends Entity>) c.getSuperclass();
                }
                assert parentWithStaticComponents != null;
                Class<? extends ComponentContainer.Factory<Entity>> factoryClass = (Class<? extends ComponentContainer.Factory<Entity>>) StaticEntityComponentPlugin.INSTANCE.spinDedicatedFactory(parentWithStaticComponents);

                return ComponentsInternals.createFactory(factoryClass);
            }).createContainer(entity);
        }
    }

    public static <C extends Component> void registerRespawnCopyStrat(ComponentKey<? super C> type, RespawnCopyStrategy<? super C> strategy) {
        RESPAWN_COPY_STRATEGIES.put(type, strategy);
    }

    @SuppressWarnings("unchecked")
    public static <C extends Component> RespawnCopyStrategy<? super C> getRespawnCopyStrategy(ComponentKey<C> type) {
        return (RespawnCopyStrategy<? super C>) RESPAWN_COPY_STRATEGIES.getOrDefault(type, DEFAULT_COPY_STRATEGY);
    }

    private static void defaultCopyStrategy(Component from, Component to, boolean lossless, boolean keepInventory) {
        if (to instanceof PlayerComponent) {
            playerComponentCopy(from, (PlayerComponent<?>) to, lossless, keepInventory);
        } else {
            RespawnCopyStrategy.LOSSLESS_ONLY.copyForRespawn(from, to, lossless, keepInventory);
        }
    }

    @SuppressWarnings("unchecked")
    private static <C extends Component> void playerComponentCopy(Component from, PlayerComponent<C> to, boolean lossless, boolean keepInventory) {
        if (to.shouldCopyForRespawn(lossless, keepInventory)) {
            to.copyForRespawn((C) from, lossless, keepInventory);
        }
    }

    @SuppressWarnings("unchecked")
    public static <C extends Component> void copyAsCopyable(Component from, CopyableComponent<C> to) {
        to.copyFrom((C) from);
    }
}
