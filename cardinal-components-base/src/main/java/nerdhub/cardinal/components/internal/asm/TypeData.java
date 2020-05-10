package nerdhub.cardinal.components.internal.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

public final class TypeData {
    private final Type type;
    private final Type supertype;
    private final ClassReader reader;

    public TypeData(Type type, Type supertype, ClassReader reader) {
        this.type = type;
        this.supertype = supertype;
        this.reader = reader;
    }

    public Type getType() {
        return type;
    }

    public Type getSupertype() {
        return supertype;
    }

    public ClassReader getReader() {
        return reader;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
