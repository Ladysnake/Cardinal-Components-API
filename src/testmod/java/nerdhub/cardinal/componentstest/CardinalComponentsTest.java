/*
 * Cardinal-Components-API
 * Copyright (C) 2019 GlassPane
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
import nerdhub.cardinal.components.api.event.*;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import nerdhub.cardinal.componentstest.vita.AmbientVita;
import nerdhub.cardinal.componentstest.vita.ChunkVita;
import nerdhub.cardinal.componentstest.vita.PlayerVita;
import nerdhub.cardinal.componentstest.vita.Vita;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.block.Material;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
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
    public static final ComponentType<Vita> VITA = ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier("componenttest:vita"), Vita.class)
        .attach(EntityComponentCallback.event(PlayerEntity.class), PlayerVita::new)
        .attach(WorldComponentCallback.EVENT, AmbientVita.WorldVita::new)
        .attach(LevelComponentCallback.EVENT, l -> new AmbientVita.LevelVita())
        .attach(ChunkComponentCallback.EVENT, ChunkVita::new);

    // inline self component callback registration
    public static final VitalityStickItem VITALITY_STICK = Registry.register(Registry.ITEM, "componenttest:vita_stick",
            ItemComponentCallback.registerSelf(new VitalityStickItem(new Item.Settings().group(ItemGroup.COMBAT))));

    public static final VitalityCondenser VITALITY_CONDENSER = Registry.register(Registry.BLOCK, "componenttest:vita_condenser",
            new VitalityCondenser(FabricBlockSettings.of(Material.STONE).dropsNothing().lightLevel(5).ticksRandomly().build()));

    public static final EntityType<VitalityZombieEntity> VITALITY_ZOMBIE = Registry.register(Registry.ENTITY_TYPE, "componenttest:vita_zombie",
            EntityType.Builder.create(VitalityZombieEntity::new, EntityCategory.MONSTER).build("zombie"));

    public static void init() {
        LOGGER.info("Hello, Components!");
        // Method reference on instance method, allows override by subclasses + access to protected variables
        EntityComponentCallback.event(VitalityZombieEntity.class).register(VitalityZombieEntity::initComponents);
        EntityComponents.setRespawnCopyStrategy(VITA, RespawnCopyStrategy.ALWAYS_COPY);
    }

    public static void clientInit() {
        EntityRendererRegistry.INSTANCE.register(VITALITY_ZOMBIE, (entityRenderDispatcher, context) -> new ZombieEntityRenderer(entityRenderDispatcher));
    }
}

