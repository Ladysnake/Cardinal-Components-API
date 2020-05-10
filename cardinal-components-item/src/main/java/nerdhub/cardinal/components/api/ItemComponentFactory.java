package nerdhub.cardinal.components.api;

import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Applied to a method to declare it as a component factory for {@linkplain ItemStack item stacks}.
 *
 * <p>The annotated method must take either no arguments, or 1 argument of type {@link ItemStack}.
 * It must return a non-null {@link Component}.
 */
@Nonnull    // annotated methods must not return null
@TypeQualifierDefault({ElementType.METHOD, ElementType.TYPE})
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ItemComponentFactory {
    /**
     * A magic string replacing an item id, representing every item
     */
    String WILDCARD = "*";

    /**
     * The id of the {@link ComponentType} which this factory makes components for.
     *
     * <p> The returned string must be a valid {@link Identifier}.
     * A {@link ComponentType} with the same id must be registered during mod initialization
     * using {@link ComponentRegistry#registerIfAbsent(Identifier, Class)}.
     *
     * @return a string representing the id of a component type
     */
    String value();

    /**
     * Defines the target item id. The factory will be called for every
     * item stack which id is the same as one of the given strings.
     *
     * <p> The returned array must not be empty, and all its elements must represent
     * either valid {@link Identifier}s, or the {@link #WILDCARD} string.
     * A missing {@code Item} at runtime is not an error, and will simply result in the factory never being called.
     *
     * <p>The magic string {@link #WILDCARD} may be returned instead of
     * a valid item id. A factory registered for the wildcard is called for every item stack ever
     * created. For performance reasons, wildcard callbacks should be avoided
     * where possible.
     *
     * @return one or more item ids, or the {@link #WILDCARD}.
     * @see ItemComponentCallback
     */
    String[] targets() default { WILDCARD };
}
