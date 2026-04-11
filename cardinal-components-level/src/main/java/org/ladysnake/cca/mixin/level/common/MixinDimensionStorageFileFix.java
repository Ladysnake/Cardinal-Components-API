/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2026 Ladysnake
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

package org.ladysnake.cca.mixin.level.common;

import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.filefix.FileFix;
import net.minecraft.util.filefix.access.FileRelation;
import net.minecraft.util.filefix.fixes.DimensionStorageFileFix;
import net.minecraft.util.filefix.operations.FileFixOperations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(DimensionStorageFileFix.class)
public abstract class MixinDimensionStorageFileFix extends FileFix {
    public MixinDimensionStorageFileFix(Schema schema) {
        super(schema);
    }

    @Inject(method = "makeFixer", at = @At("HEAD"))
    private void customFixers(CallbackInfo ci) {
        // Handle custom dimension data
        this.addFileFixOperation(FileFixOperations.applyInFolders(FileRelation.DIMENSIONS_DATA,
            List.of(FileFixOperations.move("cardinal_world_components.dat", "cardinal-components/world.dat"))
        ));

        // Handle vanilla dimensions data (they used different format, so require separate definition)
        this.addFileFixOperation(FileFixOperations.groupMove(Map.of(
            "data", "dimensions/minecraft/overworld/data/cardinal-components",
                "DIM-1/data", "dimensions/minecraft/the_nether/data/cardinal-components",
                "DIM1/data", "dimensions/minecraft/the_end/data/cardinal-components"
            ),
            List.of(FileFixOperations.move("cardinal_world_components.dat", "world.dat"))));
    }
}
