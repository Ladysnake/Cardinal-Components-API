/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2021 OnyxStudios
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
package dev.onyxstudios.componenttest;

import com.google.common.reflect.TypeToken;
import dev.onyxstudios.cca.api.v3.block.BlockComponents;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.CopyableComponent;
import dev.onyxstudios.cca.api.v3.util.ComponentContainerMetafactory;
import dev.onyxstudios.componenttest.vita.BaseVita;
import dev.onyxstudios.componenttest.vita.Vita;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.ChunkStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

public class CardinalComponentsTest {

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
        id("ccagroup"),
        () -> new ItemStack(Blocks.COBBLESTONE));   // confirm that lazy item components work

    public static final Logger LOGGER = LogManager.getLogger("Component Test");

    public static final Identifier VITA_STICK_ID = id("vita_stick");
    // inline self component callback registration
    public static final VitalityStickItem VITALITY_STICK = Registry.register(Registry.ITEM, VITA_STICK_ID,
            new VitalityStickItem(new Item.Settings().group(ITEM_GROUP).maxDamage(50)));

    public static final VitalityCondenser VITALITY_CONDENSER = new VitalityCondenser(FabricBlockSettings.of(Material.STONE).dropsNothing().lightLevel(5).ticksRandomly());

    public static final EntityType<VitalityZombieEntity> VITALITY_ZOMBIE = Registry.register(Registry.ENTITY_TYPE, "componenttest:vita_zombie",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, VitalityZombieEntity::new).dimensions(EntityType.ZOMBIE.getDimensions()).build());

    public static final BlockApiLookup<Vita, Direction> VITA_API_LOOKUP = BlockApiLookup.get(CardinalComponentsTest.id("sided_vita"), Vita.class, Direction.class);

    public static Identifier id(String path) {
        return new Identifier("componenttest", path);
    }

    public static void init() {
        LOGGER.info("Hello, Components!");

        Registry.register(Registry.BLOCK, "componenttest:vita_condenser", VITALITY_CONDENSER);

        FabricDefaultAttributeRegistry.register(VITALITY_ZOMBIE, ZombieEntity.createZombieAttributes());

        ComponentContainer.Factory.Builder<Integer> factoryBuilder = ComponentContainer.Factory.builder(Integer.class)
            .component(TestComponents.VITA, BaseVita::new);
        ComponentContainer.Factory<Integer> containerFactory = factoryBuilder.build();
        LOGGER.info(containerFactory.createContainer(3));
        LOGGER.info(containerFactory.createContainer(5));
        try {
            factoryBuilder.build();
            assert false : "Component container factory builders are single use";
        } catch (IllegalStateException ignored) { }

        try {
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("hi"), Vita.class);
            assert false : "Static components must be registered through mod metadata or plugin";
        } catch (IllegalStateException ignored) { }

        LOGGER.info(ComponentContainerMetafactory.metafactory(
            TestComponents.CUSTOM_PROVIDER_1,
            TypeToken.of(TestContainerFactory.class),
            new TypeToken<BiFunction<UUID, PlayerEntity, ? extends Component>>() {}
        ).create(UUID.randomUUID(), null));

        try {
            LOGGER.info(ComponentContainerMetafactory.metafactory(
                TestComponents.CUSTOM_PROVIDER_1,
                TypeToken.of(TestContainerFactory.class),
                new TypeToken<BiFunction<UUID, PlayerEntity, ? extends Component>>() {}
            ).create(UUID.randomUUID(), null));
            assert false : "Only one factory should be created for any given provider type";
        } catch (IllegalStateException ignored) { }

        LOGGER.info(ComponentContainerMetafactory.metafactory(
            TestComponents.CUSTOM_PROVIDER_2,
            new TypeToken<BiFunction<UUID, PlayerEntity, ComponentContainer>>() {},
            new TypeToken<BiFunction<UUID, PlayerEntity, ? extends CopyableComponent<?>>>() {}
        ).apply(UUID.randomUUID(), null));

        LOGGER.info(ComponentContainerMetafactory.metafactory(
            new Identifier("componenttest:no_factory"),
            TypeToken.of(TestContainerFactory.class),
            new TypeToken<BiFunction<UUID, PlayerEntity, ? extends Component>>() {}
        ).create(UUID.randomUUID(), null));

        UseItemCallback.EVENT.register((playerEntity, world, hand) -> {
            ItemStack stack = playerEntity.getStackInHand(hand);
            LOGGER.info("{} vitality: {}", stack, TestComponents.ALT_VITA.get(stack).getVitality()); // init components
            return TypedActionResult.pass(stack);
        });

        VITA_API_LOOKUP.registerForBlocks(
            (world, pos, state, blockEntity, context) -> TestComponents.VITA.get(Objects.requireNonNull(world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false))),
            VITALITY_CONDENSER
        );
        BlockComponents.exposeApi(TestComponents.VITA, VITA_API_LOOKUP, (vita, side) -> side == Direction.UP ? vita : null, BlockEntityType.END_PORTAL);
        BlockComponents.exposeApi(VitaCompound.KEY, VITA_API_LOOKUP, VitaCompound::get, BlockEntityType.END_GATEWAY);
    }

    public interface TestContainerFactory {
        ComponentContainer create(UUID u, @Nullable PlayerEntity p);
    }
}
