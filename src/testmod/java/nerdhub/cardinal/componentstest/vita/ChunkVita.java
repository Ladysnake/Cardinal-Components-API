package nerdhub.cardinal.componentstest.vita;

import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.NativeCloneableComponent;
import nerdhub.cardinal.components.api.util.sync.ChunkSyncedComponent;
import nerdhub.cardinal.componentstest.CardinalComponentsTest;
import net.minecraft.world.chunk.Chunk;

public class ChunkVita extends BaseVita implements ChunkSyncedComponent {
    private final Chunk chunk;

    public ChunkVita(Chunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public Chunk getChunk() {
        return this.chunk;
    }

    @Override
    public ComponentType<?> getComponentType() {
        return CardinalComponentsTest.VITA;
    }

    /**
     * Because we use {@link NativeCloneableComponent}, this override is superfluous. Otherwise, it would
     * be required so that the returned object is of the right class.
     */
    @Override
    public ChunkVita newInstance() {
        // Native cloning guarantees the returned object is of the right class
        return (ChunkVita) super.newInstance();
    }
}
