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
package org.ladysnake.cca.api.v3.component;

import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;

/**
 * Entrypoint getting invoked to declare static component types.
 *
 * <p><b>Implementations of this class must not call {@link ComponentRegistry#getOrCreate(Identifier, Class)}</b>,
 * directly or indirectly, until {@link #finalizeStaticBootstrap()} is called. They must also avoid causing the
 * instantiation of a component provider (eg. ItemStack). It is recommended to implement this interface
 * on its own class to avoid running static initializers too early, e.g. because they were referenced in field or method
 * signatures in the same class.
 *
 * <p>The entrypoint is exposed as {@code "cardinal-components:static-init"} in the mod json
 * and runs for any environment. It usually executes right before the first {@link ComponentKey}
 * is created, but can be triggered at any time by another module.
 */
public interface StaticComponentInitializer {
    /**
     * @return the identifiers of the {@link ComponentKey}s this initializer supports
     */
    default Collection<Identifier> getSupportedComponentKeys() {
        return Collections.emptySet();
    }

    /**
     * Called when static component bootstrap is finished.
     *
     * <p>It is safe to register {@link ComponentKey}s in this method.
     */
    default void finalizeStaticBootstrap() {
        // NO-OP
    }
}
