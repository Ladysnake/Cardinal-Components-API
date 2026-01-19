/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2025 Ladysnake
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
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("ccagroup"));

    public static final CreativeModeTab ITEM_GROUP = FabricCreativeModeTab.builder()
        .title(Component.translatable("componenttest:item_group"))
        .icon(() -> new ItemStack(Items.COBBLESTONE))
        .build();

    public static final Logger LOGGER = LogManager.getLogger("Component Test");

    public static final ResourceKey<Item> VITA_STICK_ID = ResourceKey.create(Registries.ITEM, id("vita_stick"));
    // inline self component callback registration
    public static final VitalityStickItem VITALITY_STICK = Registry.register(BuiltInRegistries.ITEM, VITA_STICK_ID,
            new VitalityStickItem(new Item.Properties().durability(50).setId(VITA_STICK_ID)));

    public static final ResourceKey<Block> VITALITY_CONDENSER_ID = ResourceKey.create(Registries.BLOCK, id("vita_condenser"));
    public static final VitalityCondenser VITALITY_CONDENSER = Registry.register(BuiltInRegistries.BLOCK, VITALITY_CONDENSER_ID,
        new VitalityCondenser(net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(VITALITY_CONDENSER_ID).noLootTable().lightLevel(s -> 5).randomTicks()));

    public static final ResourceKey<EntityType<?>> VITALITY_ZOMBIE_ID = ResourceKey.create(Registries.ENTITY_TYPE, id("vita_zombie"));
    public static final EntityType<VitalityZombieEntity> VITALITY_ZOMBIE = Registry.register(BuiltInRegistries.ENTITY_TYPE, VITALITY_ZOMBIE_ID,
            EntityType.Builder.of(VitalityZombieEntity::new, MobCategory.MONSTER)
                .sized(EntityType.ZOMBIE.getDimensions().width(), EntityType.ZOMBIE.getDimensions().height())
                .build(VITALITY_ZOMBIE_ID));

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("componenttest", path);
    }

    public static void init() {
        LOGGER.info("Hello, Components!");

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ITEM_GROUP_KEY, ITEM_GROUP);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("vita"), ItemVita.Data.COMPONENT_TYPE);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("alt_vita"), ItemVita.Data.ALT_COMPONENT_TYPE);

        CommandRegistrationCallback.EVENT.register((dispatcher, reg, dedicated) -> VitaCommand.register(dispatcher));

        FabricDefaultAttributeRegistry.register(VITALITY_ZOMBIE, Zombie.createAttributes());
        ItemVita.LOOKUP.registerForItems((stack, ctx) -> new ItemVita(ItemVita.Data.COMPONENT_TYPE, stack), VITALITY_STICK);

        ComponentContainer.Factory.Builder<Integer> factoryBuilder = ComponentContainer.Factory.builder(Integer.class)
            .component(Vita.KEY, BaseVita::new);
        ComponentContainer.Factory<Integer> containerFactory = factoryBuilder.build();
        CreativeModeTabEvents.modifyOutputEvent(CardinalComponentsTest.ITEM_GROUP_KEY).register(entries -> entries.accept(CardinalComponentsTest.VITALITY_STICK));
        LOGGER.info(containerFactory.createContainer(3));
        LOGGER.info(containerFactory.createContainer(5));
        try {
            factoryBuilder.build();
            assert false : "Component container factory builders are single use";
        } catch (IllegalStateException ignored) { }

        try {
            ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.parse("hi"), Vita.class);
            assert false : "Static components must be registered through mod metadata or plugin";
        } catch (IllegalStateException ignored) { }

        LOGGER.info(new GenericContainerBuilder<>(
            TestComponentFactory.class,
            TestContainerFactory.class,
            List.of(UUID.class, Player.class),
            (u, p) -> ComponentContainer.EMPTY
        ).build().create(UUID.randomUUID(), null));

        try {
            LOGGER.info(new GenericContainerBuilder<>(
                TestComponentFactory.class,
                TestContainerFactory.class,
                List.of(UUID.class, Player.class),
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
        C create(UUID u, @Nullable Player p);
    }

    public interface TestContainerFactory {
        ComponentContainer create(UUID u, @Nullable Player p);
    }
}
