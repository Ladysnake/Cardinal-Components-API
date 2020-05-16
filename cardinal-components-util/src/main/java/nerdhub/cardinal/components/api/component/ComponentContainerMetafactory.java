package nerdhub.cardinal.components.api.component;

import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import nerdhub.cardinal.components.api.event.ComponentCallback;
import nerdhub.cardinal.components.internal.util.ComponentContainerMetafactoryImpl;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;

@ApiStatus.Experimental
public final class ComponentContainerMetafactory {
    /**
     * Creates a {@link ComponentContainer} factory implementing a functional interface.
     *
     * <p>When the returned object's SAM is invoked, it will initialize a {@link ComponentContainer},
     * calling every {@link GenericComponentFactory} which {@link GenericComponentFactory#targets()} array
     * contains the {@code genericTypeId}.
     *
     * @param genericTypeId the id of the provider type for which components will be created,
     *                      as declared in {@link GenericComponentFactory#targets()}
     * @param interfaceType a class object representing a {@link FunctionalInterface}.
     * @return a {@link ComponentContainer} factory implementing {@code interfaceType}
     * @throws IllegalArgumentException     if {@code interfaceType} is not an interface
     * @throws ContainerGenerationException if {@code interfaceType} is not a valid functional interface,
     *                                      or if its single abstract method's return type is not {@link ComponentContainer},
     *                                      or if the factory generation fails
     */
    public static <I> I staticMetafactory(Identifier genericTypeId, Class<I> interfaceType) {
        return ComponentContainerMetafactoryImpl.staticMetafactory(genericTypeId, interfaceType);
    }

    /**
     * Creates a {@link ComponentContainer} factory implementing a functional interface.
     *
     * <p>When the returned object's SAM is invoked, it will initialize a {@link ComponentContainer},
     * calling every {@link GenericComponentFactory} which {@link GenericComponentFactory#targets()} array
     * contains the {@code genericTypeId}.
     *
     * @param genericTypeId       the id of the provider type for which components will be created,
     *                            as declared in {@link GenericComponentFactory#targets()}
     * @param interfaceType       a class object representing a {@link FunctionalInterface}.
     * @param containedType       the type of components held by this container.
     *                            For example, some providers require all components to be {@link CopyableComponent}
     * @param actualArgumentTypes eg. if {@code argClass} is {@code BiFunction.class}, {@code actualArgumentTypes} may be
     *                            {@code (PlayerEntity.class, UUID.class)}.
     *                            If left empty, the SAM's declared argument types will be used.
     * @param <I>                 the parameterized type of the functional interface
     * @return a {@link ComponentContainer} factory implementing {@code interfaceType}
     * @throws IllegalArgumentException if {@code interfaceType} is not a functional interface,
     *                                  or if its single abstract method's return type is not
     *                                  {@linkplain Class#isAssignableFrom(Class) assignable from} {@link ComponentContainer},
     *                                  or if {@code actualArgumentTypes} are not compatible with the SAM's declared arguments.
     */
    public static <I> I staticMetafactory(Identifier genericTypeId, Class<? super I> interfaceType, Class<? extends Component> containedType, Class<?>... actualArgumentTypes) {
        return ComponentContainerMetafactoryImpl.staticMetafactory(genericTypeId, interfaceType, containedType, actualArgumentTypes);
    }

    /**
     * Creates a {@link ComponentContainer} factory that supports both static and dynamic components.
     *
     * <p>When the returned object's SAM is invoked, it will initialize a {@link ComponentContainer},
     * calling every {@link GenericComponentFactory} which {@link GenericComponentFactory#targets()} array
     * contains the {@code genericTypeId}. It will also fire passed {@code events} to populate the container dynamically.
     *
     * @param genericTypeId the id of the provider type for which components will be created,
     *                      as declared in {@link GenericComponentFactory#targets()}
     * @param containedType the type of components held by this container.
     *                      For example, some providers require all components to be {@link CopyableComponent}
     * @param argType       the type of the argument taken by the resulting container factory and forwarded to collected component factories
     * @param events a list of runtime events that will be fired when creating a container
     * @return a {@link ComponentContainer} factory
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T, C extends Component> Function<T, ComponentContainer<C>> dynamicMetafactory(Identifier genericTypeId, Class<C> containedType, Class<T> argType, Event<? extends ComponentCallback<T, C>>... events) {
        return ComponentContainerMetafactoryImpl.dynamicMetafactory(genericTypeId, argType, containedType, events);
    }
}
