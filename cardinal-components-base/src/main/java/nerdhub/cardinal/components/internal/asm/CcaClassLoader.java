package nerdhub.cardinal.components.internal.asm;

/**
 * A class loader allowing the creation of any class from its bytecode, as well as its injection into the classpath
 */
public class CcaClassLoader extends ClassLoader {
    public static final CcaClassLoader INSTANCE = new CcaClassLoader();

    private CcaClassLoader() {
        super(CcaClassLoader.class.getClassLoader());
    }

    public Class<?> define(String name, byte[] data) {
        return defineClass(name, data, 0, data.length);
    }
}
