/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2021 OnyxStudios
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
package dev.onyxstudios.cca.internal.util;

import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.common.reflect.TypeToken;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.util.ContainerGenerationException;
import dev.onyxstudios.cca.internal.base.ComponentsInternals;
import dev.onyxstudios.cca.internal.base.asm.CcaAsmHelper;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentLoadingException;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class ComponentContainerMetafactoryImpl {

    @SuppressWarnings("unchecked")
    private static final Event<Object>[] ZERO_EVENT = (Event<Object>[]) new Event[0];

    public static <E, R, F> R metafactory(Identifier genericTypeId, TypeToken<R> containerFactoryType, TypeToken<F> componentFactoryType, @Nullable Class<? super E> callbackType, Event<E>[] events) {
        Invokable<?, ?> containerFactorySam = containerFactoryType.method(CcaAsmHelper.findSam(containerFactoryType.getRawType()));
        TypeToken<?>[] declaredArgumentTypes = containerFactorySam.getParameters().stream().map(Parameter::getType).toArray(TypeToken<?>[]::new);
        Invokable<F, ?> componentFactorySam = componentFactoryType.method(CcaAsmHelper.findSam(componentFactoryType.getRawType()));
        TypeToken<?>[] actualArgumentTypes = componentFactorySam.getParameters().stream().map(Parameter::getType).toArray(TypeToken<?>[]::new);

        if (containerFactorySam.getReturnType().getRawType() != ComponentContainer.class) {
            throw new ContainerGenerationException("Declared return type of SAM " + containerFactorySam + " is not " + ComponentContainer.class.getSimpleName());
        }

        if (actualArgumentTypes.length != declaredArgumentTypes.length) {
            throw new ContainerGenerationException("Actual and declared argument type lists differ in length: " + Arrays.stream(actualArgumentTypes).map(TypeToken::getRawType).map(Class::getSimpleName).collect(Collectors.joining(", ", "[", "]")) + ", " + Arrays.stream(declaredArgumentTypes).map(TypeToken::getRawType).map(Class::getSimpleName).collect(Collectors.joining(", ", "[", "]")) + " (component factory type: " + componentFactoryType + ", container factory type: " + containerFactoryType + ")");
        }

        for (int i = 0; i < declaredArgumentTypes.length; i++) {
            if (!declaredArgumentTypes[i].isSupertypeOf(actualArgumentTypes[i])) {
                throw new ContainerGenerationException(actualArgumentTypes[i] + " is not a valid specialization of declared argument " + declaredArgumentTypes[i]);
            }
        }

        try {
            Class<? extends R> containerFactoryClass = StaticGenericComponentPlugin.INSTANCE.spinSingleArgContainerFactory(componentFactoryType, genericTypeId, containerFactoryType.getRawType(), Arrays.stream(actualArgumentTypes).map(TypeToken::getRawType).toArray(Class<?>[]::new));
            return ComponentsInternals.createFactory(containerFactoryClass);
        } catch (StaticComponentLoadingException | IOException e) {
            throw new ContainerGenerationException("Failed to generate metafactory for " + genericTypeId, e);
        }
    }

    public static <R, C> R metafactory(Identifier genericProviderId, TypeToken<R> containerFactoryType, TypeToken<C> componentFactoryType) {
        return metafactory(genericProviderId, containerFactoryType, componentFactoryType, null, ZERO_EVENT);
    }
}
