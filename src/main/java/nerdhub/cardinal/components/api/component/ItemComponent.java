package nerdhub.cardinal.components.api.component;

import net.minecraft.nbt.CompoundTag;

/**
 * Convenience interface for item-only components.<br/>
 * Item components are <strong>NOT</strong> required to explicitly implement this interface!<br/>
 * fall back to {@link Component} for handling item components!
 */
public interface ItemComponent extends Component {

    @Override
    void fromItemTag(CompoundTag tag);

    @Override
    CompoundTag toItemTag(CompoundTag tag);

    @Override
    Component newInstanceForItemStack();

    @Override
    boolean isComponentEqual(Component other);
}
