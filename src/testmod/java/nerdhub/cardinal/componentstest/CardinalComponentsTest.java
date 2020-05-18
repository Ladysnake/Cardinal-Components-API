/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2020 OnyxStudios
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package nerdhub.cardinal.componentstest;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.api.component.ComponentContainerMetafactory;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import nerdhub.cardinal.componentstest.vita.Vita;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Function;

public class CardinalComponentsTest {

    public static final Logger LOGGER = LogManager.getLogger("Component Test");
    public static final ComponentType<Vita> ALT_VITA = ComponentRegistry.INSTANCE.registerIfAbsent(TestStaticComponentInitializer.ALT_VITA_ID, Vita.class);

    public static final String VITA_STICK_ID = "componenttest:vita_stick";
    // inline self component callback registration
    public static final VitalityStickItem VITALITY_STICK = Registry.register(Registry.ITEM, VITA_STICK_ID,
            new VitalityStickItem(new Item.Settings().group(ItemGroup.COMBAT)));

    public static final VitalityCondenser VITALITY_CONDENSER = Registry.register(Registry.BLOCK, "componenttest:vita_condenser",
            new VitalityCondenser(FabricBlockSettings.of(Material.STONE).dropsNothing().lightLevel(5).ticksRandomly()));

    public static final EntityType<VitalityZombieEntity> VITALITY_ZOMBIE = Registry.register(Registry.ENTITY_TYPE, "componenttest:vita_zombie",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, VitalityZombieEntity::new).dimensions(EntityType.ZOMBIE.getDimensions()).build());

    public static void init() {
        LOGGER.info("Hello, Components!");
        FabricDefaultAttributeRegistry.register(VITALITY_ZOMBIE, ZombieEntity.createZombieAttributes());
        EntityComponents.setRespawnCopyStrategy(CardinalTestComponents.VITA, RespawnCopyStrategy.ALWAYS_COPY);
        LOGGER.info(ComponentContainerMetafactory.staticMetafactory(
            new Identifier(CardinalTestComponents.CUSTOM_PROVIDER_1),
            TestContainerFactory.class
        ).create(UUID.randomUUID(), null));
        try {
            LOGGER.info(ComponentContainerMetafactory.staticMetafactory(
                new Identifier(CardinalTestComponents.CUSTOM_PROVIDER_1),
                TestContainerFactory.class
            ).create(UUID.randomUUID(), null));
            assert false : "Only one factory should be created for any given provider type";
        } catch (StaticComponentLoadingException ignored) { }
        try {
            LOGGER.info(ComponentContainerMetafactory.<Function<UUID, ComponentContainer<SyncedComponent>>>staticMetafactory(
                new Identifier(CardinalTestComponents.CUSTOM_PROVIDER_2),
                Function.class,
                SyncedComponent.class,
                UUID.class
            ).apply(UUID.randomUUID()));
            assert false : "Registered factory does not return " + SyncedComponent.class;
        } catch (StaticComponentLoadingException ignored) { }
        LOGGER.info(ComponentContainerMetafactory.<Function<UUID, ComponentContainer<CopyableComponent<?>>>>staticMetafactory(
            new Identifier(CardinalTestComponents.CUSTOM_PROVIDER_2),
            Function.class,
            CopyableComponent.class,
            UUID.class
        ).apply(UUID.randomUUID()));
        LOGGER.info(ComponentContainerMetafactory.dynamicMetafactory(
            new Identifier(CardinalTestComponents.CUSTOM_PROVIDER_3),
            CopyableComponent.class,
            UUID.class
        ).apply(UUID.randomUUID()));
        LOGGER.info(ComponentContainerMetafactory.staticMetafactory(
            new Identifier("componenttest:no_factory"),
            TestContainerFactory.class
        ).create(UUID.randomUUID(), null));
    }

    public interface TestContainerFactory {
        ComponentContainer<?> create(UUID u, @Nullable PlayerEntity p);
    }
}
