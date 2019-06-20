package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.component.SidedContainerCompound;
import nerdhub.cardinal.components.api.event.BlockEntityComponentCallback;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.internal.BlockEntityTypeCaller;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityType.class)
public abstract class MixinBlockEntityType<B extends BlockEntity> implements BlockEntityTypeCaller<B> {
    @SuppressWarnings("unchecked")
    private final Event<BlockEntityComponentCallback<B>> cardinal_componentEvent =
            (Event<BlockEntityComponentCallback<B>>) (Event) EventFactory.createArrayBacked(BlockEntityComponentCallback.class, callbacks -> (be, components) -> {
                for (BlockEntityComponentCallback callback : callbacks) {
                    callback.attachComponents(be, components);
                }
            });

    @Override
    public Event<BlockEntityComponentCallback<B>> getBlockEntityComponentEvent() {
        return cardinal_componentEvent;
    }

    /**
     * Fires {@link EntityComponentCallback} for every superclass of the given entity.
     * <strong>{@code b} MUST have this entity type.</strong>
     * This method has undefined behaviour if several entity classes share the same entity type.
     */
    void cardinal_fireComponentEvents(B b, SidedContainerCompound cc) {
        // assert b.getType() == this;
        cardinal_componentEvent.invoker().attachComponents(b, cc);
    }
}
