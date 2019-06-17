package nerdhub.cardinal.components.mixin;

import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.entity.EntityTickCallback;
import nerdhub.cardinal.components.impl.event.EntityComponentEventCaller;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.ArrayList;
import javax.annotation.Nullable;

@Mixin(EntityType.class)
public class MixinEntityType<E extends Entity> {
    @Nullable
	private EntityComponentCallback<E>[] cardinal_componentEvents;


    /**
     * Fires {@link EntityComponentCallback} for every superclass of the given entity.
     * <strong>{@code e} MUST have this entity type.</strong>
     * This method has undefined behaviour if several entity classes share the same entity type.
     */
    public void cardinal_fireComponentEvents(E e, ComponentContainer cc) {
        assert e.getType() == (Object) this;
        if (cardinal_componentEvents == null) {
            List<EntityComponentCallback<E>> events = new ArrayList<>();
            Class<?> c = e.getClass();
            while (Entity.class.isAssignableFrom(c)) {
                events.add(EntityComponentCallback.event(c));
                c = c.getSuperclass();
            }
            cardinal_componentEvents = events.toArray(new EntityComponentCallback[0]);
        }
        for (EntityComponentCallback event : cardinal_componentEvents) {
            event.getInvoker().attachComponents(e, cc);
        }
    }
}
