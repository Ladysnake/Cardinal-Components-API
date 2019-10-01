package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.event.ComponentCallback;
import net.fabricmc.fabric.api.event.Event;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class ComponentsInternals {
    private static final Field EVENT$TYPE;
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<Event<? extends ComponentCallback>, MethodHandle> FACTORY_CACHE = new HashMap<>();
    private static final MethodHandle IMPL_HANDLE;

    static {
        try {
            EVENT$TYPE = Class.forName("net.fabricmc.fabric.impl.event.ArrayBackedEvent").getDeclaredField("type");
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

    private static <E extends ComponentCallback<P, ? super C>, T extends Component, P, C extends T> void initComponents(ComponentType<T> type, Function<P, C> factory, P provider, ComponentContainer<C> components) {
        components.put(type, factory.apply(provider));
    }

    @SuppressWarnings("unchecked")
    private static MethodHandle createCallbackFactory(Event<?> event) {
        try {
            Class<? extends ComponentCallback> eventType = (Class<? extends ComponentCallback>) EVENT$TYPE.get(event);
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

    private static MethodType findSam(Class<? extends ComponentCallback> callbackClass) {
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
}
