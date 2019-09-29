package nerdhub.cardinal.components.internal;

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

public final class ComponentsInternals {
    private static final Field EVENT$TYPE;
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Map<Event<? extends ComponentCallback>, MethodHandle> FACTORY_CACHE = new HashMap<>();
    private static final MethodHandle IMPL_HANDLE;

    static {
        try {
            EVENT$TYPE = Class.forName("net.fabricmc.fabric.impl.event.ArrayBackedEvent").getDeclaredField("type");
            EVENT$TYPE.setAccessible(true);
            IMPL_HANDLE = LOOKUP.findStatic(ComponentsInternals.class, "initComponentsWrapper", MethodType.methodType(void.class, ComponentCallback.class, Object.class, ComponentContainer.class));
        } catch (NoSuchFieldException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to hack fabric API", e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <X, Y extends Component, E extends ComponentCallback<X, ? super Y>> E createCallback(Event<E> event, ComponentCallback<X, Y> impl) {
        try {
            return (E) FACTORY_CACHE.computeIfAbsent(event, ComponentsInternals::createCallbackFactory).invoke(impl);
        } catch (Throwable t) {
            throw new RuntimeException("Failed to instantiate hacky component callback implementation", t);
        }
    }

    @SuppressWarnings("unchecked")
    public static MethodHandle createCallbackFactory(Event<?> event) {
        try {
            Class<? extends ComponentCallback> eventType = (Class<? extends ComponentCallback>) EVENT$TYPE.get(event);
            MethodType eventSamType = findSam(eventType);
            return LambdaMetafactory.metafactory(
                LOOKUP,
                "initComponents",
                MethodType.methodType(eventType, ComponentCallback.class),
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

    private static<X, Y extends Component> void initComponentsWrapper(ComponentCallback<X, Y> wrapped, X provider, ComponentContainer<Y> components) {
        wrapped.initComponents(provider, components);
    }

    private static MethodType findSam(Class<? extends ComponentCallback> callbackClass) {
        try {
            for (Method m : callbackClass.getMethods()) {
                if (Modifier.isAbstract(m.getModifiers())) {
                    return LOOKUP.unreflect(m).type().dropParameterTypes(0, 1);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException(callbackClass + " is not a functional interface!");
    }
}
