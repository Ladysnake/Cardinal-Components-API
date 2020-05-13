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
package nerdhub.cardinal.components.internal;

import nerdhub.cardinal.components.api.component.ComponentContainer;
import nerdhub.cardinal.components.internal.asm.AnnotationData;
import nerdhub.cardinal.components.internal.asm.MethodData;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

@ApiStatus.OverrideOnly
public interface StaticComponentPlugin {
    /**
     * A pattern that should be used to check that component type ids are valid
     */
    Pattern IDENTIFIER_PATTERN = Pattern.compile("([a-z0-9_.-]+:)?[a-z0-9/._-]+");

    /**
     * @return the annotation processed by this plugin
     */
    Class<? extends Annotation> getAnnotationType();

    /**
     * Scans an annotated component factory method.
     *
     * <p>Implementations should store processed data so that it can be used in {@link #generate()}.
     * <strong>Classes should not be generated in this method!</strong>
     * @param factory descriptor of the annotated method
     * @param annotation ASM data about the annotation being processed
     * @return a valid identifier string for a recognized component type
     */
    String scan(MethodData factory, AnnotationData annotation) throws IOException;

    /**
     * Generates classes dynamically based on previously scanned information.
     *
     * <p>Implementations of this method commonly generate one or more {@link ComponentContainer} implementations,
     * as well as matching {@link FeedbackContainerFactory factories}.
     */
    void generate() throws IOException;
}
