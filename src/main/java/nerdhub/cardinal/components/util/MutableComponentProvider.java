package nerdhub.cardinal.components.util;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.provider.ItemComponentProvider;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.Set;

/**
 * used to access an object's components.
 */
public class MutableComponentProvider extends SimpleComponentProvider {
    public MutableComponentProvider(ComponentContainer backing) {
        super(backing);
    }

    public void setBackingContainer(ComponentContainer backing) {
        this.backing = backing;
    }

    public ComponentContainer getBackingContainer(ComponentContainer backing) {
        return this.backing;
    }
}


