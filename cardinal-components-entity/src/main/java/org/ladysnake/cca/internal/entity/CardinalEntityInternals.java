/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2024 Ladysnake
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
package org.ladysnake.cca.internal.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.CopyableComponent;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;
import org.ladysnake.cca.internal.base.ComponentsInternals;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class CardinalEntityInternals {

    public static final RespawnCopyStrategy<Component> DEFAULT_COPY_STRATEGY = CardinalEntityInternals::defaultCopyStrategy;

    private CardinalEntityInternals() { throw new AssertionError(); }

    private static final Map<Class<? extends Entity>, ComponentContainer.Factory<Entity>> entityContainerFactories = new HashMap<>();
    private static final Map<ComponentKey<?>, Map<Class<? extends Entity>, RespawnCopyStrategy<?>>> respawnCopyStrategies = new HashMap<>();
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

    public static <C extends Component> void registerRespawnCopyStrat(ComponentKey<? super C> type, Class<? extends Entity> entityClass, RespawnCopyStrategy<? super C> strategy) {
        if (respawnCopyStrategies.computeIfAbsent(type, (t) -> new HashMap<>()).put(entityClass, strategy) != null) {
            ComponentsInternals.LOGGER.warn("Multiple respawn copy strategies registered for key {} and entity class {}", type, entityClass);
        }
    }

    @SuppressWarnings("unchecked")
    public static <C extends Component> RespawnCopyStrategy<? super C> getRespawnCopyStrategy(ComponentKey<C> type, Class<? extends LivingEntity> entityClass) {
        @Nullable RespawnCopyStrategy<?> strat = null;
        Class<?> c = entityClass;
        while (strat == null && LivingEntity.class.isAssignableFrom(c)) {
            strat = respawnCopyStrategies.getOrDefault(type, Map.of()).get(c);
            c = c.getSuperclass();
        }
        return strat == null ? DEFAULT_COPY_STRATEGY : (RespawnCopyStrategy<? super C>) strat;
    }

    private static void defaultCopyStrategy(Component from, Component to, RegistryWrapper.WrapperLookup registryLookup, boolean lossless, boolean keepInventory, boolean sameCharacter) {
        if (to instanceof RespawnableComponent) {
            playerComponentCopy(from, (RespawnableComponent<?>) to, registryLookup, lossless, keepInventory, sameCharacter);
        } else {
            RespawnCopyStrategy.LOSSLESS_ONLY.copyForRespawn(from, to, registryLookup, lossless, keepInventory, sameCharacter);
        }
    }

    @SuppressWarnings("unchecked")
    private static <C extends Component> void playerComponentCopy(Component from, RespawnableComponent<C> to, RegistryWrapper.WrapperLookup registryLookup, boolean lossless, boolean keepInventory, boolean sameCharacter) {
        if (to.shouldCopyForRespawn(lossless, keepInventory, sameCharacter)) {
            to.copyForRespawn((C) from, registryLookup, lossless, keepInventory, sameCharacter);
        }
    }

    @SuppressWarnings("unchecked")
    public static <C extends Component> void copyAsCopyable(Component from, CopyableComponent<C> to, RegistryWrapper.WrapperLookup registryLookup) {
        to.copyFrom((C) from, registryLookup);
    }
}
