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

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.event.ComponentCallback;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import net.fabricmc.fabric.api.event.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class ComponentsInternals {
    public static final Logger LOGGER = LogManager.getLogger("Cardinal Components API");

    private static final Field EVENT$TYPE;
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<Event<? extends ComponentCallback<?, ?>>, MethodHandle> FACTORY_CACHE = new HashMap<>();
    private static final MethodHandle IMPL_HANDLE;

    static {
        try {
            EVENT$TYPE = Class.forName("net.fabricmc.fabric.impl.base.event.ArrayBackedEvent").getDeclaredField("type");
            EVENT$TYPE.setAccessible(true);
            IMPL_HANDLE = LOOKUP.findStatic(ComponentsInternals.class, "initComponents", MethodType.methodType(void.class, ComponentType.class, Function.class, Object.class, ComponentContainer.class));
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to hack fabric API", e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <E extends ComponentCallback<P, ? super C>, T extends Component, P, C extends T> E createCallback(Event<E> event, ComponentType<T> type, Function<P,C> factory) {
        try {
            return (E) FACTORY_CACHE.computeIfAbsent(event, ComponentsInternals::createCallbackFactory).invoke(type, factory);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to instantiate hacky component callback implementation", t);
        }
    }

    private static <T extends Component, P, C extends T> void initComponents(ComponentType<T> type, Function<P, C> factory, P provider, ComponentContainer<C> components) {
        components.put(type, factory.apply(provider));
    }

    @SuppressWarnings("unchecked")
    private static MethodHandle createCallbackFactory(Event<?> event) {
        try {
            Class<? extends ComponentCallback<?, ?>> eventType = (Class<? extends ComponentCallback<?, ?>>) EVENT$TYPE.get(event);
            MethodType eventSamType = findSam(eventType);
            return LambdaMetafactory.metafactory(
                LOOKUP,
                "initComponents",
                MethodType.methodType(eventType, ComponentType.class, Function.class),
                eventSamType,
                IMPL_HANDLE,
                eventSamType
            ).getTarget();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to hack fabric api", e);
        } catch (LambdaConversionException e) {
            throw new RuntimeException("Failed to create new implementation for a component callback", e);
        }
    }

    private static MethodType findSam(Class<? extends ComponentCallback<?, ?>> callbackClass) {
        try {
            for (Method m : callbackClass.getMethods()) {
                if (Modifier.isAbstract(m.getModifiers())) {
                    return LOOKUP.unreflect(m).type().dropParameterTypes(0, 1); // drop <this>
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException(callbackClass + " is not a functional interface!");
    }

    @Nonnull
    public static <R> R createFactory(Class<R> factoryClass, Event<?>... events) {
        try {
            Constructor<?>[] constructors = factoryClass.getConstructors();
            if (constructors.length != 1) {
                throw new IllegalArgumentException("Expected 1 constructor declaration in " + factoryClass + ", got " + Arrays.toString(constructors));
            }
            @SuppressWarnings("unchecked") R ret =
                (R) constructors[0].newInstance((Object[]) events);
            return ret;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new StaticComponentLoadingException("Failed to instantiate generated component factory", e);
        }
    }
}
