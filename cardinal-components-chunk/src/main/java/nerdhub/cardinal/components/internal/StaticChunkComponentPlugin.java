package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.ChunkComponentFactory;
import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.internal.asm.CcaAsmHelper;
import nerdhub.cardinal.components.internal.asm.FactoryClassScanner;
import nerdhub.cardinal.components.internal.asm.NamedMethodDescriptor;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class StaticChunkComponentPlugin implements StaticComponentPlugin {
    public static final String CHUNK_IMPL_SUFFIX = "ChunkImpl";
    public static final StaticChunkComponentPlugin INSTANCE = new StaticChunkComponentPlugin();

    private final Map<String, NamedMethodDescriptor> componentFactories = new HashMap<>();
    private final String chunkClass = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_2791");
    private Class<? extends FeedbackContainerFactory<?, ?>> factoryClass;

    public Class<? extends FeedbackContainerFactory<?, ?>> getFactoryClass() {
        return Objects.requireNonNull(this.factoryClass, "PreLaunch not fired ?!");
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return ChunkComponentFactory.class;
    }

    @Override
    public String scan(FactoryClassScanner.AsmFactoryData data, MethodNode method) {
        NamedMethodDescriptor factoryDescriptor = data.getFactoryDescriptor();
        if (factoryDescriptor.args.length > 1) {
            throw new StaticComponentLoadingException("Too many arguments in methods " + factoryDescriptor + ". Should be either no-args or a single " + chunkClass + " argument.");
        }
        String value = (String) data.get("value");
        componentFactories.put(value, factoryDescriptor);
        return value;
    }

    @Override
    public void generate() {
        Type chunkType = Type.getObjectType(chunkClass.replace('.', '/'));
        Class<? extends ComponentContainer<?>> containerCls = CcaAsmHelper.defineContainer(this.componentFactories, CHUNK_IMPL_SUFFIX, chunkType);
        this.factoryClass = CcaAsmHelper.createSingleArgFactory(CHUNK_IMPL_SUFFIX, Type.getType(containerCls), chunkType);
    }
}
