/*
 * Cardinal-Components-API
 * Copyright (C) 2019-2020 OnyxStudios
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package nerdhub.cardinal.components.internal.asm;


import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.lang.invoke.MethodHandleInfo;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class MethodData {
    public final Type ownerType;
    public final String name;
    public final int access;
    public final Type descriptor;

    public MethodData(MethodHandleInfo factoryInfo) {
        this(
            Type.getType(factoryInfo.getDeclaringClass()),
            factoryInfo.getName(),
            factoryInfo.getModifiers(),
            Type.getMethodType(Type.getType(factoryInfo.getMethodType().returnType()), factoryInfo.getMethodType().parameterList().stream().map(Type::getType).toArray(Type[]::new))
        );
    }

    public MethodData(Type ownerType, MethodNode node) {
        this(ownerType, node.name, node.access, Type.getMethodType(node.desc));
    }

    public MethodData(Type ownerType, String name, int access, Type descriptor) {
        this.ownerType = ownerType;
        this.name = name;
        this.access = access;
        this.descriptor = descriptor;
    }

    @Override
    public String toString() {
        return Modifier.toString(this.access) + " "
            + this.descriptor.getReturnType().getClassName() + " "
            + this.ownerType.getClassName() + "." + this.name +
            "(" + Arrays.stream(this.descriptor.getArgumentTypes()).map(Type::getClassName).collect(Collectors.joining(", ")) + ")";
    }
}
