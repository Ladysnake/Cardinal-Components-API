package nerdhub.cardinal.components;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import nerdhub.cardinal.components.api.util.sync.WorldSyncedComponent;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public final class ComponentsWorldNetworking {
    public static void init() {
        if (!FabricLoader.getInstance().isModLoaded("fabric-networking")) {
            return;
        }
        ClientSidePacketRegistry.INSTANCE.register(WorldSyncedComponent.PACKET_ID, (context, buffer) -> {
            int entityId = buffer.readInt();
            Identifier componentTypeId = buffer.readIdentifier();
            ComponentType<?> componentType = ComponentRegistry.INSTANCE.get(componentTypeId);
            if (componentType == null) {
                return;
            }
            Component c = componentType.get(context.getPlayer().world);
            if (c instanceof SyncedComponent) {
                ((SyncedComponent) c).processPacket(context, buffer);
            }
        });
    }
}
