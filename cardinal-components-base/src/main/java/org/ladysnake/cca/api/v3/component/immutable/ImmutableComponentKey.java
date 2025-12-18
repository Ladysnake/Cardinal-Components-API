package org.ladysnake.cca.api.v3.component.immutable;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;

import java.util.function.UnaryOperator;

public abstract class ImmutableComponentKey<C extends ImmutableComponent> extends ComponentKey<ImmutableComponentWrapper<C, ?>> {
    private final @Nullable MapCodec<C> mapCodec;
    private final @Nullable PacketCodec<RegistryByteBuf, C> packetCodec;
    private final Class<C> immutableComponentClass;

    protected ImmutableComponentKey(Identifier id, Class<C> immutableComponentClass, Class<ImmutableComponentWrapper<C, ?>> wrapperClass, @Nullable MapCodec<C> mapCodec, @Nullable PacketCodec<RegistryByteBuf, C> packetCodec) {
        super(id, wrapperClass);
        this.immutableComponentClass = immutableComponentClass;
        this.mapCodec = mapCodec;
        this.packetCodec = packetCodec;
    }

    public Class<C> getImmutableComponentClass() {
        return this.immutableComponentClass;
    }

    public MapCodec<C> getMapCodec() {
        return this.mapCodec;
    }

    public PacketCodec<RegistryByteBuf, C> getPacketCodec() {
        return this.packetCodec;
    }

    public C getValue(Object owner) {
        return this.get(owner).getData();
    }

    public void set(Object owner, C data) {
        var wrapper = this.get(owner);
        wrapper.setData(data);
    }

    public void setAndSync(Object owner, C data) {
        var wrapper = this.get(owner);
        wrapper.setAndSync(data);
    }

    public void update(Object owner, UnaryOperator<C> operator) {
        var wrapper = this.get(owner);
        wrapper.update(operator);
    }

    public void updateAndSync(Object owner, UnaryOperator<C> operator) {
        var wrapper = this.get(owner);
        wrapper.updateAndSync(operator);
    }

    public <O> void update(O owner, ImmutableComponent.Modifier<C, O> modifier) {
        var wrapper = this.getWrapper(owner);
        wrapper.update(modifier);
    }

    public <O> void updateAndSync(O owner, ImmutableComponent.Modifier<C, O> modifier) {
        var wrapper = this.getWrapper(owner);
        wrapper.updateAndSync(modifier);
    }

    @SuppressWarnings("unchecked")
    private <O> @NotNull ImmutableComponentWrapper<C, O> getWrapper(O owner) {
        return (ImmutableComponentWrapper<C, O>) this.get(owner);
    }
}
