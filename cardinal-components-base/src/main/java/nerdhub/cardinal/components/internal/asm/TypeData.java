package nerdhub.cardinal.components.internal.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public final class TypeData {
    private final Type type;
    private final Type supertype;
    private final ClassReader reader;
    private ClassNode node;

    public TypeData(Type type, Type supertype, ClassReader reader) {
        this.type = type;
        this.supertype = supertype;
        this.reader = reader;
    }

    public Type getType() {
        return this.type;
    }

    public Type getSupertype() {
        return this.supertype;
    }

    public ClassReader getReader() {
        return this.reader;
    }

    public ClassNode getNode() {
        if (this.node == null) {
            this.node = new ClassNode(CcaAsmConstants.ASM_VERSION);
            this.reader.accept(this.node, 0);
        }
        return this.node;
    }

    @Override
    public String toString() {
        return this.type.toString();
    }
}
