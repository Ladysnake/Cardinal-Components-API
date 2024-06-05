/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2024 Ladysnake
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
package org.ladysnake.componenttest.content;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.ChunkStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentContainer;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.internal.base.GenericContainerBuilder;
import org.ladysnake.cca.test.base.BaseVita;
import org.ladysnake.cca.test.base.Vita;
import org.ladysnake.cca.test.block.CcaBlockTestMod;
import org.ladysnake.componenttest.content.vita.ItemVita;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CardinalComponentsTest {

    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, id("ccagroup"));

    public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder()
        .displayName(Text.translatable("componenttest:item_group"))
        .icon(() -> new ItemStack(Items.COBBLESTONE))
        .build();

    public static final Logger LOGGER = LogManager.getLogger("Component Test");

    public static final Identifier VITA_STICK_ID = id("vita_stick");
    // inline self component callback registration
    public static final VitalityStickItem VITALITY_STICK = Registry.register(Registries.ITEM, VITA_STICK_ID,
            new VitalityStickItem(new Item.Settings().maxDamage(50)));

    public static final VitalityCondenser VITALITY_CONDENSER = new VitalityCondenser(FabricBlockSettings.copyOf(Blocks.STONE).dropsNothing().luminance(s -> 5).ticksRandomly());

    public static final EntityType<VitalityZombieEntity> VITALITY_ZOMBIE = Registry.register(Registries.ENTITY_TYPE, "componenttest:vita_zombie",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, VitalityZombieEntity::new).dimensions(EntityType.ZOMBIE.getDimensions()).build());

    public static Identifier id(String path) {
        return Identifier.of("componenttest", path);
    }

    public static void init() {
        LOGGER.info("Hello, Components!");

        Registry.register(Registries.ITEM_GROUP, ITEM_GROUP_KEY, ITEM_GROUP);
        Registry.register(Registries.DATA_COMPONENT_TYPE, id("vita"), ItemVita.Data.COMPONENT_TYPE);
        Registry.register(Registries.DATA_COMPONENT_TYPE, id("alt_vita"), ItemVita.Data.ALT_COMPONENT_TYPE);

        Registry.register(Registries.BLOCK, "componenttest:vita_condenser", VITALITY_CONDENSER);
        CommandRegistrationCallback.EVENT.register((dispatcher, reg, dedicated) -> VitaCommand.register(dispatcher));

        FabricDefaultAttributeRegistry.register(VITALITY_ZOMBIE, ZombieEntity.createZombieAttributes());
        ItemVita.LOOKUP.registerForItems((stack, ctx) -> new ItemVita(ItemVita.Data.COMPONENT_TYPE, stack), VITALITY_STICK);

        ComponentContainer.Factory.Builder<Integer> factoryBuilder = ComponentContainer.Factory.builder(Integer.class)
            .component(Vita.KEY, BaseVita::new);
        ComponentContainer.Factory<Integer> containerFactory = factoryBuilder.build();
        ItemGroupEvents.modifyEntriesEvent(CardinalComponentsTest.ITEM_GROUP_KEY).register(entries -> entries.add(CardinalComponentsTest.VITALITY_STICK));
        LOGGER.info(containerFactory.createContainer(3));
        LOGGER.info(containerFactory.createContainer(5));
        try {
            factoryBuilder.build();
            assert false : "Component container factory builders are single use";
        } catch (IllegalStateException ignored) { }

        try {
            ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of("hi"), Vita.class);
            assert false : "Static components must be registered through mod metadata or plugin";
        } catch (IllegalStateException ignored) { }

        LOGGER.info(new GenericContainerBuilder<>(
            TestComponentFactory.class,
            TestContainerFactory.class,
            List.of(UUID.class, PlayerEntity.class),
            (u, p) -> ComponentContainer.EMPTY
        ).build().create(UUID.randomUUID(), null));

        try {
            LOGGER.info(new GenericContainerBuilder<>(
                TestComponentFactory.class,
                TestContainerFactory.class,
                List.of(UUID.class, PlayerEntity.class),
                (u, p) -> ComponentContainer.EMPTY
            ).build().create(UUID.randomUUID(), null));
            assert false : "Only one factory should be created for any given provider type";
        } catch (IllegalStateException ignored) { }

        CcaBlockTestMod.VITA_API_LOOKUP.registerForBlocks(
            (world, pos, state, blockEntity, context) -> Vita.KEY.get(Objects.requireNonNull(world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false))),
            VITALITY_CONDENSER
        );
    }

    @FunctionalInterface
    public interface TestComponentFactory<C extends Component> {
        C create(UUID u, @Nullable PlayerEntity p);
    }

    public interface TestContainerFactory {
        ComponentContainer create(UUID u, @Nullable PlayerEntity p);
    }
}
