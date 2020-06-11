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
package dev.onyxstudios.cca.api.component.util;

import com.google.common.reflect.TypeToken;
import dev.onyxstudios.cca.internal.util.ComponentContainerMetafactoryImpl;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public final class ComponentContainerMetafactory {
    /**
     * Creates a {@link ComponentContainer} factory that supports only statically declared components.
     *
     * <p>When the returned object's SAM is invoked, it will initialize a {@link ComponentContainer},
     * calling every registered factory for the given provider.
     *
     * @param genericProviderId    the id of the provider type for which components will be created,
     *                             as declared in {@link GenericComponentFactoryRegistry#register(Identifier, Identifier, TypeToken, Object)}
     * @param containerFactoryType the interface implemented by the returned container factory.
     *                             Must be a {@link FunctionalInterface}.
     * @param componentFactoryType the type of the static factories called to initialize the returned container,
     *                             as declared in {@link GenericComponentFactoryRegistry#register(Identifier, Identifier, TypeToken, Object)}.
     *                             Must be a {@link FunctionalInterface}.
     * @return a {@link ComponentContainer} factory
     */
    public static <R, C> R metafactory(Identifier genericProviderId, TypeToken<R> containerFactoryType, TypeToken<C> componentFactoryType) {
        return ComponentContainerMetafactoryImpl.metafactory(genericProviderId, containerFactoryType, componentFactoryType);
    }

    /**
     * Creates a {@link ComponentContainer} factory that supports both static and dynamic components.
     *
     * <p>When the returned object's SAM is invoked, it will initialize a {@link ComponentContainer},
     * calling every statically registered factory as well as every dynamically registered callback for the given provider.
     *
     * @param genericProviderId    the id of the provider type for which components will be created,
     *                             as declared in {@link GenericComponentFactoryRegistry#register(Identifier, Identifier, TypeToken, Object)}
     * @param containerFactoryType the interface implemented by the returned container factory.
     *                             Must be a {@link FunctionalInterface}.
     * @param componentFactoryType the type of the static factories called to initialize the returned container,
     *                             as declared in {@link GenericComponentFactoryRegistry#register(Identifier, Identifier, TypeToken, Object)}.
     *                             Must be a {@link FunctionalInterface}.
     * @param callbackType         the type of the callbacks that will be fired to dynamically initialize
     * @param events               a list of runtime events that will be fired when creating a container
     * @return a {@link ComponentContainer} factory
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <E, R, C> R metafactory(Identifier genericProviderId, TypeToken<R> containerFactoryType, TypeToken<C> componentFactoryType, Class<? super E> callbackType, Event<E>... events) {
        return ComponentContainerMetafactoryImpl.metafactory(genericProviderId, containerFactoryType, componentFactoryType, callbackType, events);
    }
}
