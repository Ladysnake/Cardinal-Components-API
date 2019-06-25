package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.internal.BlockEntityTypeCaller;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityType.class)
public abstract class MixinBlockEntityType<B extends BlockEntity> implements BlockEntityTypeCaller<B> {

}
