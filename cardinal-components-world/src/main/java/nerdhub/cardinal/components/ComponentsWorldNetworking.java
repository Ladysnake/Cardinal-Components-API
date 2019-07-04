package nerdhub.cardinal.components;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.ComponentProvider;
import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import nerdhub.cardinal.components.api.event.WorldSyncCallback;
import nerdhub.cardinal.components.api.util.Components;
import nerdhub.cardinal.components.api.util.sync.WorldSyncedComponent;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public final class ComponentsWorldNetworking {
    public static void init() {
        if (!FabricLoader.getInstance().isModLoaded("fabric-networking")) {
            return;
        }
        WorldSyncCallback.EVENT.register(ComponentsWorldNetworking::syncWorldComponents);
        ClientSidePacketRegistry.INSTANCE.register(WorldSyncedComponent.PACKET_ID, (context, buffer) -> {
            Identifier componentTypeId = buffer.readIdentifier();
            ComponentType<?> componentType = ComponentRegistry.INSTANCE.get(componentTypeId);
            if (componentType == null) {
                return;
            }
            Component c = componentType.get(MinecraftClient.getInstance().world);
            if (c instanceof SyncedComponent) {
                ((SyncedComponent) c).processPacket(context, buffer);
            }
        });
    }

    private static void syncWorldComponents(ServerPlayerEntity player, World world) {
        Components.forEach(ComponentProvider.fromWorld(world), (componentType, component) -> {
            if (component instanceof SyncedComponent) {
                ((SyncedComponent) component).syncWith(player);
            }
        });
    }

}
