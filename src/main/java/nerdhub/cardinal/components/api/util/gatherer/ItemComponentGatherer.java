package nerdhub.cardinal.api.util.component;

/**
 * A {@link ComponentGatherer} specialized for item stacks. 
 *
 * <p> If an {@link Item} implements this interface, its {@link ItemComponentGatherer#initComponents}
 * method will be automatically called for every item stack created that holds this item.
 */
public interface ItemComponentGatherer extends ComponentGatherer<ItemStack> {
    @Override
    void initComponents(ItemStack stack, ComponentContainer cc);
}
