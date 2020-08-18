/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2020 OnyxStudios
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
package nerdhub.cardinal.components.api.component.extension;

import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

/**
 * A component that is aware of its {@code ComponentType}.
 */
public interface TypeAwareComponent extends Component {
    /**
     * Return the component type this component instance is associated with.
     *
     * <p> The returned type may differ between instances of the same class,
     * but remains constant between calls on the same instance.
     * The general contract is that, for a given provider {@code p} that
     * maps a component type {@code t} to a component {@code c} implementing
     * {@code TypeAwareComponent}, the expression
     * {@code ((TypeAwareComponent)t.get(p)).getComponentType() == t}
     * always evaluates to {@code true}.
     *
     * <p>For example:
     * <pre>{@code
     *      ComponentType<MyComponent> MY_COMPONENT = ComponentRegistry.register(...);
     *
     *      class MyTypeAwareComponent implements MyComponent, TypeAwareComponent {
     *          public ComponentType getComponentType() {
     *              return MY_COMPONENT;
     *          }
     *      }
     * }</pre>
     */
    ComponentType<?> getComponentType();

    @SuppressWarnings("unchecked")
    @ApiStatus.Experimental
    static <C extends Component> ComponentType<? super C> lookupComponentType(ComponentProvider holder, C component) {
        ComponentKey<? super C> selfType = null;
        ComponentContainer container = holder.getComponentContainer();
        Set<? extends ComponentKey<?>> keys = container == null
            ? ((nerdhub.cardinal.components.api.component.ComponentProvider) holder).getComponentTypes()
            : container.keys();
        for (ComponentKey<?> componentType : keys) {
            if (componentType.getNullable(holder) == component) {
                if (selfType == null) {
                    // unchecked cast but safe because of ComponentType#getNullable's contract
                    selfType = (ComponentKey<? super C>) componentType;
                } else {
                    throw new IllegalStateException("Component " + component + " is attached to the same provider under 2 or more types");
                }
            }
        }
        if (selfType == null) {
            throw new IllegalStateException("getComponentProvider() returned invalid value");
        }
        return (ComponentType<? super C>) selfType;
    }
}
