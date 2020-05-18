package nerdhub.cardinal.componentstest;

import nerdhub.cardinal.components.api.component.*;
import nerdhub.cardinal.componentstest.vita.BaseVita;
import nerdhub.cardinal.componentstest.vita.PlayerVita;
import nerdhub.cardinal.componentstest.vita.Vita;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class TestStaticComponentInitializer implements StaticEntityComponentInitializer, StaticChunkComponentInitializer, StaticLevelComponentInitializer, StaticWorldComponentInitializer, StaticGenericComponentInitializer, StaticItemComponentInitializer {
    public static final Identifier VITA_ID = new Identifier(CardinalTestComponents.VITA_ID);
    public static final Identifier ALT_VITA_ID = new Identifier("componenttest:alt_vita");
    public static final Identifier CUSTOM_PROVIDER_1 = new Identifier("componenttest:custom/1");
    public static final Identifier CUSTOM_PROVIDER_2 = new Identifier("componenttest:custom/2");
    public static final Identifier CUSTOM_PROVIDER_3 = new Identifier("componenttest:custom/3");

    @Override
    public Set<Identifier> getSupportedComponentTypes() {
        return new HashSet<>(Arrays.asList(ALT_VITA_ID, VITA_ID));
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry, MethodHandles.Lookup lookup) throws ReflectiveOperationException {
        registry.register(ALT_VITA_ID, HostileEntity.class, lookup.findStatic(TestStaticComponentInitializer.class, "create", MethodType.methodType(Vita.class)));
        registry.register(VITA_ID, LivingEntity.class, lookup.findStatic(CardinalTestComponents.class, "createForEntity", MethodType.methodType(Vita.class)));
        registry.register(VITA_ID, PlayerEntity.class, lookup.findStatic(CardinalTestComponents.class, "createForEntity", MethodType.methodType(PlayerVita.class, PlayerEntity.class)));
        registry.register(VITA_ID, VitalityZombieEntity.class, lookup.findStatic(CardinalTestComponents.class, "createForEntity", MethodType.methodType(Component.class, VitalityZombieEntity.class)));
        registry.register(VITA_ID, PhantomEntity.class, lookup.findStatic(CardinalTestComponents.class, "createForPhantom", MethodType.methodType(Vita.class)));
    }

    @Override
    public void registerChunkComponentFactories(ChunkComponentFactoryRegistry registry, MethodHandles.Lookup lookup) throws ReflectiveOperationException {
        registry.register(VITA_ID, lookup.findStatic(CardinalTestComponents.class, "createForChunk", MethodType.methodType(Component.class, Chunk.class)));
    }

    @Override
    public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry, MethodHandles.Lookup lookup) throws ReflectiveOperationException {
        registry.register(VITA_ID, lookup.findStatic(CardinalTestComponents.class, "createForSave", MethodType.methodType(Component.class)));
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry, MethodHandles.Lookup lookup) throws ReflectiveOperationException {
        registry.register(VITA_ID, lookup.findStatic(CardinalTestComponents.class, "createForWorld", MethodType.methodType(Component.class, World.class)));
    }

    @Override
    public void registerGenericComponentFactories(GenericComponentFactoryRegistry registry, MethodHandles.Lookup lookup) throws ReflectiveOperationException {
        registry.register(VITA_ID, CUSTOM_PROVIDER_1, lookup.findStatic(CardinalTestComponents.class, "createForThirdParty", MethodType.methodType(BaseVita.class)));
        registry.register(VITA_ID, CUSTOM_PROVIDER_2, lookup.findStatic(CardinalTestComponents.class, "createForThirdParty", MethodType.methodType(BaseVita.class)));
        registry.register(VITA_ID, CUSTOM_PROVIDER_3, lookup.findStatic(CardinalTestComponents.class, "createForThirdParty", MethodType.methodType(BaseVita.class)));
    }

    @Override
    public void registerItemComponentFactories(ItemComponentFactoryRegistry registry, MethodHandles.Lookup lookup) throws ReflectiveOperationException {
        registry.register(VITA_ID, null, lookup.findStatic(CardinalTestComponents.class, "createForStack", MethodType.methodType(Vita.class, ItemStack.class)));
        registry.register(VITA_ID, new Identifier(CardinalComponentsTest.VITA_STICK_ID), lookup.findStatic(CardinalTestComponents.class, "createForStick", MethodType.methodType(Component.class)));
    }

    @Override
    public void finalizeStaticBootstrap() {
        CardinalComponentsTest.LOGGER.info("CCA Bootstrap complete!");
    }

    public static Vita create() {
        return new BaseVita();
    }
}
