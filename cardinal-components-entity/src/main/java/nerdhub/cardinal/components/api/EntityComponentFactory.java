package nerdhub.cardinal.components.api;

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Applied to a method to declare it as a component factory for entities.
 *
 * <p>The annotated method must take either no arguments, or 1 argument of type {@link Entity}
 * or one of its subclasses (eg. {@code PlayerEntity}). It must return a non-null {@link Component}.
 */
@Nonnull    // annotated methods must not return null
@TypeQualifierDefault(ElementType.METHOD)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityComponentFactory {
    /**
     * The id of the {@link ComponentType} which this factory makes components for.
     *
     * <p> The returned string must be a valid {@link net.minecraft.util.Identifier}.
     * A {@link ComponentType} with the same id must be registered during mod initialization
     * using {@link ComponentRegistry#registerIfAbsent(Identifier, Class)}.
     *
     * @return a string representing the id of a component type
     */
    String value();

    /**
     * Defines the target entity class. The factory will be called for every
     * entity that {@linkplain Class#isInstance(Object) is an instance} of the given class.
     *
     * <p> If this property is not <strong>explicitly defined</strong>,
     * the target entity class will be inferred from the factory's first
     * parameter. If the factory takes no argument and the target is not explicitly
     * defined, or the factory's first argument is not {@linkplain Class#isAssignableFrom(Class) assignable from}
     * the explicit target class, the factory is invalid.
     *
     * <p> The given value should always be the most specific entity
     * class for the provider's use. For example, a factory which goal is to attach a component
     * to players should use {@code PlayerEntity.class},
     * not one of its superclasses. This limits the need for entity-dependant
     * checks, as well as the amount of redundant factory invocations.
     *
     * @return The class object representing the desired entity type
     */
    Class<? extends Entity> targets() default Entity.class;
}
