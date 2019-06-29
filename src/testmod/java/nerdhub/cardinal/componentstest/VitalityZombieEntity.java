package nerdhub.cardinal.componentstest;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;

public class VitalityZombieEntity extends ZombieEntity {
    public VitalityZombieEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
    }

    protected void initComponents(ComponentContainer components) {
        components.put(CardinalComponentsTest.VITA, new EntityVita(this, 20));
    }
}
