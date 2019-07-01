package nerdhub.cardinal.componentstest.vita;

import nerdhub.cardinal.components.api.component.trait.CloneableComponent;
import net.minecraft.nbt.CompoundTag;

public class BaseVita implements Vita, CloneableComponent<BaseVita> {
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

    public BaseVita newInstance() {
        return new BaseVita();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vita)) return false;
        return vitality == ((Vita) o).getVitality();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(vitality);
    }
}
