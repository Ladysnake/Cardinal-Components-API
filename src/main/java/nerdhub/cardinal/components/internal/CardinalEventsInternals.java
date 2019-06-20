package nerdhub.cardinal.components.internal;

public final class CardinalEventsInternals {
    private CardinalEventsInternals() { throw new AssertionError(); }

    private static final Map<Class<? extends Entity>, Event> EVENTS = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Entity> Event<EntityComponentCallback<T>> event(Class<T> clazz) {
        return (Event<EntityComponentCallback<T>>) EVENTS.computeIfAbsent(clazz, c ->
                EventFactory.createArrayBacked(EntityComponentCallback.class, callbacks -> (entity, components) -> {
                for(EntityComponentCallback callback : callbacks) {
                    callback.attachComponents(entity, components);
                }
            })
        );
    }
    
}