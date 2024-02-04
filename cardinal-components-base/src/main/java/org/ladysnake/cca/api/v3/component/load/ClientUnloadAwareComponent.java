/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2023 Ladysnake
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
package org.ladysnake.cca.api.v3.component.load;

import com.demonwav.mcdev.annotations.CheckEnv;
import com.demonwav.mcdev.annotations.Env;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.internal.base.asm.CalledByAsm;
import net.minecraft.util.Identifier;

/**
 * A component that gets notified whenever the provider it is attached to gets unloaded.
 *
 * <p>This interface must be visible at factory registration time - which means the class implementing it
 * must either be the parameter to {@link ComponentRegistryV3#getOrCreate(Identifier, Class)} or declared explicitly
 * using a dedicated method on the factory registry.
 *
 * <p>Not every provider supports loading events. Check individual module documentation for more information.
 * @see ClientLoadAwareComponent
 */
public interface ClientUnloadAwareComponent extends Component {
    /**
     * Called after the provider of this component has been unloaded.
     *
     * <p>The semantics of "unloading" differ based on the provider.
     * In <em>most</em> cases, this method will only be called once in an object's lifecycle,
     * and it <em>should</em> be called as many times as the corresponding loading event if applicable.
     */
    @CheckEnv(Env.CLIENT)
    @CalledByAsm
    void unloadClientside();
}
