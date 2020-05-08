package nerdhub.cardinal.componentstest;

import nerdhub.cardinal.components.api.ChunkComponentFactory;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.componentstest.vita.ChunkVita;
import net.minecraft.world.chunk.Chunk;

public final class CcaTestFactory {
    @ChunkComponentFactory("componenttest:vita")
    public static Component create(Chunk c) {
        return new ChunkVita(c);
    }
}
