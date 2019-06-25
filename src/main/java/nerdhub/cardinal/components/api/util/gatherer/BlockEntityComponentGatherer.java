package nerdhub.cardinal.components.api.util.gatherer;

import nerdhub.cardinal.components.api.component.container.SidedContainerCompound;
import nerdhub.cardinal.components.api.event.BlockEntityComponentCallback;
import net.minecraft.block.entity.BlockEntity;

/**
 * Convenience interface for attaching components to sides of custom instances.
 * 
 * <p> If a {@link BlockEntity} implements this interface, its {@code initComponents()}
 * method is called automatically when {@link BlockEntityComponentCallback#EVENT} is fired.
 */
public interface BlockEntityComponentGatherer {
    void initComponents(SidedContainerCompound cc);
}
