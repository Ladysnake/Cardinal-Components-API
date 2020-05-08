package nerdhub.cardinal.components.internal.asm;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import java.util.Locale;

public final class CcaAsmConstants {
    public static final int ASM_VERSION = Opcodes.ASM6;
    // existing references
    public static final String COMPONENT = "nerdhub/cardinal/components/api/component/Component";
    public static final String COMPONENT_CONTAINER = "nerdhub/cardinal/components/api/component/ComponentContainer";
    public static final String COMPONENT_TYPE = "nerdhub/cardinal/components/api/ComponentType";
    public static final String COMPONENT_PROVIDER = "nerdhub/cardinal/components/api/component/ComponentProvider";
    public static final String CONTAINER_FACTORY_IMPL = "nerdhub/cardinal/components/internal/FeedbackContainerFactory";
    public static final String DYNAMIC_COMPONENT_CONTAINER_IMPL = "nerdhub/cardinal/components/api/util/container/FastComponentContainer";
    public static final String DYNAMIC_COMPONENT_CONTAINER_PUT_DESC = "(Lnerdhub/cardinal/components/api/ComponentType;Lnerdhub/cardinal/components/api/component/Component;)Lnerdhub/cardinal/components/api/component/Component;";
    public static final String LAZY_COMPONENT_TYPE = "nerdhub/cardinal/components/api/util/LazyComponentType";
    public static final String IDENTIFIER = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_2960").replace('.', '/');
    public static final String EVENT = "net/fabricmc/fabric/api/event/Event";

    // generated references
    public static final String STATIC_COMPONENT_CONTAINER = "nerdhub/cardinal/components/_generated_/GeneratedComponentContainer";
    public static final String STATIC_CONTAINER_GETTER_DESC = "()Lnerdhub/cardinal/components/api/component/Component;";
    public static final String STATIC_COMPONENT_TYPE = "nerdhub/cardinal/components/_generated_/ComponentType";
    public static final String STATIC_COMPONENT_TYPES = "nerdhub/cardinal/components/_generated_/StaticComponentTypes";
    public static final String STATIC_CONTAINER_FACTORY = "nerdhub/cardinal/components/_generated_/GeneratedContainerFactory";

    public static String getComponentTypeName(String identifier) {
        return STATIC_COMPONENT_TYPE + "$" + getJavaIdentifierName(identifier);
    }

    @Nonnull
    public static String getJavaIdentifierName(String identifier) {
        return identifier.replace(':', '$');
    }

    @NotNull
    public static String getTypeConstantName(String identifier) {
        return getJavaIdentifierName(identifier).toUpperCase(Locale.ROOT);
    }

    @Nonnull
    public static String getStaticStorageGetterName(String identifier) {
        return "get$" + getJavaIdentifierName(identifier);
    }
}
