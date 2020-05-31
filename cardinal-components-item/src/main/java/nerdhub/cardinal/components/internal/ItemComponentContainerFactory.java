package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface ItemComponentContainerFactory {
    ComponentContainer<CopyableComponent<?>> create(Item item, ItemStack stack);
}
