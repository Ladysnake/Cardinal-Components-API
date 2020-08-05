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
package dev.onyxstudios.cca.internal.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import dev.onyxstudios.cca.api.component.v3.event.BlockEntityComponentCallback;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import nerdhub.cardinal.components.api.component.BlockComponentProvider;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class CardinalBlockInternals {

    private CardinalBlockInternals() { throw new AssertionError(); }

    private static final Map<Class<? extends BlockEntity>, Event<?>> BLOCK_ENTITY_EVENTS = Collections.synchronizedMap(new HashMap<>());
    private static final Map<Class<? extends BlockEntity>, DynamicContainerFactory<BlockEntity, Component>> blockEntityContainerFactories = new HashMap<>();
    private static final Object factoryMutex = new Object();
    private static final Map<Block, CompoundBlockComponentProvider> INJECTIONS = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> Event<BlockEntityComponentCallback<T>> event(Class<T> clazz) {
        Preconditions.checkArgument(BlockEntity.class.isAssignableFrom(clazz), "Entity component events must be registered on entity classes.");
        // This cast keeps the gates of hell shut. This cast keeps the darkness within. This cast ensures the feeble
        // light of the compiler never goes out, for what comes after is only death and destruction.
        // You who sees this code, turn back before it is too late. For no one must witness the horror sealed within.
        // DO NOT REMOVE THIS CAST (https://gist.github.com/Pyrofab/10892e2256ed181855b0809670cfdbbb)
        //noinspection RedundantCast
        return (Event<BlockEntityComponentCallback<T>>) BLOCK_ENTITY_EVENTS.computeIfAbsent(clazz, c ->
            EventFactory.createArrayBacked(BlockEntityComponentCallback.class, callbacks -> (BlockEntityComponentCallback<BlockEntity>) (entity, components) -> {
                for (BlockEntityComponentCallback<BlockEntity> callback : callbacks) {
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
    public static ComponentContainer<?> createEntityComponentContainer(BlockEntity entity) {
        Class<? extends BlockEntity> entityClass = entity.getClass();
        DynamicContainerFactory<BlockEntity, Component> existing = blockEntityContainerFactories.get(entityClass);
        if (existing != null) {
            return existing.create(entity);
        }
        synchronized (factoryMutex) {
            // computeIfAbsent and not put, because the factory may have been generated while waiting
            return blockEntityContainerFactories.computeIfAbsent(entityClass, cl -> {
                List<Event<?>> events = new ArrayList<>();
                Class<? extends BlockEntity> c = cl;
                Class<? extends BlockEntity> parentWithStaticComponents = null;

                while (BlockEntity.class.isAssignableFrom(c)) {
                    events.add(BlockEntityComponentCallback.event(c));
                    if (parentWithStaticComponents == null && StaticBlockEntityComponentPlugin.INSTANCE.requiresStaticFactory(c)) {   // try to find a specialized ASM factory
                        parentWithStaticComponents = c;
                    }
                    c = (Class<? extends BlockEntity>) c.getSuperclass();
                }
                assert parentWithStaticComponents != null;
                Class<? extends DynamicContainerFactory<BlockEntity,Component>> factoryClass = (Class<? extends DynamicContainerFactory<BlockEntity, Component>>) StaticBlockEntityComponentPlugin.INSTANCE.spinDedicatedFactory(new StaticBlockEntityComponentPlugin.Key(events.size(), parentWithStaticComponents));

                return ComponentsInternals.createFactory(factoryClass, Lists.reverse(events).toArray(new Event[0]));
            }).create(entity);
        }
    }

    @SuppressWarnings("unchecked")
    public static <C extends Component> void copyAsCopyable(Component from, CopyableComponent<C> to) {
        to.copyFrom((C) from);
    }

    public static BlockComponentProvider getProvider(Block block) {
        if (INJECTIONS.containsKey(block)) return INJECTIONS.get(block);
        return (BlockComponentProvider) block;
    }

    public static void addSecondaryProvider(Block block, BlockComponentProvider provider) {
        CompoundBlockComponentProvider compound = INJECTIONS.computeIfAbsent(block, bl -> new CompoundBlockComponentProvider((BlockComponentProvider) block));
        compound.addSecondary(provider);
    }
}
