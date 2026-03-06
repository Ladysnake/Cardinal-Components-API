package org.ladysnake.cca.mixin.scoreboard;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.scoreboard.ScoreboardState;
import org.ladysnake.cca.internal.scoreboard.CcaPackedState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(ScoreboardState.class)
public class MixinScoreboardState {
    @Shadow
    private ScoreboardState.Packed packedState;

    @ModifyExpressionValue(method = "set", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ScoreboardState$Packed;equals(Ljava/lang/Object;)Z"))
    private boolean compareComponentValues(boolean original, ScoreboardState.Packed packed){
        return original && Objects.equals(((CcaPackedState) (Object) packed).cca$getSerializedComponents(), ((CcaPackedState) (Object) packedState).cca$getSerializedComponents());
    }
}
