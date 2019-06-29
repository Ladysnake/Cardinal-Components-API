/*
 * Cardinal-Components-API
 * Copyright (C) 2019 GlassPane
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package nerdhub.cardinal.components.api.util;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.util.Set;

public final class Components {
    private Components() { throw new AssertionError(); }

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
                if(!other.hasComponent(type) || !type.get(accessor).isComponentEqual(type.get(other))) {
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
