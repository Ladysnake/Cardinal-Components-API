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
import nerdhub.cardinal.components.api.component.EntityComponentFactoryRegistry;
import nerdhub.cardinal.components.api.component.StaticEntityComponentInitializer;
import nerdhub.cardinal.components.internal.asm.MethodData;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class StaticEntityComponentPlugin extends DispatchingLazy implements EntityComponentFactoryRegistry {
    public static final StaticEntityComponentPlugin INSTANCE = new StaticEntityComponentPlugin();

    private static String getSuffix(Class<?> entityClass) {
        String simpleName = entityClass.getSimpleName();
        return String.format("EntityImpl_%s_%s", simpleName, Integer.toHexString(entityClass.getName().hashCode()));
    }

    private final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
    private final Map<Class<? extends Entity>, Map</*ComponentType*/Identifier, MethodData>> componentFactories = new HashMap<>();
    private final Map<Class<? extends Entity>, Class<? extends FeedbackContainerFactory<?, ?>>> factoryClasses = new HashMap<>();

    @Nullable
    public Class<? extends FeedbackContainerFactory<?, ?>> spinDedicatedFactory(Class<? extends Entity> cl) {
        this.ensureInitialized();

        if (!this.componentFactories.containsKey(cl)) {
            // the caller is already iterating superclasses, we only care about whether this specific class has components
            return null;
        }

        // we need a cache as this method is called for a given class each time one of its subclasses is loaded.
        return this.factoryClasses.computeIfAbsent(cl, entityClass -> {
            Map<Identifier, MethodData> compiled = new LinkedHashMap<>(this.componentFactories.get(entityClass));
            Class<?> type = entityClass;

            while (type != Entity.class) {
                type = type.getSuperclass();
                this.componentFactories.getOrDefault(type, Collections.emptyMap()).forEach(compiled::putIfAbsent);
            }

            String implSuffix = getSuffix(entityClass);
            Type entityType = Type.getType(Entity.class);

            try {
                Class<? extends ComponentContainer<?>> containerCls = StaticComponentPluginBase.spinComponentContainer(compiled, implSuffix, entityType);
                return StaticComponentPluginBase.spinSingleArgFactory(implSuffix, Type.getType(containerCls), entityType);
            } catch (IOException e) {
                throw new StaticComponentLoadingException("Failed to generate a dedicated component container for " + cl, e);
            }
        });
    }

    @Override
    protected void init() {
        CcaBootstrap.INSTANCE.processSpecializedInitializers(StaticEntityComponentInitializer.class,
            (entrypoint, provider) -> entrypoint.registerEntityComponentFactories(this, this.lookup));
    }

    @Override
    public void register(Identifier componentId, Class<? extends Entity> target, MethodHandle factory) {
        MethodHandleInfo factoryInfo = this.lookup.revealDirect(factory);
        MethodType factoryType = factoryInfo.getMethodType();
        if (factoryType.parameterCount() > 1) {
            throw new StaticComponentLoadingException("Too many arguments in method " + factoryInfo + ". Should be either no-args or a single Entity argument.");
        }
        if (factoryType.parameterCount() > 0 && target != factoryType.parameterType(0)) {
            throw new IllegalStateException("Argument " + factoryType.parameterType(0) + " in method " + factoryInfo + " does not match declared target entity class " + target);
        }
        Map<Identifier, MethodData> specializedMap = this.componentFactories.computeIfAbsent(target, t -> new HashMap<>());
        MethodData previousFactory = specializedMap.get(componentId);
        if (previousFactory != null) {
            throw new StaticComponentLoadingException("Duplicate factory declarations for " + componentId + " on " + target + ": " + factory + " and " + previousFactory);
        }
        specializedMap.put(componentId, new MethodData(factoryInfo, factory));
    }
}
