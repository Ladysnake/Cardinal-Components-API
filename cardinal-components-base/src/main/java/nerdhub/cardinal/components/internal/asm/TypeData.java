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
