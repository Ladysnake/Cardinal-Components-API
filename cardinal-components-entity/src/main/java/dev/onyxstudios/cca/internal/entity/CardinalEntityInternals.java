/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2022 OnyxStudios
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
import net.minecraft.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class CardinalEntityInternals {

    public static final RespawnCopyStrategy<Component> DEFAULT_COPY_STRATEGY = CardinalEntityInternals::defaultCopyStrategy;

    private CardinalEntityInternals() { throw new AssertionError(); }

    private static final Map<Class<? extends Entity>, ComponentContainer.Factory<Entity>> entityContainerFactories = new HashMap<>();
    private static final Map<ComponentKey<?>, RespawnCopyStrategy<?>> respawnCopyStrategies = new HashMap<>();
    private static final Object factoryMutex = new Object();

    /**
     * Gets a container factory for an entity class, or creates one if none exists.
     * The container factory will populate the container by invoking events for that class
     * and every superclass, in order from least specific (Entity) to most specific ({@code clazz}).
     */
    public static ComponentContainer createEntityComponentContainer(Entity entity) {
        Class<? extends Entity> entityClass = entity.getClass();

        return Objects.requireNonNullElseGet(
            entityContainerFactories.get(entityClass),  // Non-synchronized fast path
            () -> getEntityFactory(entityClass)
        ).createContainer(entity);
    }

    // can be called from both client and server thread, see issue #26
    private static synchronized ComponentContainer.Factory<Entity> getEntityFactory(Class<? extends Entity> entityClass) {
        // need to check again despite synchronization, because
        // 1- recursive calls
        // 2- the factory may have been generated while waiting from createComponents
        ComponentContainer.Factory<Entity> existing = entityContainerFactories.get(entityClass);
        if (existing != null) return existing;

        ComponentContainer.Factory<Entity> factory;
        if (StaticEntityComponentPlugin.INSTANCE.requiresStaticFactory(entityClass)) {
            factory = StaticEntityComponentPlugin.INSTANCE.buildDedicatedFactory(entityClass);
        } else {
            @SuppressWarnings("unchecked") var superclass = (Class<? extends Entity>) entityClass.getSuperclass();
            assert Entity.class.isAssignableFrom(superclass) : "requiresStaticFactory returned false on Entity?";
            factory = /* recursive call */ getEntityFactory(superclass);
        }
        entityContainerFactories.put(entityClass, factory);
        return factory;
    }

    public static <C extends Component> void registerRespawnCopyStrat(ComponentKey<? super C> type, RespawnCopyStrategy<? super C> strategy) {
        respawnCopyStrategies.put(type, strategy);
    }

    @SuppressWarnings("unchecked")
    public static <C extends Component> RespawnCopyStrategy<? super C> getRespawnCopyStrategy(ComponentKey<C> type) {
        return (RespawnCopyStrategy<? super C>) respawnCopyStrategies.getOrDefault(type, DEFAULT_COPY_STRATEGY);
    }

    private static void defaultCopyStrategy(Component from, Component to, boolean lossless, boolean keepInventory, boolean sameCharacter) {
        if (to instanceof PlayerComponent) {
            playerComponentCopy(from, (PlayerComponent<?>) to, lossless, keepInventory, sameCharacter);
        } else {
            RespawnCopyStrategy.LOSSLESS_ONLY.copyForRespawn(from, to, lossless, keepInventory, sameCharacter);
        }
    }

    @SuppressWarnings("unchecked")
    private static <C extends Component> void playerComponentCopy(Component from, PlayerComponent<C> to, boolean lossless, boolean keepInventory, boolean sameCharacter) {
        if (to.shouldCopyForRespawn(lossless, keepInventory, sameCharacter)) {
            to.copyForRespawn((C) from, lossless, keepInventory, sameCharacter);
        }
    }

    @SuppressWarnings("unchecked")
    public static <C extends Component> void copyAsCopyable(Component from, CopyableComponent<C> to) {
        to.copyFrom((C) from);
    }
}
