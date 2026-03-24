/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2026 Ladysnake
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
package org.ladysnake.cca.internal.level;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentFactory;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v8.component.CardinalComponent;
import org.ladysnake.cca.api.v8.level.LevelComponentFactoryRegistry;
import org.ladysnake.cca.api.v8.level.LevelComponentInitializer;
import org.ladysnake.cca.internal.base.QualifiedComponentFactory;
import org.ladysnake.cca.internal.base.asm.CcaAsmHelper;
import org.ladysnake.cca.internal.base.asm.StaticComponentLoadingException;
import org.ladysnake.cca.internal.base.asm.StaticComponentPluginBase;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class StaticLevelComponentPlugin extends StaticComponentPluginBase<Level, LevelComponentInitializer> implements LevelComponentFactoryRegistry {
    public static final StaticLevelComponentPlugin INSTANCE = new StaticLevelComponentPlugin();
    private final Map<@Nullable ResourceKey<Level>, Map<ComponentKey<?>, QualifiedComponentFactory<ComponentFactory<Level, ?>>>> worldComponentFactories = new Reference2ObjectOpenHashMap<>();

    private static String getSuffix(@Nullable ResourceKey<Level> dimensionId) {
        return "WorldImpl" + (dimensionId == null ? "" : "_" + CcaAsmHelper.getJavaIdentifierName(dimensionId.identifier()));
    }

    private StaticLevelComponentPlugin() {
        super("loading a world", Level.class);
    }

    public boolean requiresStaticFactory(ResourceKey<Level> dimensionId) {
        INSTANCE.ensureInitialized();

        return this.worldComponentFactories.containsKey(dimensionId);
    }

    public ComponentContainer.Factory<Level> buildDedicatedFactory(@Nullable ResourceKey<Level> dimensionId) {
        INSTANCE.ensureInitialized();

        var compiled = new LinkedHashMap<>(this.worldComponentFactories.getOrDefault(null, Collections.emptyMap()));
        compiled.putAll(this.worldComponentFactories.getOrDefault(dimensionId, Collections.emptyMap()));

        ComponentContainer.Factory.Builder<Level> builder = ComponentContainer.Factory.builder(Level.class)
            .factoryNameSuffix(getSuffix(dimensionId));

        for (var entry : compiled.entrySet()) {
            addToBuilder(builder, entry);
        }

        return builder.build();
    }

    private <C extends CardinalComponent> void addToBuilder(ComponentContainer.Factory.Builder<Level> builder, Map.Entry<ComponentKey<?>, QualifiedComponentFactory<ComponentFactory<Level, ?>>> entry) {
        @SuppressWarnings("unchecked") var key = (ComponentKey<C>) entry.getKey();
        @SuppressWarnings("unchecked") var factory = (ComponentFactory<Level, C>) entry.getValue().factory();
        @SuppressWarnings("unchecked") var impl = (Class<C>) entry.getValue().impl();
        builder.component(key, impl, factory, entry.getValue().dependencies());
    }

    @Override
    protected Collection<EntrypointContainer<LevelComponentInitializer>> getEntrypoints() {
        return getComponentEntrypoints("cardinal-components-level", LevelComponentInitializer.class);
    }

    @Override
    protected void dispatchRegistration(LevelComponentInitializer entrypoint) {
        entrypoint.registerLevelComponentFactories(this);
    }

    @Override
    public <C extends CardinalComponent> void register(ComponentKey<C> type, ComponentFactory<Level, ? extends C> factory) {
        this.checkLoading(LevelComponentFactoryRegistry.class, "register");
        this.register0(null, type, new QualifiedComponentFactory<>(factory, type.getComponentClass(), Set.of()));
    }

    @Override
    public <C extends CardinalComponent> void register(ComponentKey<? super C> type, Class<C> impl, ComponentFactory<Level, ? extends C> factory) {
        this.checkLoading(LevelComponentFactoryRegistry.class, "register");
        this.register0(null, type, new QualifiedComponentFactory<>(factory, type.getComponentClass(), Set.of()));
    }

    @Override
    public <C extends CardinalComponent> void registerFor(ResourceKey<Level> dimensionId, ComponentKey<C> type, ComponentFactory<Level, ? extends C> factory) {
        this.checkLoading(LevelComponentFactoryRegistry.class, "register");
        this.register0(dimensionId, type, new QualifiedComponentFactory<>(factory, type.getComponentClass(), Set.of()));
    }

    @Override
    public <C extends CardinalComponent> void registerFor(ResourceKey<Level> dimensionId, ComponentKey<? super C> type, Class<C> impl, ComponentFactory<Level, ? extends C> factory) {
        this.checkLoading(LevelComponentFactoryRegistry.class, "register");
        this.register0(dimensionId, type, new QualifiedComponentFactory<>(factory, impl, Set.of()));
    }

    private <C extends CardinalComponent> void register0(@Nullable ResourceKey<Level> dimensionId, ComponentKey<? super C> type, QualifiedComponentFactory<ComponentFactory<Level, ? extends C>> factory) {
        var specializedMap = this.worldComponentFactories.computeIfAbsent(dimensionId, t -> new LinkedHashMap<>());
        var previousFactory = specializedMap.get(type);

        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for %s on %s: %s and %s".formatted(type.getId(), dimensionId, factory, previousFactory));
        }

        @SuppressWarnings("unchecked") var factory1 = (QualifiedComponentFactory<ComponentFactory<Level, ?>>) (QualifiedComponentFactory<?>) factory;
        specializedMap.put(type, factory1);
    }
}
