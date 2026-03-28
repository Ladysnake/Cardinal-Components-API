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
