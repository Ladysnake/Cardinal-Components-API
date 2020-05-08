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

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nonnull;
import java.util.Locale;

public final class CcaAsmConstants {
    public static final int ASM_VERSION = Opcodes.ASM6;
    // existing references
    public static final String COMPONENT = "nerdhub/cardinal/components/api/component/Component";
    public static final String COMPONENT_CONTAINER = "nerdhub/cardinal/components/api/component/ComponentContainer";
    public static final String COMPONENT_TYPE = "nerdhub/cardinal/components/api/ComponentType";
    public static final String COMPONENT_PROVIDER = "nerdhub/cardinal/components/api/component/ComponentProvider";
    public static final String CONTAINER_FACTORY_IMPL = "nerdhub/cardinal/components/internal/FeedbackContainerFactory";
    public static final String DYNAMIC_COMPONENT_CONTAINER_IMPL = "nerdhub/cardinal/components/api/util/container/FastComponentContainer";
    public static final String DYNAMIC_COMPONENT_CONTAINER_PUT_DESC = "(Lnerdhub/cardinal/components/api/ComponentType;Lnerdhub/cardinal/components/api/component/Component;)Lnerdhub/cardinal/components/api/component/Component;";
    public static final String LAZY_COMPONENT_TYPE = "nerdhub/cardinal/components/api/util/LazyComponentType";
    public static final String IDENTIFIER = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_2960").replace('.', '/');
    public static final String EVENT = "net/fabricmc/fabric/api/event/Event";

    // generated references
    public static final String STATIC_COMPONENT_CONTAINER = "nerdhub/cardinal/components/_generated_/GeneratedComponentContainer";
    public static final String STATIC_CONTAINER_GETTER_DESC = "()Lnerdhub/cardinal/components/api/component/Component;";
    public static final String STATIC_COMPONENT_TYPE = "nerdhub/cardinal/components/_generated_/ComponentType";
    public static final String STATIC_COMPONENT_TYPES = "nerdhub/cardinal/components/_generated_/StaticComponentTypes";
    public static final String STATIC_CONTAINER_FACTORY = "nerdhub/cardinal/components/_generated_/GeneratedContainerFactory";

    public static String getComponentTypeName(String identifier) {
        return STATIC_COMPONENT_TYPE + "$" + getJavaIdentifierName(identifier);
    }

    @Nonnull
    public static String getJavaIdentifierName(String identifier) {
        return identifier.replace(':', '$');
    }

    @NotNull
    public static String getTypeConstantName(String identifier) {
        return getJavaIdentifierName(identifier).toUpperCase(Locale.ROOT);
    }

    @Nonnull
    public static String getStaticStorageGetterName(String identifier) {
        return "get$" + getJavaIdentifierName(identifier);
    }
}
