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

import nerdhub.cardinal.components.api.ChunkComponentFactory;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.internal.asm.CcaAsmHelper;
import nerdhub.cardinal.components.internal.asm.FactoryClassScanner;
import nerdhub.cardinal.components.internal.asm.NamedMethodDescriptor;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class StaticChunkComponentPlugin implements StaticComponentPlugin {
    public static final String CHUNK_IMPL_SUFFIX = "ChunkImpl";
    public static final StaticChunkComponentPlugin INSTANCE = new StaticChunkComponentPlugin();

    private final Map<String, NamedMethodDescriptor> componentFactories = new HashMap<>();
    private final String chunkClass = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_2791");
    private Class<? extends FeedbackContainerFactory<?, ?>> factoryClass;

    public Class<? extends FeedbackContainerFactory<?, ?>> getFactoryClass() {
        return Objects.requireNonNull(this.factoryClass, "PreLaunch not fired ?!");
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return ChunkComponentFactory.class;
    }

    @Override
    public String scan(FactoryClassScanner.AsmFactoryData data, MethodNode method) {
        NamedMethodDescriptor factoryDescriptor = data.getFactoryDescriptor();
        if (factoryDescriptor.args.length > 1) {
            throw new StaticComponentLoadingException("Too many arguments in methods " + factoryDescriptor + ". Should be either no-args or a single " + chunkClass + " argument.");
        }
        String value = (String) data.get("value");
        componentFactories.put(value, factoryDescriptor);
        return value;
    }

    @Override
    public void generate() {
        Type chunkType = Type.getObjectType(chunkClass.replace('.', '/'));
        Class<? extends ComponentContainer<?>> containerCls = CcaAsmHelper.defineContainer(this.componentFactories, CHUNK_IMPL_SUFFIX, chunkType);
        this.factoryClass = CcaAsmHelper.createSingleArgFactory(CHUNK_IMPL_SUFFIX, Type.getType(containerCls), chunkType);
    }
}
