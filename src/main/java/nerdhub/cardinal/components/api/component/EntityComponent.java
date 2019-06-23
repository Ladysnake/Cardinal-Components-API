package nerdhub.cardinal.components.api.component;

import net.minecraft.nbt.CompoundTag;

/**
 * Convenience interface for entity-only components.<br/>
 * Entity components are <strong>NOT</strong> required to explicitly implement this interface!<br/>
 * fall back to {@link Component} for handling entity components!
 */
public interface EntityComponent extends Component {

    @Override
    void fromTag(CompoundTag tag);

    @Override
    CompoundTag toTag(CompoundTag tag);

    @Override
    boolean isComponentEqual(Component other);
}
