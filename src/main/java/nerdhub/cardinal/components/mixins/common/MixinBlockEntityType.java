package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.component.SidedComponentContainer;
import nerdhub.cardinal.components.api.event.BlockEntityComponentCallback;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(EntityType.class)
public abstract class MixinBlockEntityType<B extends BlockEntity> {
    @Nullable
	private Event<BlockEntityComponentCallback<? super B>>[] cardinal_componentEvents;

    /**
     * Fires {@link EntityComponentCallback} for every superclass of the given entity.
     * <strong>{@code b} MUST have this entity type.</strong>
     * This method has undefined behaviour if several entity classes share the same entity type.
     */
    @SuppressWarnings("unchecked")
    void cardinal_fireComponentEvents(B b, SidedComponentContainer cc) {
        // assert b.getType() == this;
        if (cardinal_componentEvents == null) {
            List<Event<BlockEntityComponentCallback<? super B>>> events = new ArrayList<>();
            Class c = b.getClass();
            while (Entity.class.isAssignableFrom(c)) {
                events.add(BlockEntityComponentCallback.event(c));
                c = c.getSuperclass();
            }
            cardinal_componentEvents = events.toArray(new Event[0]);
        }
        for (Event<BlockEntityComponentCallback<? super B>> event : cardinal_componentEvents) {
            event.invoker().attachComponents(b, cc);
        }
    }
}
