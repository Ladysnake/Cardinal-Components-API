/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2025 OnyxStudios
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
package dev.onyxstudios.cca.test.block;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class GlobalTickingComponent implements Component, ServerTickingComponent {

    public static final ComponentKey<GlobalTickingComponent> KEY = ComponentRegistry.getOrCreate(new Identifier(CcaBlockTestMod.MOD_ID, "global_ticking_test"), GlobalTickingComponent.class);

    public GlobalTickingComponent(Object provider) {
        // NO-OP
    }

    @Nullable
    private BooleanSupplier onTick;

    void setTickAction(@Nullable BooleanSupplier onTick) {
        this.onTick = onTick;
    }

    @Override public void serverTick() {
        if(this.onTick != null) {
            if(!this.onTick.getAsBoolean()) {
                this.onTick = null;
            }
        }
    }

    public Optional<BooleanSupplier> getTickAction() {
        return Optional.ofNullable(this.onTick);
    }

    @Override public void readFromNbt(NbtCompound tag) {
        // NO-OP
    }

    @Override public void writeToNbt(NbtCompound tag) {
        // NO-OP
    }
}
