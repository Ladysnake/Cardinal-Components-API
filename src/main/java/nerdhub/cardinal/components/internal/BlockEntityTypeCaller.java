package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.event.BlockEntityComponentCallback;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.block.entity.BlockEntity;

public interface BlockEntityTypeCaller<B extends BlockEntity> {
	Event<BlockEntityComponentCallback<B>> getBlockEntityComponentEvent();
}