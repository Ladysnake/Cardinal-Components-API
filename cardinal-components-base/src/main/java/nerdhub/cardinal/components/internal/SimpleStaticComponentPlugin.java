package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.internal.asm.AnnotationData;
import nerdhub.cardinal.components.internal.asm.CcaAsmHelper;
import nerdhub.cardinal.components.internal.asm.NamedMethodDescriptor;
import nerdhub.cardinal.components.internal.asm.StaticComponentLoadingException;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class SimpleStaticComponentPlugin implements StaticComponentPlugin {
    private final Map<String, NamedMethodDescriptor> componentFactories = new HashMap<>();
    private final String providerClass;
    private final String implSuffix;
    private final Class<? extends Annotation> annotationType;
    private Class<? extends FeedbackContainerFactory<?, ?>> factoryClass;

    protected SimpleStaticComponentPlugin(String className, String implSuffix, Class<? extends Annotation> annotationType) {
        this.providerClass = className;
        this.implSuffix = implSuffix;
        this.annotationType = annotationType;
    }

    public Class<? extends FeedbackContainerFactory<?, ?>> getFactoryClass() {
        return Objects.requireNonNull(this.factoryClass, "PreLaunch not fired ?!");
    }

    @Override
    public Class<? extends Annotation> getAnnotationType() {
        return this.annotationType;
    }

    @ApiStatus.OverrideOnly
    @Override
    public String scan(NamedMethodDescriptor factoryDescriptor, AnnotationData data, MethodNode method) {
        if (factoryDescriptor.descriptor.getArgumentTypes().length > 1) {
            throw new StaticComponentLoadingException("Too many arguments in method " + factoryDescriptor + ". Should be either no-args or a single " + this.providerClass + " argument.");
        }
        String value = data.get("value", String.class);
        this.componentFactories.put(value, factoryDescriptor);
        return value;
    }

    @ApiStatus.OverrideOnly
    @Override
    public void generate() {
        Type levelType = Type.getObjectType(this.providerClass.replace('.', '/'));
        Class<? extends ComponentContainer<?>> containerCls = CcaAsmHelper.defineContainer(this.componentFactories, this.implSuffix, levelType);
        this.factoryClass = CcaAsmHelper.defineSingleArgFactory(this.implSuffix, Type.getType(containerCls), levelType);
    }
}
