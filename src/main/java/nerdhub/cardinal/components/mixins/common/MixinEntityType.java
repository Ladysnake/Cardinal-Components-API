package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.impl.IndexedComponentContainer;
import nerdhub.cardinal.components.internal.FeedbackContainerFactory;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(EntityType.class)
public abstract class MixinEntityType<E extends Entity> {
    @Nullable
	private Event<EntityComponentCallback<? super E>>[] cardinal_componentEvents;
    @Unique
    private FeedbackContainerFactory componentContainerFactory = new FeedbackContainerFactory();

    /**
     * Fires {@link EntityComponentCallback} for every superclass of the given entity.
     * <strong>{@code e} MUST have this entity type.</strong>
     * This method has undefined behaviour if several entity classes share the same entity type.
     */
    @SuppressWarnings("unchecked")
    ComponentContainer cardinal_fireComponentEvents(E e) {
        // assert e.getType() == this;
        IndexedComponentContainer cc = this.componentContainerFactory.create();
        if (cardinal_componentEvents == null) {
            List<Event<EntityComponentCallback<? super E>>> events = new ArrayList<>();
            Class c = e.getClass();
            while (Entity.class.isAssignableFrom(c)) {
                events.add(EntityComponentCallback.event(c));
                c = c.getSuperclass();
            }
            cardinal_componentEvents = events.toArray(new Event[0]);
        }
        for (Event<EntityComponentCallback<? super E>> event : cardinal_componentEvents) {
            event.invoker().attachComponents(e, cc);
        }
        this.componentContainerFactory.adjustFrom(cc);
        return cc;
    }
}
