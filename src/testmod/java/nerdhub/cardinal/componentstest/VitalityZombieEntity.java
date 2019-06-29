package nerdhub.cardinal.componentstest;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;

public class VitalityZombieEntity extends ZombieEntity {
    public VitalityZombieEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.world.isClient) {
            this.world.addParticle(ParticleTypes.DRAGON_BREATH, this.x, this.y + 0.3D, this.z, this.random.nextGaussian() * 0.05D, this.random.nextGaussian() * 0.05D, this.random.nextGaussian() * 0.05D);
        }
    }

    protected void initComponents(ComponentContainer components) {
        components.put(CardinalComponentsTest.VITA, new EntityVita(this, 20));
    }
}
