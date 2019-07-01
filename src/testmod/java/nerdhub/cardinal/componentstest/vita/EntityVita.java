package nerdhub.cardinal.componentstest.vita;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class EntityVita extends BaseVita {
    protected LivingEntity owner;

    public EntityVita(LivingEntity owner, int baseVitality) {
        this.owner = owner;
        this.vitality = baseVitality;
    }

    @Override
    public void setVitality(int value) {
        super.setVitality(value);
        if (!this.owner.world.isClient) {
            if (this.getVitality() == 0) {
                owner.addPotionEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 4000));
            } else if (this.getVitality() > 10) {
                owner.addPotionEffect(new StatusEffectInstance(StatusEffects.SPEED, 1000));
            }
        }
    }
}
