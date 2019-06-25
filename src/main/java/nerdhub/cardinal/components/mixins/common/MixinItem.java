package nerdhub.cardinal.components.mixins.common;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class MixinItem {
    // TODO attach a ComponentGatherer to this class on server load, using the event

}
