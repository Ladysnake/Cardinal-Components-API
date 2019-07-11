/*
 * Cardinal-Components-API
 * Copyright (C) 2019 GlassPane
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
package nerdhub.cardinal.components.api.util;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a function that extracts a property from a given object.
 *
 * <p> Such functions may be chained to represent the traversal of a full object path.
 * Object paths formed in this way will short-circuit at any point
 * a <code>null</code> reference is encountered.
 *
 * <p> Object paths are similar to {@link Optional Optionals} in that they offer
 * the ability to turn a sequence of operations into a succinct pipeline
 * that abstracts away <code>null</code> handling.
 * However, while an {@code Optional} is an immutable container object wrapping the
 * actual value, an {@code ObjectPath} only describes the computational
 * operations that will be performed on a source object.
 * Because object paths do not need to create new container objects at each step of
 * the computation, they are likely (though not guaranteed) to be faster than their
 * {@code Optional} counterparts. For further performance gains as well as code
 * compacting, it is recommended to cache and reuse {@code ObjectPath} instances.
 *
 * <p> As {@code ObjectPath} should be reusable on multiple sources, even concurrently,
 * its operations should be stateless.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @author Pyrofab
 */
@FunctionalInterface
public interface ObjectPath<T, R> extends Function<T, R> {

    /**
     * Applies this path function to the given argument.
     * Any failure to find the object property located by this function should result
     * in a <code>null</code> return value.
     *
     * @param t a source object
     * @return the function result
     */
    @Override
    @Nullable R apply(T t);

    /**
     * Applies this function to the given argument. If a value is present at the end
     * of this object path, returns the value, otherwise throws {@link NoSuchElementException}.
     *
     * @param t a source object
     * @return the non-null value obtained by {@link #apply(Object) applying} this function to the source
     * @throws NoSuchElementException if the object cannot be mapped to a value using this function
     */
    default R get(T t) {
        R r = this.apply(t);
        if (r == null) {
            throw new NoSuchElementException();
        }
        return r;
    }

    /**
     * Convert this {@code ObjectPath} to an {@code Optional} describing the result
     * of {@link #apply(Object)} on the given argument. If this function does not
     * produce a value from the given argument, the returned {@code Optional} will
     * be empty.
     *
     * @param value the possibly-null value to extract a property from
     * @return an {@code Optional} with a present value if the specified value
     * and the result of this function is non-null, otherwise an empty {@code Optional}
     */
    default Optional<R> toOptional(@Nullable T value) {
        return Optional.ofNullable(value).map(this);
    }

    /**
     * Returns a composed function that first applies the {@code before}
     * function to its input, and then applies this function to the result.
     * If <code>before</code> returns <code>null</code>, this function will
     * not be applied and the resulting function will return null instead.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V> the type of input to the {@code before} function, and to the
     *           composed function
     * @param before the function to apply before this function is applied
     * @return a composed function that first applies the {@code before}
     * function and then applies this function if the result is not null
     * @throws NullPointerException if before is null
     *
     * @see #andThen(Function)
     */
    @Override
    default <V> ObjectPath<V, R> compose(Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (obj) -> {
            T t = before.apply(obj);
            return t != null ? this.apply(t) : null;
        };
    }

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If the application of <code>this</code> returns <code>null</code>,
     * <code>after</code> will not be applied and the resulting function will
     * return <code>null</code> instead.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V>   the type of output of the {@code after} function, and of the
     *              composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     * @see #compose(Function)
     */
    @Override
    default <V> ObjectPath<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (s) -> {
            R r = this.apply(s);
            return r != null ? after.apply(r) : null;
        };
    }

    /**
     * Returns a composed function that first applies this function to
     * its input, and then calls {@code other} if there is no result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param other a {@code Supplier} whose result is returned if no value
     * is present
     * @return a composed function that first applies this function and then
     * returns the result or calls {@code other} if none is given
     * @throws NullPointerException if other is null
     * @see Optional#orElseGet(Supplier)
     */
    default ObjectPath<T, R> orElseGet(Supplier<R> other) {
        Objects.requireNonNull(other);
        return (s) -> {
            R r = this.apply(s);
            return r != null ? r : other.get();
        };
    }

    /**
     * Returns a composed {@code Consumer} that first applies this function to
     * its input, and then performs the {@code after} operation.
     * If the application of <code>this</code> returns <code>null</code>,
     * <code>after</code> will not be applied and the resulting function will
     * return <code>null</code> instead.
     * If performing either operation throws an exception, it is relayed to the caller of the
     * composed operation.  If applying this function throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform on the result of
     * @return a composed {@code Consumer} that first applies this function
     * and then performs the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default Consumer<T> andThenDo(Consumer<? super R> after) {
        Objects.requireNonNull(after);
        return (T t) -> {
            R r = this.apply(t);
            if (r != null) {
                after.accept(r);
            }
        };
    }

    /**
     * Returns a composed {@code Predicate} that first applies this function to
     * its input, and then evaluates the {@code after} predicate.
     * If the application of <code>this</code> returns <code>null</code>,
     * <code>after</code> will not be evaluated and the resulting predicate will
     * return <code>false</code> instead.
     * If performing either operation throws an exception, it is relayed to the caller of the
     * composed predicate.  If applying this function throws an exception,
     * the {@code after} predicate will not be evaluated.
     *
     * @param after the operation to perform on the result of
     * @return a composed {@code Predicate} that first applies this
     * function and then evaluates the {@code after} predicate
     * @throws NullPointerException if {@code after} is null
     */
    default Predicate<T> andThenTest(Predicate<? super R> after) {
        Objects.requireNonNull(after);
        return (T t) -> {
            R r = this.apply(t);
            return r != null && after.test(r);
        };
    }

    /**
     * Returns a composed function that first applies this function to
     * its input, and then casts the result to the class or interface
     * represented by the given {@code Class} object.
     * If the application of <code>this</code> returns a value that is
     * not an instance of the given class, the resulting function will
     * return <code>null</code> instead.
     * If evaluation of this function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param clazz the class of the type to cast this function's results to
     * @param <V>   the type to cast this function's results to
     * @return a composed function that first applies this function and then
     * returns the result after casting, or null if the result cannot be casted.
     */
    @SuppressWarnings("unchecked")
    default <V> ObjectPath<T, V> thenCastTo(Class<V> clazz) {
        Objects.requireNonNull(clazz);
        return (obj) -> {
            R r = this.apply(obj);
            return clazz.isInstance(r) ? (V) r : null;
        };
    }
}
