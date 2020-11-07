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
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import dev.onyxstudios.cca.api.v3.component.tick.ClientTickingComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import dev.onyxstudios.cca.internal.base.LazyDispatcher;
import dev.onyxstudios.cca.internal.base.asm.CcaAsmHelper;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentLoadingException;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentPluginBase;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

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
    private final List<PredicatedComponentFactory<?>> dynamicFactories = new ArrayList<>();
    private final Map<Class<? extends BlockEntity>, Map<ComponentKey<?>, Class<? extends Component>>> beComponentImpls = new HashMap<>();
    private final Map<Class<? extends BlockEntity>, Map<ComponentKey<?>, BlockEntityComponentFactory<?, ?>>> beComponentFactories = new Reference2ObjectOpenHashMap<>();
    private final Map<Class<? extends BlockEntity>, Class<? extends ComponentContainer.Factory<BlockEntity>>> factoryClasses = new Reference2ObjectOpenHashMap<>();
    private final Set<Class<? extends BlockEntity>> clientTicking = new ReferenceOpenHashSet<>();
    private final Set<Class<? extends BlockEntity>> serverTicking = new ReferenceOpenHashSet<>();

    public Map<ComponentKey<?>, BlockComponentProvider<?>> getComponentFactories(Identifier blockId) {
        this.ensureInitialized();
        assert this.wildcard != null;
        return this.blockComponentFactories.getOrDefault(blockId, this.wildcard);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getComponentTicker(World world, T be, @Nullable BlockEntityTicker<T> base) {
        if (world.isClient && this.clientTicking.contains(be.getClass())) {
            if (base == null) return (w, pos, state, blockEntity) -> ComponentProvider.fromBlockEntity(blockEntity).getComponentContainer().tickClientComponents();
            return (w, pos, state, blockEntity) -> {
                ComponentProvider.fromBlockEntity(blockEntity).getComponentContainer().tickClientComponents();
                base.tick(w, pos, state, blockEntity);
            };
        } else if (!world.isClient && this.serverTicking.contains(be.getClass())) {
            if (base == null) return (w, pos, state, blockEntity) -> ComponentProvider.fromBlockEntity(blockEntity).getComponentContainer().tickServerComponents();
            return (w, pos, state, blockEntity) -> {
                ComponentProvider.fromBlockEntity(blockEntity).getComponentContainer().tickServerComponents();
                base.tick(w, pos, state, blockEntity);
            };
        }
        return base;
    }

    public boolean requiresStaticFactory(Class<? extends BlockEntity> entityClass) {
        StaticBlockComponentPlugin.INSTANCE.ensureInitialized();
        return entityClass == BlockEntity.class || this.beComponentFactories.containsKey(entityClass);
    }

    public Class<? extends ComponentContainer.Factory<BlockEntity>> spinDedicatedFactory(Class<? extends BlockEntity> key) {
        StaticBlockComponentPlugin.INSTANCE.ensureInitialized();

        // we need a cache as this method is called for a given class each time one of its subclasses is loaded.
        return this.factoryClasses.computeIfAbsent(key, entityClass -> {
            for (PredicatedComponentFactory<?> dynamicFactory : this.dynamicFactories) {
                dynamicFactory.tryRegister(entityClass);
            }

            Map<ComponentKey<?>, BlockEntityComponentFactory<?, ?>> compiled = new LinkedHashMap<>(this.beComponentFactories.getOrDefault(entityClass, Collections.emptyMap()));
            Map<ComponentKey<?>, Class<? extends Component>> compiledImpls = new LinkedHashMap<>(this.beComponentImpls.getOrDefault(entityClass, Collections.emptyMap()));
            Class<? extends BlockEntity> type = entityClass;

            while (type != BlockEntity.class) {
                type = type.getSuperclass().asSubclass(BlockEntity.class);
                this.beComponentFactories.getOrDefault(type, Collections.emptyMap()).forEach(compiled::putIfAbsent);
                for (Map.Entry<ComponentKey<?>, Class<? extends Component>> entry : this.beComponentImpls.getOrDefault(type, Collections.emptyMap()).entrySet()) {
                    Class<? extends Component> impl = entry.getValue();
                    if (ClientTickingComponent.class.isAssignableFrom(impl)) this.clientTicking.add(entityClass);
                    if (ServerTickingComponent.class.isAssignableFrom(impl)) this.serverTicking.add(entityClass);
                    compiledImpls.putIfAbsent(entry.getKey(), impl);
                }
            }

            String implSuffix = getSuffix(entityClass);

            try {
                Class<? extends ComponentContainer> containerCls = CcaAsmHelper.spinComponentContainer(
                    BlockEntityComponentFactory.class,
                    compiled,
                    compiledImpls,
                    implSuffix
                );

                return StaticComponentPluginBase.spinContainerFactory(
                    implSuffix,
                    ComponentContainer.Factory.class,
                    containerCls,
                    entityClass
                );
            } catch (IOException e) {
                throw new StaticComponentLoadingException("Failed to generate a dedicated component container for " + entityClass, e);
            }
        });
    }

    public <C extends Component, E extends BlockEntity> void registerFor(Class<E> target, ComponentKey<C> type, BlockEntityComponentFactory<C, E> factory) {
        this.checkLoading(BlockComponentFactoryRegistry.class, "register");
        this.register0(target, type, factory, type.getComponentClass());
    }

    private <C extends Component, F extends C, E extends BlockEntity> void register0(Class<? extends E> target, ComponentKey<? super C> type, BlockEntityComponentFactory<F, E> factory, Class<C> impl) {
        Map<ComponentKey<?>, BlockEntityComponentFactory<?, ?>> specializedMap = this.beComponentFactories.computeIfAbsent(target, t -> new LinkedHashMap<>());
        BlockEntityComponentFactory<?, ?> previousFactory = specializedMap.get(type);
        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for " + type.getId() + " on " + target + ": " + factory + " and " + previousFactory);
        }
        BlockEntityComponentFactory<Component, E> checked = entity -> Objects.requireNonNull(((BlockEntityComponentFactory<?, E>) factory).createForBlockEntity(entity), "Component factory " + factory + " for " + type.getId() + " returned null on " + entity.getClass().getSimpleName());
        specializedMap.put(type, checked);
        this.beComponentImpls.computeIfAbsent(target, t -> new LinkedHashMap<>()).put(type, impl);
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
    public <C extends Component, B extends BlockEntity> Registration<C, B> beginRegistration(Class<B> target, ComponentKey<C> key) {
        return new RegistrationImpl<>(target, key);
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

    private final class PredicatedComponentFactory<C extends Component> {
        private final Predicate<Class<? extends BlockEntity>> predicate;
        private final ComponentKey<? super C> type;
        private final BlockEntityComponentFactory<C, BlockEntity> factory;
        private final Class<C> impl;

        public PredicatedComponentFactory(Predicate<Class<? extends BlockEntity>> predicate, ComponentKey<? super C> type, BlockEntityComponentFactory<C, BlockEntity> factory, Class<C> impl) {
            this.type = type;
            this.factory = factory;
            this.predicate = predicate;
            this.impl = impl;
        }

        public void tryRegister(Class<? extends BlockEntity> clazz) {
            if (this.predicate.test(clazz)) {
                StaticBlockComponentPlugin.this.register0(clazz, this.type, this.factory, this.impl);
            }
        }
    }

    private final class RegistrationImpl<C extends Component, E extends BlockEntity> implements Registration<C, E> {
        private final Class<E> target;
        private final ComponentKey<? super C> key;
        private Class<C> componentClass;
        private Predicate<Class<? extends E>> test;

        RegistrationImpl(Class<E> target, ComponentKey<C> key) {
            this.target = target;
            this.componentClass = key.getComponentClass();
            this.test = null;
            this.key = key;
        }

        @Override
        public Registration<C, E> filter(Predicate<Class<? extends E>> test) {
            this.test = this.test == null ? test : this.test.and(test);
            return this;
        }

        @Override
        public <I extends C> Registration<I, E> impl(Class<I> impl) {
            @SuppressWarnings("unchecked") RegistrationImpl<I, E> ret = (RegistrationImpl<I, E>) this;
            ret.componentClass = impl;
            return ret;
        }

        @Override
        public void end(BlockEntityComponentFactory<C, E> factory) {
            StaticBlockComponentPlugin.this.checkLoading(Registration.class, "end");
            if (this.test == null) {
                StaticBlockComponentPlugin.this.register0(
                    this.target,
                    this.key,
                    factory,
                    this.componentClass
                );
            } else {
                StaticBlockComponentPlugin.this.dynamicFactories.add(new PredicatedComponentFactory<>(
                    c -> this.target.isAssignableFrom(c) && this.test.test(c.asSubclass(this.target)),
                    this.key,
                    entity -> factory.createForBlockEntity(this.target.cast(entity)),
                    this.componentClass
                ));
            }
        }
    }
}
