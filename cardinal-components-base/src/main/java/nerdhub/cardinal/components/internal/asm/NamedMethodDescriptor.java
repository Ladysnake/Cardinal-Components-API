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

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class NamedMethodDescriptor {
    public final Type ownerType;
    public final String name;
    private final int access;
    public final Type descriptor;
    public final Type[] args;

    public NamedMethodDescriptor(Type ownerType, String name, int access, Type descriptor) {
        this.ownerType = ownerType;
        this.name = name;
        this.access = access;
        this.descriptor = descriptor;
        this.args = descriptor.getArgumentTypes();
    }

    @Override
    public String toString() {
        return Modifier.toString(access) + " " + descriptor.getReturnType().getClassName() + " " + ownerType.getClassName() + "." + name + "(" + Arrays.stream(args).map(Type::getClassName).collect(Collectors.joining(", ")) + ")";
    }
}
