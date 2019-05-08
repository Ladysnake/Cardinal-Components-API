package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.ItemComponentProvider;
import nerdhub.cardinal.components.api.component.ItemComponent;
import nerdhub.cardinal.components.mixins.accessor.ItemstackComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public class MixinItem implements ItemComponentProvider {

    @Override
    public void initComponents(ItemStack stack) {
        //NO-OP
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public <T extends ItemComponent> void addComponent(ItemStack stack, ComponentType<T> type, T component) {
        ((ItemstackComponents) (Object) stack).addComponent(type, component);
    }
}
