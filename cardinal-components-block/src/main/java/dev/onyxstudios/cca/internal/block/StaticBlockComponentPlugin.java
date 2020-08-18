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

import dev.onyxstudios.cca.api.v3.block.BlockComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.block.BlockComponentInitializer;
import dev.onyxstudios.cca.api.v3.block.BlockComponentProvider;
import dev.onyxstudios.cca.api.v3.block.BlockEntityComponentFactory;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.internal.base.DynamicContainerFactory;
import dev.onyxstudios.cca.internal.base.LazyDispatcher;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentLoadingException;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import nerdhub.cardinal.components.api.component.Component;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public final class StaticBlockComponentPlugin extends LazyDispatcher implements BlockComponentFactoryRegistry {
    public static final StaticBlockComponentPlugin INSTANCE = new StaticBlockComponentPlugin();
    private static String getSuffix(Class<? extends BlockEntity> key) {
        String simpleName = key.getSimpleName();
        return String.format("BlockEntityImpl_%s_%s", simpleName, Integer.toHexString(key.getName().hashCode()));
    }

    private StaticBlockComponentPlugin() {
        super("creating a BlockEntity");
    }

    private Map<ComponentKey<?>, BlockComponentProvider<?>> wildcard;
    private final Map<Identifier, Map<ComponentKey<?>, BlockComponentProvider<?>>> blockComponentFactories = new HashMap<>();
    private final Map<Class<? extends BlockEntity>, Map<ComponentKey<?>, BlockEntityComponentFactory<?, ?>>> beComponentFactories = new Reference2ObjectOpenHashMap<>();
    private final Map<Class<? extends BlockEntity>, Class<? extends DynamicContainerFactory<BlockEntity>>> factoryClasses = new Reference2ObjectOpenHashMap<>();

    public Map<ComponentKey<?>, BlockComponentProvider<?>> getComponentFactories(Identifier blockId) {
        this.ensureInitialized();
        assert this.wildcard != null;
        return this.blockComponentFactories.getOrDefault(blockId, this.wildcard);
    }

    public boolean requiresStaticFactory(Class<? extends BlockEntity> entityClass) {
        StaticBlockComponentPlugin.INSTANCE.ensureInitialized();
        return entityClass == BlockEntity.class || this.beComponentFactories.containsKey(entityClass);
    }

    public Class<? extends DynamicContainerFactory<BlockEntity>> spinDedicatedFactory(Class<? extends BlockEntity> key) {
        StaticBlockComponentPlugin.INSTANCE.ensureInitialized();

        // we need a cache as this method is called for a given class each time one of its subclasses is loaded.
        return this.factoryClasses.computeIfAbsent(key, entityClass -> {

            Map<ComponentKey<?>, BlockEntityComponentFactory<?, ?>> compiled = new LinkedHashMap<>(this.beComponentFactories.getOrDefault(key, Collections.emptyMap()));
            Class<? extends BlockEntity> type = entityClass;

            while (type != BlockEntity.class) {
                type = type.getSuperclass().asSubclass(BlockEntity.class);
                this.beComponentFactories.getOrDefault(entityClass, Collections.emptyMap()).forEach(compiled::putIfAbsent);
            }

            String implSuffix = getSuffix(entityClass);

            try {
                Class<? extends ComponentContainer> containerCls = StaticComponentPluginBase.spinComponentContainer(BlockEntityComponentFactory.class, compiled, implSuffix);

                return StaticComponentPluginBase.spinContainerFactory(
                    implSuffix,
                    DynamicContainerFactory.class,
                    containerCls,
                    null,
                    0,
                    entityClass
                );
            } catch (IOException e) {
                throw new StaticComponentLoadingException("Failed to generate a dedicated component container for " + entityClass, e);
            }
        });
    }

    public <C extends Component, E extends BlockEntity> void registerFor(Class<E> target, ComponentKey<C> type, BlockEntityComponentFactory<C, E> factory) {
        this.checkLoading(BlockComponentFactoryRegistry.class, "register");
        Map<ComponentKey<?>, BlockEntityComponentFactory<?, ?>> specializedMap = this.beComponentFactories.computeIfAbsent(target, t -> new HashMap<>());
        BlockEntityComponentFactory<?, ?> previousFactory = specializedMap.get(type);
        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for " + type.getId() + " on " + target + ": " + factory + " and " + previousFactory);
        }
        BlockEntityComponentFactory<Component, E> checked = entity -> Objects.requireNonNull(((BlockEntityComponentFactory<?, E>) factory).createForBlockEntity(entity), "Component factory " + factory + " for " + type.getId() + " returned null on " + entity.getClass().getSimpleName());
        specializedMap.put(type, checked);
    }

    @Override
    public <C extends Component> void registerFor(@Nullable Identifier blockId, ComponentKey<? super C> type, BlockComponentProvider<C> factory) {
        this.checkLoading(BlockComponentFactoryRegistry.class, "register");
        Map<ComponentKey<?>, BlockComponentProvider<?>> specializedMap = this.blockComponentFactories.computeIfAbsent(blockId, t -> new Reference2ObjectOpenHashMap<>());
        BlockComponentProvider<?> previousFactory = specializedMap.get(type);
        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for " + type.getId() + " on " + (blockId == null ? "every block" : "block '" + blockId + "'") + ": " + factory + " and " + previousFactory);
        }
        specializedMap.put(type, factory);
    }

    @Override
    protected void init() {
        StaticComponentPluginBase.processInitializers(
            FabricLoader.getInstance().getEntrypointContainers("cardinal-components-block", BlockComponentInitializer.class),
            initializer -> initializer.registerBlockComponentFactories(this)
        );
        this.wildcard = this.blockComponentFactories.getOrDefault(null, Collections.emptyMap());
        this.blockComponentFactories.forEach((id, map) -> {
            if (id != null) this.wildcard.forEach(map::putIfAbsent);
        });
    }
}
