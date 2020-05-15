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

import nerdhub.cardinal.components.api.ItemComponentFactory;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.internal.asm.AnnotationData;
import nerdhub.cardinal.components.internal.asm.MethodData;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

public final class StaticItemComponentPlugin implements StaticComponentPlugin {
    public static final StaticItemComponentPlugin INSTANCE = new StaticItemComponentPlugin();

    private static String getSuffix(String itemId) {
        if (itemId.equals(ItemComponentFactory.WILDCARD)) {
            return "ItemStackImpl_All";
        }
        return "ItemStackImpl_" + itemId.replace(':', '$').replace('/', '$');
    }

    private final Map</*Identifier*/String, Map</*ComponentType*/String, MethodData>> componentFactories = new HashMap<>();
    private final String itemStackClass = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_1799");
    private final Map</*Identifier*/String, Class<? extends FeedbackContainerFactory<?, ?>>> factoryClasses = new HashMap<>();

    @Nullable
    public Class<? extends FeedbackContainerFactory<?, ?>> getFactoryClass(String itemId) {
        Class<? extends FeedbackContainerFactory<?, ?>> specificFactory = this.factoryClasses.get(itemId);
        if (specificFactory != null) {
            return specificFactory;
        }
        return this.factoryClasses.get(ItemComponentFactory.WILDCARD);
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return ItemComponentFactory.class;
    }

    @Override
    public String scan(MethodData factory, AnnotationData annotation) {
        Type[] factoryArgs = factory.descriptor.getArgumentTypes();
        if (factoryArgs.length > 1) {
            throw new StaticComponentLoadingException("Too many arguments in method " + factory + ". Should be either no-args or a single " + this.itemStackClass + " argument.");
        }
        List<String> targets = annotation.get("targets", List.class);
        HashSet<String> resolvedTargets = targets.stream().map(id -> id.indexOf(':') >= 0 || id.equals(ItemComponentFactory.WILDCARD) ? id : "minecraft:" + id).collect(Collectors.toCollection(HashSet::new));
        if (targets.size() != resolvedTargets.size()) {
            throw new StaticComponentLoadingException("ItemStack component factory '" + factory + "' is trying to subscribe with duplicate item ids (" + String.join(", ", targets) + ")");
        }
        String value = annotation.get("value", String.class);
        for (String target : resolvedTargets) {
            if (target.equals(ItemComponentFactory.WILDCARD)) {
                if (resolvedTargets.size() > 1) {
                    throw new StaticComponentLoadingException("ItemStack component factory '" + factory + "' is trying to subscribe with both wildcard and specific ids (" + String.join(", ", targets) + ")");
                }
            } else if (!IDENTIFIER_PATTERN.matcher(target).matches()) {
                throw new StaticComponentLoadingException("ItemStack component factory '" + factory + "' is subscribing with invalid id: " + target);
            }
            Map<String, MethodData> specializedMap = this.componentFactories.computeIfAbsent(target, t -> new HashMap<>());
            MethodData previousFactory = specializedMap.get(value);
            if (previousFactory != null) {
                throw new StaticComponentLoadingException("Duplicate factory declarations for " + value + " on item id '" + target + "': " + factory + " and " + previousFactory);
            }
            specializedMap.put(value, factory);
        }
        return value;
    }

    @Override
    public void generate() throws IOException {
        Type itemType = Type.getObjectType(this.itemStackClass.replace('.', '/'));
        Map<String, MethodData> wildcardMap = this.componentFactories.getOrDefault(ItemComponentFactory.WILDCARD, Collections.emptyMap());
        for (Map.Entry</*Identifier*/String, Map</*ComponentType*/String, MethodData>> entry : this.componentFactories.entrySet()) {
            Map<String, MethodData> compiled = new HashMap<>(entry.getValue());
            wildcardMap.forEach(compiled::putIfAbsent);
            String implSuffix = getSuffix(entry.getKey());
            Class<? extends ComponentContainer<?>> containerCls = StaticComponentPluginBase.spinComponentContainer(compiled, implSuffix, itemType);
            this.factoryClasses.put(entry.getKey(), StaticComponentPluginBase.spinSingleArgFactory(implSuffix, Type.getType(containerCls), itemType));
        }
    }
}
