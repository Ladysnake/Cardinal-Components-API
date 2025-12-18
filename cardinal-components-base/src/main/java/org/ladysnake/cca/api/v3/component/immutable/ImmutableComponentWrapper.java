package org.ladysnake.cca.api.v3.component.immutable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.CopyableComponent;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

@ApiStatus.NonExtendable
public abstract class ImmutableComponentWrapper<C extends ImmutableComponent, O> implements
    Component,
    CopyableComponent<ImmutableComponentWrapper<C, O>> {
    private final ImmutableComponentKey<C> key;
    private final O owner;
    private @NotNull C data;

    protected ImmutableComponentWrapper(ImmutableComponentKey<C> key, O owner, C data) {
        this.key = key;
        this.owner = owner;
        this.data = data;
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        // overridden if key.mapCodec != null
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        // overridden if key.mapCodec != null
    }

    @Override
    public void copyFrom(ImmutableComponentWrapper<C, O> other, RegistryWrapper.WrapperLookup registryLookup) {
        this.data = other.data;
    }

    public ImmutableComponentKey<C> getKey() {
        return key;
    }

    public O getOwner() {
        return owner;
    }

    public C getData() {
        return this.data;
    }

    public void setData(C data) {
        this.data = data;
    }

    public void setAndSync(C data) {
        boolean shouldSync = !this.data.equals(data);
        this.setData(this.data);
        if (shouldSync) {
            this.key.sync(this.owner);
        }
    }

    public void update(UnaryOperator<C> operator) {
        this.setData(operator.apply(this.data));
    }

    public void updateAndSync(UnaryOperator<C> operator) {
        this.setAndSync(operator.apply(this.data));
    }

    public void update(ImmutableComponent.Modifier<C, O> modifier) {
        this.setData(modifier.modify(this.data, this.owner));
    }

    public void updateAndSync(ImmutableComponent.Modifier<C, O> modifier) {
        this.setAndSync(modifier.modify(this.data, this.owner));
    }
}
