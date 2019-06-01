package nerdhub.cardinal.components.mixins.common;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.ItemComponentProvider;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.util.accessor.ItemstackComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class MixinItem implements ItemComponentProvider {

    @Override
    public void createComponents(ItemStack stack) {
        //NO-OP
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public <T extends Component> void addComponent(ItemStack stack, ComponentType<T> type, T component) {
        ((ItemstackComponents) (Object) stack).setComponentValue(type, component);
    }
}
