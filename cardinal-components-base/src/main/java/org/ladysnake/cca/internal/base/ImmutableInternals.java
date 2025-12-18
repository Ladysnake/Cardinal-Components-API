package org.ladysnake.cca.internal.base;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.immutable.ImmutableComponent;
import org.ladysnake.cca.api.v3.component.immutable.ImmutableComponentHookType;
import org.ladysnake.cca.api.v3.component.immutable.ImmutableComponentKey;
import org.ladysnake.cca.api.v3.component.immutable.ImmutableComponentWrapper;

import java.lang.invoke.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ImmutableInternals {
    private static final MethodHandle RUN_TRANSFORMER;

    static {
        try {
            RUN_TRANSFORMER = MethodHandles.lookup().findStatic(ImmutableInternals.class, "runTransformer", MethodType.methodType(void.class, ImmutableComponent.Modifier.class, ImmutableComponentWrapper.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to find one or more method handles", e);
        }
    }

    //TODO id and type do not uniquely describe a component implementation - e.g. predicates in entity component registration
    public static final Map<ImmutableComponentHookType<?>, Map<Pair<Identifier, Type>, ImmutableComponent.Modifier<?, ?>>> HOOKS = new HashMap<>();
    public static final Set<ImmutableComponentHookType<?>> HOOK_TYPES = Set.of(
        ImmutableComponentHookType.SERVER_TICK,
        ImmutableComponentHookType.CLIENT_TICK,
        ImmutableComponentHookType.SERVER_LOAD,
        ImmutableComponentHookType.CLIENT_LOAD,
        ImmutableComponentHookType.SERVER_UNLOAD,
        ImmutableComponentHookType.CLIENT_UNLOAD
    );

    public static <C extends ImmutableComponent, E extends Entity> void addHook(ImmutableComponentKey<C> key, Class<E> target, ImmutableComponentHookType<?> type, ImmutableComponent.Modifier<C, E> modifier) {
        HOOKS.computeIfAbsent(type, $ -> new HashMap<>()).put(Pair.of(key.getId(), target), modifier);
    }

    public static Object bootstrap(MethodHandles.Lookup lookup,
                                   String methodName,
                                   MethodType methodType,
                                   String id,
                                   Type targetClass) throws Throwable {
        ImmutableComponentHookType<?> callbackType = HOOK_TYPES.stream()
            .filter(t -> t.methodName().equals(methodName))
            .filter(t -> t.implType().equals(methodType))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid method name/type: %s:%s".formatted(methodName, methodType.descriptorString())));
        MethodHandle handle = makeModifierHandler(lookup, id, targetClass, HOOKS.getOrDefault(callbackType, Map.of()));
        return new ConstantCallSite(handle);
    }

    private static MethodHandle makeModifierHandler(MethodHandles.Lookup lookup, String id, Type targetClass, Map<Pair<Identifier, Type>, ImmutableComponent.Modifier<?, ?>> handlers) throws NoSuchMethodException, IllegalAccessException {
        var modifier = handlers.get(Pair.of(Identifier.of(id), targetClass));
        if (modifier == null) {
            return MethodHandles.empty(MethodType.methodType(void.class, ImmutableComponentWrapper.class));
        }

        return RUN_TRANSFORMER.bindTo(modifier);
    }

    public static <C extends ImmutableComponent, O> void runTransformer(ImmutableComponent.Modifier<C, O> transformer, ImmutableComponentWrapper<C, O> wrapper) {
        wrapper.setData(transformer.modify(wrapper.getData(), wrapper.getOwner()));
    }

    public static <C extends ImmutableComponent> void wrapperRead(ImmutableComponentWrapper<C, ?> wrapper, NbtCompound compound, RegistryWrapper.WrapperLookup registries) {
        var ops = RegistryOps.of(NbtOps.INSTANCE, registries);
        var decoded = wrapper.getKey().getMapCodec().decode(ops, ops.getMap(compound).getOrThrow())
            .getOrThrow(e -> new RuntimeException("Error decoding component %s:\n%s".formatted(wrapper.getKey().getId(), e)));
        wrapper.setData(decoded);
    }

    public static <C extends ImmutableComponent> void wrapperWrite(ImmutableComponentWrapper<C, ?> wrapper, NbtCompound compound, RegistryWrapper.WrapperLookup registries) {
        var ops = RegistryOps.of(NbtOps.INSTANCE, registries);
        wrapper.getKey().getMapCodec().encode(wrapper.getData(), ops, ops.mapBuilder())
            .build(compound)
            .getOrThrow(e -> new RuntimeException("Error encoding component %s:\n%s".formatted(wrapper.getKey().getId(), e)));
    }

    public static <C extends ImmutableComponent> void wrapperApplySync(ImmutableComponentWrapper<C, ?> wrapper, RegistryByteBuf buf) {
        var decoded = wrapper.getKey().getPacketCodec().decode(buf);
        wrapper.setData(decoded);
    }

    public static <C extends ImmutableComponent> void wrapperWriteSync(ImmutableComponentWrapper<C, ?> wrapper, RegistryByteBuf buf) {
        wrapper.getKey().getPacketCodec().encode(buf, wrapper.getData());
    }
}
