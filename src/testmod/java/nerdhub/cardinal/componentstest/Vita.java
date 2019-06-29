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

import nerdhub.cardinal.components.api.component.Component;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.CompoundTag;

public interface Vita extends Component {
    int getVitality();
    void setVitality(int value);
    default int transferTo(Vita dest, int amount) {
        int sourceVitality = this.getVitality();
        int actualAmount = Math.min(sourceVitality, amount);
        this.setVitality(sourceVitality - actualAmount);
        dest.setVitality(dest.getVitality() + actualAmount);
        return amount - actualAmount;
    }
}

class BaseVita implements Vita {
    protected int vitality;

    @Override
    public int getVitality() {
        return this.vitality;
    }

    @Override
    public void setVitality(int value) {
        this.vitality = value;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.vitality = tag.getInt("vitality");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("vitality", this.vitality);
        return tag;
    }

    @Override
    public Component newInstance() {
        return new BaseVita();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseVita baseVita = (BaseVita) o;
        return vitality == baseVita.vitality;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(vitality);
    }
}

class EntityVita extends BaseVita {
    private LivingEntity owner;

    public EntityVita(LivingEntity owner, int baseVitality) {
        this.owner = owner;
        this.vitality = baseVitality;
    }

    @Override
    public void setVitality(int value) {
        super.setVitality(value);
        if (this.getVitality() == 0) {
            owner.addPotionEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 4000));
        } else if (this.getVitality() > 10) {
            owner.addPotionEffect(new StatusEffectInstance(StatusEffects.SPEED, 1000));
        }
    }
}
