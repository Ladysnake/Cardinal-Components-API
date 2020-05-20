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
package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.ItemComponentFactoryRegistry;
import nerdhub.cardinal.components.api.component.StaticItemComponentInitializer;
import nerdhub.cardinal.components.internal.asm.CcaAsmHelper;
import nerdhub.cardinal.components.internal.asm.MethodData;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class StaticItemComponentPlugin extends DispatchingLazy implements ItemComponentFactoryRegistry {
    public static final StaticItemComponentPlugin INSTANCE = new StaticItemComponentPlugin();
    private final MethodHandles.Lookup lookup = MethodHandles.publicLookup();

    private static String getSuffix(@Nullable Identifier itemId) {
        if (itemId == null) {
            return "ItemStackImpl_All";
        }
        return "ItemStackImpl_" + CcaAsmHelper.getJavaIdentifierName(itemId);
    }

    private final Map<@Nullable Identifier, Map</*ComponentType*/Identifier, MethodData>> componentFactories = new HashMap<>();
    private final String itemStackClass = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_1799");
    private final Map<@Nullable Identifier, Class<? extends FeedbackContainerFactory<?, ?>>> factoryClasses = new HashMap<>();

    @Nullable
    public Class<? extends FeedbackContainerFactory<?, ?>> getFactoryClass(Identifier itemId) {
        CcaBootstrap.INSTANCE.ensureInitialized();
        Class<? extends FeedbackContainerFactory<?, ?>> specificFactory = this.factoryClasses.get(itemId);
        if (specificFactory != null) {
            return specificFactory;
        }
        return this.factoryClasses.get(null);
    }

    @Override
    public void register(Identifier componentId, @Nullable Identifier itemId, MethodHandle factory) {
        MethodHandleInfo factoryInfo = this.lookup.revealDirect(factory);
        MethodType factoryType = factoryInfo.getMethodType();
        if (factoryType.parameterCount() > 1 || factoryType.parameterCount() == 1 && factoryType.parameterType(0) != ItemStack.class) {
            throw new StaticComponentLoadingException("Invalid factory signature " + factory + ". Should be either no-args or a single ItemStack argument.");
        }
        Map<Identifier, MethodData> specializedMap = this.componentFactories.computeIfAbsent(itemId, t -> new HashMap<>());
        MethodData previousFactory = specializedMap.get(componentId);
        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for " + componentId + " on " + (itemId == null ? "every item" : "item '" + itemId + "'") + ": " + factory + " and " + previousFactory);
        }
        specializedMap.put(componentId, new MethodData(factoryInfo, factory));
    }

    @Override
    protected void init() {
        CcaBootstrap.INSTANCE.processSpecializedInitializers(StaticItemComponentInitializer.class,
            (initializer, provider) -> initializer.registerItemComponentFactories(this, this.lookup));
        Type itemType = Type.getObjectType(this.itemStackClass.replace('.', '/'));
        Map<Identifier, MethodData> wildcardMap = this.componentFactories.getOrDefault(null, Collections.emptyMap());
        for (Map.Entry<Identifier, Map<Identifier, MethodData>> entry : this.componentFactories.entrySet()) {
            try {
                Map<Identifier, MethodData> compiled = new HashMap<>(entry.getValue());
                wildcardMap.forEach(compiled::putIfAbsent);
                String implSuffix = getSuffix(entry.getKey());
                Class<? extends ComponentContainer<?>> containerCls = StaticComponentPluginBase.spinComponentContainer(compiled, implSuffix, itemType);
                this.factoryClasses.put(entry.getKey(), StaticComponentPluginBase.spinSingleArgFactory(implSuffix, Type.getType(containerCls), itemType));
            } catch (IOException e) {
                throw new StaticComponentLoadingException("Failed to generate a dedicated component container for " + (entry.getKey() == null ? "every item" : entry.getKey()), e);
            }
        }
    }
}
