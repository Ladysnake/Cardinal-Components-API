package nerdhub.cardinal.componentstest.vita;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.component.sync.EntitySyncedComponent;
import nerdhub.cardinal.componentstest.CardinalComponentsTest;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * A Vita component attached to players, and automatically synchronized with their owner
 */
public class PlayerVita extends EntityVita implements EntitySyncedComponent {

    public PlayerVita(PlayerEntity owner, int baseVitality) {
        super(owner, baseVitality);
    }

    @Override
    public void markDirty() {
        if (!this.getEntity().world.isClient) {
            // We only sync with the holder, not with everyone around
            this.syncWith((ServerPlayerEntity) this.getEntity());
        }
    }

    @Override
    public void setVitality(int value) {
        super.setVitality(value);
        this.markDirty();
    }

    @Override
    public LivingEntity getEntity() {
        return this.owner;
    }

    @Override
    public ComponentType<?> getComponentType() {
        // Hardcoded but could be passed in the constructor
        return CardinalComponentsTest.VITA;
    }
}
