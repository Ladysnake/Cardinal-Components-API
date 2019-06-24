package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.provider.ComponentProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.util.Set;

public final class Components {
    private Components() { throw new AssertionError(); }

    /**
     * Helper method to inject components into block entities based on the block they are linked to.
     *
     * <p> This approach has some restrictions: <ul>
     * <li> Blocks added to the tag that do not have a {@link BlockEntity} will be ignored. </li>
     * <li> Changes to the tag require a server restart to be effective. This restriction is notably
     * to avoid lingering components on existing {@code BlockEntity} instances. </li>
     * </ul>
     *
     * <p> Example:
     * <pre><code>
     *    // Injects instances of MyStorageComponent into the down side of blocks marked as chests
     *    Components.addAttachedComponent(MyComponentTypes.STORAGE, MyStorageComponent::new, MoreChestBlockTags.CHEST, Direction.DOWN);
     * </code></pre>
     *
     * @see SidedContainerCompound
     * @see #addAttachedComponent(ComponentType, Function, BlockEntityType, Direction, Direction[])
     */
    @Beta
    public static <T> void addAttachedComponent(ComponentType<T> type, Function<BlockEntity, T> factory, Tag<Block> blocks, @Nullable Direction first, Direction... moreSides) {
        ComponentGatherer gatherer = (be, cc) -> {
            cc.get(first).put(type, factory.apply(be));
            for (Direction d : moreSides) {
                cc.get(d).put(type, factory.apply(be));
            }
        }
        BlockEntityComponentCallback.EVENT.register(b -> blocks.contains(b) ? gatherer : null);
    }

    /**
     * Helper method to inject components into block entities.
     *
     * <p> Example:
     * <pre><code>
     *    // Injects instances of MyStorageComponent into the core and top sides of block entities using the chest {@code BlockEntityType}
     *    Components.addAttachedComponent(MyComponentTypes.STORAGE, MyStorageComponent::new, BlockComponentTypes.CHEST, null, Direction.UP);
     * </code></pre>
     *
     * @see SidedContainerCompound
     */
    public static <T, B> void addAttachedComponent(ComponentType<T> type, Function<B, T> factory, BlockEntityType<B> beType, @Nullable Direction side, Direction... moreSides) {
        SidedComponentGatherer gatherer = (be, cc) -> {
            if (be.getType() == beType) {
                cc.put(factory.apply((T) be));
                for (Direction d : moreSides) {
                    cc.get(d).put(type, factory.apply(be));
                }
            }
        }
        BlockEntityComponentCallback.EVENT.register(b -> beType.supportsBlock(b) ? gatherer : null);
    }

    /**
     * Checks item stack equality based on their exposed components.
     *
     * <p> Two {@link ItemStack#isEmpty empty} item stacks will be considered
     * equal, as they would expose no component.
     */
    public static boolean areComponentsEqual(ItemStack stack1, ItemStack stack2) {
        return (stack1.isEmpty() && stack2.isEmpty()) || areComponentsEqual(ComponentProvider.fromItemStack(stack1), ComponentProvider.fromItemStack(stack2));
    }

    /**
     * Compares a provider with another for equality based on the components they expose.
     * Returns {@code true} if the two providers expose the same component types through
     * {@link ComponentProvider#getComponentTypes}, and, for each of the types exposed as such,
     * the corresponding component values are equal according to {@link Component#isComponentEqual}.
     */
    public static boolean areComponentsEqual(ComponentProvider accessor, ComponentProvider other) {
        Set<ComponentType<? extends Component>> types = accessor.getComponentTypes();
        if(types.size() == other.getComponentTypes().size()) {
            for(ComponentType<? extends Component> type : types) {
                if(!other.hasComponent(type) || !accessor.getComponent(type).isComponentEqual(other.getComponent(type))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static Component copyOf(Component toCopy) {
        Component ret = toCopy.newInstance();
        ret.fromTag(toCopy.toTag(new CompoundTag()));
        return ret;
    }

}
