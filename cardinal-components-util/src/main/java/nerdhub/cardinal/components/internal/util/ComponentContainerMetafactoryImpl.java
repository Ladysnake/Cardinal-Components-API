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
package nerdhub.cardinal.components.internal.util;

import com.google.common.reflect.TypeToken;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.ContainerGenerationException;
import nerdhub.cardinal.components.api.event.ComponentCallback;
import nerdhub.cardinal.components.internal.ComponentsInternals;
import nerdhub.cardinal.components.internal.FeedbackContainerFactory;
import nerdhub.cardinal.components.internal.asm.CcaAsmHelper;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;

public final class ComponentContainerMetafactoryImpl {

    public static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static <I> I staticMetafactory(Identifier genericTypeId, Class<I> interfaceType, TypeToken<?> componentFactoryType) {
        if (!interfaceType.isInterface()) {
            throw new IllegalArgumentException(interfaceType + " is not an interface");
        }
        Method sam = CcaAsmHelper.findSam(interfaceType);
        Class<?>[] declaredArgumentTypes = sam.getParameterTypes();
        if (sam.getReturnType() != ComponentContainer.class) {
            throw new ContainerGenerationException("Return type of SAM " + sam + " is not " + ComponentContainer.class.getSimpleName());
        }
        return createStaticContainerFactory(genericTypeId, interfaceType, sam, componentFactoryType, declaredArgumentTypes);
    }

    public static <I> I staticMetafactory(Identifier genericTypeId, Class<? super I> interfaceType, TypeToken<?> componentFactoryType, Class<?>[] actualArgumentTypes) {
        Method sam = CcaAsmHelper.findSam(interfaceType);
        Class<?>[] declaredArgumentTypes = sam.getParameterTypes();
        if (!sam.getReturnType().isAssignableFrom(ComponentContainer.class)) {
            throw new ContainerGenerationException("Declared return type of SAM " + sam + " is not " + ComponentContainer.class.getSimpleName() + " or a superclass");
        }
        if (actualArgumentTypes.length != declaredArgumentTypes.length) {
            throw new ContainerGenerationException("Actual and declared argument type lists differ in length: " + Arrays.toString(actualArgumentTypes) + ", " + Arrays.toString(declaredArgumentTypes));
        }
        for (int i = 0; i < declaredArgumentTypes.length; i++) {
            if (!declaredArgumentTypes[i].isAssignableFrom(actualArgumentTypes[i])) {
                throw new ContainerGenerationException(actualArgumentTypes[i] + " is not a valid specialization of declared argument " + declaredArgumentTypes[i].getTypeName());
            }
        }
        return createStaticContainerFactory(genericTypeId, interfaceType, sam, componentFactoryType, actualArgumentTypes);
    }

    private static <I> I createStaticContainerFactory(Identifier genericTypeId, Class<? super I> interfaceType, Method sam, TypeToken<?> componentFactoryType, Class<?>[] argumentTypes) {
        try {
            Class<? extends ComponentContainer<?>> containerClass = StaticGenericComponentPlugin.INSTANCE.spinComponentContainer(componentFactoryType, genericTypeId);
            MethodType ctorType = MethodType.methodType(void.class, int.class).appendParameterTypes(sam.getParameterTypes());
            MethodType samType = MethodType.methodType(sam.getReturnType(), sam.getParameterTypes());
            MethodType instantiatedSamType = MethodType.methodType(sam.getReturnType(), argumentTypes);
            MethodHandle mh = LOOKUP.findConstructor(containerClass, ctorType);
            CallSite metafactory = LambdaMetafactory.metafactory(LOOKUP, sam.getName(), MethodType.methodType(interfaceType, int.class), samType, mh, instantiatedSamType);
            @SuppressWarnings("unchecked") I ret = (I) interfaceType.cast(metafactory.getTarget().invoke(0));
            return ret;
        } catch (Throwable e) {
            throw new ContainerGenerationException("Failed to generate metafactory for " + genericTypeId, e);
        }
    }

    public static <T, C extends Component> Function<T, ComponentContainer<C>> dynamicMetafactory(Identifier genericTypeId, Class<T> argClass, Class<C> componentClass, Event<? extends ComponentCallback<T, C>>[] callbacks) {
        try {
            @SuppressWarnings("unchecked") Class<? extends FeedbackContainerFactory<T, C>> containerFactoryClass = (Class<? extends FeedbackContainerFactory<T, C>>) StaticGenericComponentPlugin.INSTANCE.spinSingleArgContainerFactory(TypeToken.of(Function.class), genericTypeId, componentClass, argClass);
            return ComponentsInternals.createFactory(containerFactoryClass, callbacks)::create;
        } catch (StaticComponentLoadingException | IOException e) {
            throw new ContainerGenerationException("Failed to generate metafactory for " + genericTypeId, e);
        }
    }
}
