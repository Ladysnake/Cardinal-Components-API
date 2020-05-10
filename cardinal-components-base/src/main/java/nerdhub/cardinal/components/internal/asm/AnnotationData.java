package nerdhub.cardinal.components.internal.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public final class AnnotationData {
    public static AnnotationData create(AnnotationNode node) throws IOException {
        List<Object> values = node.values;
        ClassNode annotationClass = CcaAsmHelper.getClassNode(Type.getType(node.desc));
        Map<String, Object> data = new HashMap<>((int) Math.ceil(values.size() / 2.0));
        Map<String, Object> defaultData = new HashMap<>(annotationClass.methods.size());
        for (int i = 0; i < values.size(); i+= 2) {
            data.put((String) values.get(i), values.get(i+1));
        }
        for (MethodNode method : annotationClass.methods) {
            defaultData.put(method.name, method.annotationDefault);
        }
        return new AnnotationData(data, defaultData);
    }

    private final Map<String, Object> annotationData;
    private final Map<String, Object> defaultData;

    private AnnotationData(Map<String, Object> annotationData, Map<String, Object> defaultData) {
        this.annotationData = annotationData;
        this.defaultData = defaultData;
    }

    /**
     * @param key the value name
     * @param type the requested value type, which must be {@link Byte}, {@link Boolean}, {@link
     * Character}, {@link Short}, {@link Integer} , {@link Long}, {@link Float}, {@link Double},
     * {@link String}, {@link AnnotationNode} or {@link List} sort..
     * @return a {@link Byte}, {@link Boolean}, {@link Character}, {@link Short},
     * {@link Integer}, {@link Long}, {@link Float},
     * {@link Double}, {@link String} or {@link org.objectweb.asm.Type}, or a two elements String
     * array (for enumeration values), an {@link AnnotationNode}, or a {@link List} of values of one
     * of the preceding types.
     */
    public <T> T get(String key, Class<? super T> type) {
        T value = castUnsafe(type, this.annotationData.getOrDefault(key, this.defaultData.get(key)));
        if (value == null) throw new NoSuchElementException("Unrecognized annotation key " + key);
        return value;
    }

    /**
     * Gets the value of an annotation <em>if it was explicitly defined in the declaration</em>.
     *
     *  @param key the value name
     * @param type the requested value type, which must represent the type {@link Byte}, {@link Boolean}, {@link
     * Character}, {@link Short}, {@link Integer} , {@link Long}, {@link Float}, {@link Double},
     * {@link String}, {@link AnnotationNode} or {@link List}.
     * @return a {@link Byte}, {@link Boolean}, {@link Character}, {@link Short},
     * {@link Integer}, {@link Long}, {@link Float},
     * {@link Double}, {@link String} or {@link org.objectweb.asm.Type}, or a two elements String
     * array (for enumeration values), an {@link AnnotationNode}, or a {@link List} of values of one
     * of the preceding types.
     */
    @Nullable
    public <T> T getIfDeclared(String key, Class<? super T> type) {
        return castUnsafe(type, this.annotationData.get(key));
    }

    @SuppressWarnings("unchecked")
    private static <T> T castUnsafe(Class<? super T> type, Object ret) {
        return (T) type.cast(ret);
    }

    public AnnotationData getInnerAnnotation(String name) throws IOException {
        return create(this.get(name, AnnotationNode.class));
    }

    public List<AnnotationData> getInnerAnnotationList(String name) throws IOException {
        List<AnnotationNode> raw = this.get(name, List.class);
        List<AnnotationData> resolved = new ArrayList<>();
        for (AnnotationNode annotationNode : raw) {
            resolved.add(create(annotationNode));
        }
        return resolved;
    }
}
