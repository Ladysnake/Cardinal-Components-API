package nerdhub.cardinal.components.api.component;

/**
 * Entrypoint getting invoked to register <em>static</em> generic (typically for third party providers)
 * component factories.
 *
 * <p>The entrypoint, like every {@link StaticComponentInitializer}, is exposed as
 * {@code cardinal-components:static-init} in the mod json and runs for any environment.
 *
 * @since 2.4.0
 */
public interface StaticGenericComponentInitializer extends StaticComponentInitializer {
    /**
     * Called to register component factories for statically declared component types.
     *
     * <p><strong>The passed registry must not be held onto!</strong> Static component factories
     * must not be registered outside of this method.
     *
     * @param registry a {@link GenericComponentFactoryRegistry} for <em>statically declared</em> components
     */
    void registerGenericComponentFactories(GenericComponentFactoryRegistry registry);
}
