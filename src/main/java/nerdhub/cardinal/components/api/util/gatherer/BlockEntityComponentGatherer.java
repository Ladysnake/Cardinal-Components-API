package nerdhub.cardinal.components.api.util.component;

/**
 * Convenience interface for attaching components to sides of custom instances.
 * 
 * <p> If a {@link BlockEntity} implements this interface, its 
 * {@link #initComponents(SidedComponentContainer)} method called automatically when
 * {@link BlockEntityComponentCallback#EVENT} is fired.
 */
public interface BlockEntityComponentGatherer {
    void initComponents(SidedComponentContainer cc);
}
