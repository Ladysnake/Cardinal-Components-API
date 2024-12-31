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
