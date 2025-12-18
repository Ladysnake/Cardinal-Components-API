package org.ladysnake.cca.test.base;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.immutable.ImmutableComponent;
import org.ladysnake.cca.api.v3.component.immutable.ImmutableComponentKey;

public record Energy(int amount) implements ImmutableComponent {
    public static final PacketCodec<RegistryByteBuf, Energy> STREAM_CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT,
        Energy::amount,
        Energy::new);

    public static final MapCodec<Energy> MAP_CODEC = Codec.INT.fieldOf("energy_amount")
        .xmap(Energy::new, Energy::amount);

    public static final ImmutableComponentKey<Energy> KEY = ComponentRegistry.getOrCreate(
        Identifier.of("cca-base-test", "energy"),
        Energy.class,
        MAP_CODEC,
        STREAM_CODEC);
}
