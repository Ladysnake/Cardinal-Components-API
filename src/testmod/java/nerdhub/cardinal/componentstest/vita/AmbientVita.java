package nerdhub.cardinal.componentstest.vita;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.sync.BaseSyncedComponent;
import nerdhub.cardinal.components.api.util.sync.LevelSyncedComponent;
import nerdhub.cardinal.components.api.util.sync.WorldSyncedComponent;
import nerdhub.cardinal.componentstest.CardinalComponentsTest;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.Objects;

public abstract class AmbientVita extends BaseVita implements BaseSyncedComponent {
    @Override
    public ComponentType<?> getComponentType() {
        return CardinalComponentsTest.VITA;
    }

    public abstract void syncWithAll(MinecraftServer server);

    @Override
    public void processPacket(PacketContext ctx, PacketByteBuf buf) {
        int vita = buf.readInt();
        ctx.getTaskQueue().execute(() -> {
            this.setVitality(vita);
            World world = ctx.getPlayer().world;
            // Very bad shortcut to get a dimension's name
            String worldName = Objects.requireNonNull(DimensionType.getId(world.getDimension().getType())).getPath().replace('_', ' ');
            String worldVita = I18n.translate(
                    "componenttest:title.world_vitality",
                    CardinalComponentsTest.VITA.get(world).getVitality(),
                    CardinalComponentsTest.VITA.get(world.getLevelProperties()).getVitality()
            );
            InGameHud inGameHud = MinecraftClient.getInstance().inGameHud;
            inGameHud.setTitles(null, worldVita, -1, -1, -1);
            inGameHud.setTitles(worldName, null, -1, -1, -1);
        });
    }

    @Override
    public void writeToPacket(PacketByteBuf buf) {
        buf.writeInt(this.getVitality());
    }

    /**
     * Implements markDirty and syncWith through {@code WorldSyncedComponent}
     */
    public static class WorldVita extends AmbientVita implements WorldSyncedComponent {
        private final World world;

        public WorldVita(World world) {
            this.world = world;
        }

        @Override
        public World getWorld() {
            return this.world;
        }

        @Override
        public void syncWithAll(MinecraftServer server) {
            WorldSyncedComponent.super.markDirty();
        }
    }

    /**
     * Implements markDirty and syncWith through {@code LevelSyncedComponent}
     */
    public static class LevelVita extends AmbientVita implements LevelSyncedComponent {
        @Override
        public void syncWithAll(MinecraftServer server) {
            LevelSyncedComponent.super.syncWithAll(server);
        }
    }
}
