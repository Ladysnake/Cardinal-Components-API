package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.internal.asm.FactoryClassScanner;
import org.objectweb.asm.tree.MethodNode;

import java.lang.annotation.Annotation;

public interface StaticComponentPlugin {
    /**
     * @return the annotation used by this plugin
     */
    Class<? extends Annotation> annotationType();

    /**
     *
     *
     * @param data ASM data about the method being processed
     * @param method the method node being processed
     * @return an identifier for a recognized component type, or {@code null}
     */
    String scan(FactoryClassScanner.AsmFactoryData data, MethodNode method);

    void generate();
}
