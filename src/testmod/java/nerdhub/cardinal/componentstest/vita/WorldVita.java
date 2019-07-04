package nerdhub.cardinal.componentstest.vita;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.sync.WorldSyncedComponent;
import nerdhub.cardinal.componentstest.CardinalComponentsTest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.Objects;

public class WorldVita extends BaseVita implements WorldSyncedComponent {
    private World world;

    public WorldVita(World world) {
        this.world = world;
    }

    @Override
    public void setVitality(int value) {
        super.setVitality(value);
        if (world.isClient) {
            // never do this in your mods.
            String worldName = Objects.requireNonNull(DimensionType.getId(world.getDimension().getType())).getPath().replace('_', ' ');
            String worldVita = I18n.translate("componenttest:title.world_vitality", this.getVitality());
            MinecraftClient.getInstance().inGameHud.setTitles(worldName, worldVita, 10, 40, 5);
        }
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @Override
    public ComponentType<?> getComponentType() {
        return CardinalComponentsTest.VITA;
    }
}
