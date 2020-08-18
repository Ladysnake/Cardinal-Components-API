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
package dev.onyxstudios.componenttest;

import com.google.common.reflect.TypeToken;
import dev.onyxstudios.cca.api.v3.component.ComponentContainer;
import dev.onyxstudios.cca.api.v3.util.ComponentContainerMetafactory;
import dev.onyxstudios.cca.internal.base.asm.StaticComponentLoadingException;
import dev.onyxstudios.componenttest.vita.BaseVita;
import dev.onyxstudios.componenttest.vita.Vita;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.component.extension.CopyableComponent;
import nerdhub.cardinal.components.api.component.extension.SyncedComponent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
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
import java.util.function.BiFunction;
import java.util.function.Function;

public class CardinalComponentsTest {

    public static final Logger LOGGER = LogManager.getLogger("Component Test");

    public static final Identifier VITA_STICK_ID = id("vita_stick");
    // inline self component callback registration
    public static final VitalityStickItem VITALITY_STICK = Registry.register(Registry.ITEM, VITA_STICK_ID,
            new VitalityStickItem(new Item.Settings().group(ItemGroup.COMBAT)));

    public static final VitalityCondenser VITALITY_CONDENSER = Registry.register(Registry.BLOCK, "componenttest:vita_condenser",
            new VitalityCondenser(FabricBlockSettings.of(Material.STONE).dropsNothing().lightLevel(5).ticksRandomly()));

    public static final EntityType<VitalityZombieEntity> VITALITY_ZOMBIE = Registry.register(Registry.ENTITY_TYPE, "componenttest:vita_zombie",
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, VitalityZombieEntity::new).dimensions(EntityType.ZOMBIE.getDimensions()).build());

    public static Identifier id(String path) {
        return new Identifier("componenttest", path);
    }

    public static void init() {
        LOGGER.info("Hello, Components!");

        for (int i = 0; i < 16; i++) {
            ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier(String.valueOf(Math.random())), Vita.class);
        }

        FabricDefaultAttributeRegistry.register(VITALITY_ZOMBIE, ZombieEntity.createZombieAttributes());

        ComponentContainer.FactoryBuilder<Integer> factoryBuilder = ComponentContainer.factoryBuilder(Integer.class)
            .component(TestComponents.VITA, BaseVita::new);
        Function<Integer, ComponentContainer> containerFactory = factoryBuilder.build();
        LOGGER.info(containerFactory.apply(3));
        LOGGER.info(containerFactory.apply(5));
        try {
            factoryBuilder.build();
            assert false : "Component container factory builders are single use";
        } catch (IllegalStateException ignored) { }
        try {
            ComponentRegistry.INSTANCE.registerStatic(TestComponents.OLD_VITA.getId(), TestComponents.OLD_VITA.getComponentClass());
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

        try {
            LOGGER.info(ComponentContainerMetafactory.metafactory(
                TestComponents.CUSTOM_PROVIDER_2,
                new TypeToken<BiFunction<UUID, PlayerEntity, ? extends ComponentContainer>>() {},
                new TypeToken<BiFunction<UUID, PlayerEntity, ? extends SyncedComponent>>() {}
            ).apply(UUID.randomUUID(), null));
            assert false : "Registered factory does not return " + SyncedComponent.class;
        } catch (StaticComponentLoadingException ignored) { }

        LOGGER.info(ComponentContainerMetafactory.metafactory(
            TestComponents.CUSTOM_PROVIDER_2,
            new TypeToken<BiFunction<UUID, PlayerEntity, ComponentContainer>>() {},
            new TypeToken<BiFunction<UUID, PlayerEntity, ? extends CopyableComponent<?>>>() {}
        ).apply(UUID.randomUUID(), null));

        LOGGER.info(ComponentContainerMetafactory.metafactory(
            TestComponents.CUSTOM_PROVIDER_3,
            TypeToken.of(TestContainerFactory.class),
            TestComponents.CUSTOM_FACTORY_TYPE,
            TestCallback.class,
            TestCallback.EVENT
        ).create(UUID.randomUUID(), null));

        LOGGER.info(ComponentContainerMetafactory.metafactory(
            new Identifier("componenttest:no_factory"),
            TypeToken.of(TestContainerFactory.class),
            new TypeToken<BiFunction<UUID, PlayerEntity, ? extends Component>>() {}
        ).create(UUID.randomUUID(), null));
    }

    public interface TestContainerFactory {
        ComponentContainer create(UUID u, @Nullable PlayerEntity p);
    }

    public interface TestCallback {
        Event<TestCallback> EVENT = EventFactory.createArrayBacked(TestCallback.class, callbacks -> (uuid, p, c) -> {
            for (TestCallback callback : callbacks) {
                callback.initComponents(uuid, p, c);
            }
        });

        void initComponents(UUID uuid, @Nullable PlayerEntity p, nerdhub.cardinal.components.api.component.ComponentContainer<Component> components);
    }
}
