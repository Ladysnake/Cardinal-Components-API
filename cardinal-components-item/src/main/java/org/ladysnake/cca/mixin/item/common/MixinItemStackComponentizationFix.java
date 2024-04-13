package org.ladysnake.cca.mixin.item.common;

import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import org.ladysnake.cca.internal.item.StaticItemComponentPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackComponentizationFix.class)
public abstract class MixinItemStackComponentizationFix {
    @Inject(method = "fixStack", at = @At("RETURN"))
    private static void fixStack(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic, CallbackInfo ci) {
        StaticItemComponentPlugin.INSTANCE.migrate(data);
    }
}
