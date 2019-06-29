package nerdhub.cardinal.componentstest;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.event.ItemComponentCallback;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CardinalComponentsTest {

    public static final Logger LOGGER = LogManager.getLogger("Component Test");
    public static final ComponentType<Vita> VITA = ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier("componenttest:vita"), Vita.class);

    // inline self component callback registration
    public static final VitalityStickItem VITALITY_STICK = Registry.register(Registry.ITEM, "componenttest:vita_stick",
            ItemComponentCallback.registerSelf(new VitalityStickItem(new Item.Settings().group(ItemGroup.COMBAT))));

    public static final EntityType<VitalityZombieEntity> VITALITY_ZOMBIE = Registry.register(Registry.ENTITY_TYPE, "componenttest:vita_zombie",
            EntityType.Builder.create(VitalityZombieEntity::new, EntityCategory.MONSTER).build("zombie"));

    public static void init() {
        LOGGER.info("Hello, Components!");
        // Method reference on instance method, allows override by subclasses + access to protected variables
        EntityComponentCallback.event(VitalityZombieEntity.class).register(VitalityZombieEntity::initComponents);
        EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> components.put(VITA, new EntityVita(player, 0)));
    }
}

