/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2024 Ladysnake
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
package org.ladysnake.cca.api.v3.item;

import net.minecraft.component.DataComponentType;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;

/**
 * Allows registering migrations from {@linkplain org.ladysnake.cca.api.v3.component.Component CCA components} to {@linkplain net.minecraft.component.Component vanilla components}.
 */
public interface ItemComponentMigrationRegistry {
    /**
     * Registers an item component migration from the specified {@link ComponentKey#getId() CCA Component ID} to an equivalent {@link DataComponentType}.
     *
     * <p>This hooks into the vanilla datafixing process and may therefore not correctly migrate data for stacks stored in modded containers.
     *
     * @param oldComponentId the item component ID from CCA days
     * @param mcComponentType the new vanilla component type
     */
    void registerMigration(Identifier oldComponentId, DataComponentType<?> mcComponentType);
}
