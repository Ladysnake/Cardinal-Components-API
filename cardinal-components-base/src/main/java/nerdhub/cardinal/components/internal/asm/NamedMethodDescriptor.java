package nerdhub.cardinal.components.internal.asm;


import org.objectweb.asm.Type;

public final class NamedMethodDescriptor {
    public final Type ownerType;
    public final String name;
    public final Type descriptor;
    public final Type[] args;

    public NamedMethodDescriptor(Type ownerType, String name, Type descriptor) {
        this.ownerType = ownerType;
        this.name = name;
        this.descriptor = descriptor;
        this.args = descriptor.getArgumentTypes();
    }

    @Override
    public String toString() {
        return ownerType + ";" + name + descriptor;
    }
}
