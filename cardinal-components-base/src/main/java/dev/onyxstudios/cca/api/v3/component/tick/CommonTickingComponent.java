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
package dev.onyxstudios.cca.api.v3.component.tick;

import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import net.minecraft.util.Identifier;

/**
 * A component that gets ticked alongside the provider it is attached to, both serverside and clientside.
 *
 * <p>This interface must be visible at factory registration time - which means the class implementing it
 * must either be the parameter to {@link ComponentRegistryV3#getOrCreate(Identifier, Class)} or declared explicitly
 * using a dedicated method on the factory registry.
 *
 * <p>If a provider only supports serverside ticking, this interface behaves the same as {@link ServerTickingComponent}.
 *
 * @see ClientTickingComponent
 * @see ServerTickingComponent
 */
public interface CommonTickingComponent extends ServerTickingComponent, ClientTickingComponent {
    @Override
    default void clientTick() {
        this.tick();
    }

    @Override
    default void serverTick() {
        this.tick();
    }

    void tick();
}
