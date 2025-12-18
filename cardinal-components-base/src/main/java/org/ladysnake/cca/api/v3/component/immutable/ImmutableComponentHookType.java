package org.ladysnake.cca.api.v3.component.immutable;

import org.ladysnake.cca.api.v3.component.load.ClientLoadAwareComponent;
import org.ladysnake.cca.api.v3.component.load.ClientUnloadAwareComponent;
import org.ladysnake.cca.api.v3.component.load.ServerLoadAwareComponent;
import org.ladysnake.cca.api.v3.component.load.ServerUnloadAwareComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public record ImmutableComponentHookType<I>(Class<I> itf,
                                            String methodName,
                                            MethodType exposedType,
                                            MethodType implType) {
    public ImmutableComponentHookType {
        if (!itf.isInterface()
            || Arrays.stream(itf.getDeclaredMethods())
            .filter(m -> Modifier.isAbstract(m.getModifiers()))
            .count() != 1) {
            throw new IllegalArgumentException("ImmutableComponentHookType accepts only functional interfaces");
        }
    }

    public static <I> ImmutableComponentHookType<I> fromFunctionalInterface(Class<I> itf) {
        var method = Arrays.stream(itf.getDeclaredMethods())
            .filter(m -> Modifier.isAbstract(m.getModifiers()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("ImmutableComponentHookType accepts only functional interfaces"));
        String name = method.getName();
        var type = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        var implType = type.insertParameterTypes(0, ImmutableComponentWrapper.class);
        return new ImmutableComponentHookType<>(itf, name, type, implType);
    }

    public static final ImmutableComponentHookType<ServerTickingComponent> SERVER_TICK = fromFunctionalInterface(ServerTickingComponent.class);
    public static final ImmutableComponentHookType<ClientTickingComponent> CLIENT_TICK = fromFunctionalInterface(ClientTickingComponent.class);
    public static final ImmutableComponentHookType<ServerLoadAwareComponent> SERVER_LOAD = fromFunctionalInterface(ServerLoadAwareComponent.class);
    public static final ImmutableComponentHookType<ClientLoadAwareComponent> CLIENT_LOAD = fromFunctionalInterface(ClientLoadAwareComponent.class);
    public static final ImmutableComponentHookType<ServerUnloadAwareComponent> SERVER_UNLOAD = fromFunctionalInterface(ServerUnloadAwareComponent.class);
    public static final ImmutableComponentHookType<ClientUnloadAwareComponent> CLIENT_UNLOAD = fromFunctionalInterface(ClientUnloadAwareComponent.class);
}
