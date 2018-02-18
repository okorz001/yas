package org.korz.yas;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Static methods for manipulating or creating {@link Seq} instances.
 */
public abstract class Seqs {
    private Seqs() {}

    // Factory methods

    /**
     * Returns the empty sequence.
     * <p>
     * The empty sequence is a singleton and is shared between all sequences.
     * Since the empty sequence has no values, it is safe to share between
     * all sequences of any type.
     * @param <T> Unused.
     * @return The empty sequence.
     */
    public static <T> Seq<T> empty() {
        return Empty.instance();
    }

    /**
     * Creates a new sequence by prepending a value to an existing sequence.
     * @param value The first value of the new sequence.
     * @param seq The remaining values of the new sequence.
     * @param <T> The type of values in the new sequence.
     * @return A new sequence.
     */
    public static <T> Seq<T> cons(T value, Seq<? extends T> seq) {
        return new Cons<>(value, seq);
    }

    /**
     * Creates a lazy sequence that is generated on-demand.
     * <p>
     * The sequence is guaranteed to be generated no more than once. The
     * generating function will not be called until the first invocation of
     * a {@link Seq} method. The result of the function is cached
     * and future invocations of {@link Seq} methods will return the cached
     * result. If no {@link Seq} methods are never called, then the function
     * will never be called, allowing the creation of <i>infinite sequences</i>.
     * <p>
     * <b>Important</b>: The generating function must not depend on any external
     * state whatsoever. The return value must be deterministic since the
     * execution of the function is delayed indefinitely.
     * @param supplier The function to generate the sequence.
     * @param <T> The type of values in the sequence.
     * @return A new sequence.
     */
    public static <T> Seq<T> lazy(Supplier<Seq<? extends T>> supplier) {
        return new Lazy<>(supplier);
    }

    /**
     * Creates an infinite sequence of a single value.
     * @param val The value.
     * @param <T> The type of the value.
     * @return The sequence.
     */
    public static <T> Seq<T> repeat(T val) {
        return new Repeat<>(val);
    }

    // AbstractSeq helpers

    /**
     * Implements {@link Object#toString} for a {@link Seq}.
     * @param seq A sequence.
     * @return The string representation of the sequence.
     * @see Seq#toString
     */
    public static String toString(Seq<?> seq) {
        StringBuilder str = new StringBuilder("(");
        if (!seq.empty()) {
            str.append(seq.first());
            // foldLeft is more "correct"
            forEach(it -> str.append(", ").append(it), seq.rest());
        }
        str.append(")");
        return str.toString();
    }

    /**
     * Implements {@link Object#equals} for a {@link Seq}.
     * @param seq A sequence.
     * @param other Another sequence.
     * @return {@code true} if the sequences contains equal elements in the
     *         same order.
     * @see Seq#equals
     */
    public static boolean equals(Seq<?> seq, Seq<?> other) {
        if (seq.empty() && other.empty())
            return true;
        return seq.empty() == other.empty() &&
                seq.first().equals(other.first()) &&
                seq.rest().equals(other.rest());
    }

    /**
     * Implements {@link Object#hashCode} for a {@link Seq}.
     * <p>
     * It is guaranteed that two equal {@link Seq} instances will have equal
     * hash codes.
     * @param seq A sequence.
     * @return The hash code of the sequence.
     * @see Seq#hashCode
     * @see Seq#equals
     */
    public static int hashCode(Seq<?> seq) {
        // djb2
        return foldLeft((hash, first) -> hash * 33 + first.hashCode(), 5381, seq);
    }

    /**
     * Returns the same sequence, but with an upcast type parameter.
     * <p>
     * <b>tl;dr:</b> Safely ignores a compiler warning because {@link Seq} is
     * immutable. This function has zero runtime behavior due to type erasure.
     * <p>
     * Although we know the following code sample to be valid and safe, the
     * compiler will reject it:
     * <pre><code>
     * public static &lt;T&gt; void validate(Predicate&lt;T&gt; pred, Iterable&lt;T&gt; vals) {
     *     for (T val : vals)
     *         if (!pred.test(val))
     *             throw new RuntimeError();
     * }
     *
     * public static void main(String[] args) {
     *     Predicate&lt;Object&gt; isNonNull = Objects::nonNull;
     *     List&lt;String&gt; argsList = Arrays.asList(args);
     *     validate(isNonNull, argList);
     * }
     * </code></pre>
     * This code is rejected because T is resolved to both Object and String
     * but it required to strictly bind to a single type:
     * <pre><code>
     * error: method validate in class Main cannot be applied to given types;
     *     validate(isNonNull, argsList);
     *     ^
     * required: Predicate&lt;T&gt;,Iterable&lt;T&gt;
     * found: Predicate&lt;Object&gt;,List&lt;String&gt;
     * </code></pre>
     * The code sample can be fixed by using a wildcard type parameter:
     * <pre><code>
     * validate(Predicate&lt;T&gt; pred, Iterable&lt;? extends T&gt; vals)
     * </code></pre>
     * This causes T to bind to Object. This is permitted because String is a
     * subclass of Object, satisfying the wildcard parameter.
     * <p>
     * However, wildcards introduce another problem:
     * <pre><code>
     * List&lt;Object&gt; base = null;
     * List&lt;? extends Object&gt; derived = null;
     * base = derived;
     * </code></pre>
     * This code is rejected because there is no subclass/superclass
     * relationship between instances with different type parameters.
     * <pre><code>
     * error: incompatible types: List&lt;CAP#1&gt; cannot be converted to List&lt;Object&gt;
     *     base = derived;
     *     ^
     * where CAP#1 is a fresh type-variable:
     * CAP#1 extends Object from capture of ? extends Object
     * </code></pre>
     * This is actually a good thing. Consider if derived is a List of String.
     * It is legal to insert any Object into base, but doing so would
     * eventually cause a {@link ClassCastException} when using derived.
     * <p>
     * Since we know that a {@link Seq} is immutable, it is impossible to
     * insert a base-type object into derived-type sequence. Thus, we can safely
     * ignore compiler warnings about upcasting the type parameter. This
     * function has zero runtime behavior due to type erasure.
     * @param seq The sequence.
     * @param <T> The desired type parameter.
     * @return The same sequence, but with a new type parameter.
     * @see <a href="https://docs.oracle.com/javase/tutorial/java/generics/inheritance.html">
     *     Learning the Java Language: Generics: Generics, Inheritance, and Subtypes</a>
     * @see <a href="https://docs.oracle.com/javase/tutorial/java/generics/upperBounded.html">
     *     Learning the Java Language: Generics: Upper Bounded Wildcards</a>
     * @see <a href="https://docs.oracle.com/javase/tutorial/java/generics/subtyping.html">
     *     Learning the Java Language: Generics: Wildcards and Subtyping</a>
     */
    @SuppressWarnings("unchecked") // cannot insert T into seq
    public static <T> Seq<T> upcast(Seq<? extends T> seq) {
        return (Seq<T>) seq;
    }

    // map-derived operations

    /**
     * Creates a new sequence by transforming all elements in another sequence.
     * <p>
     * The returned sequence is <i>lazy</i>. If the function is intended to
     * cause side-effects, use {@link Seqs#forEach} instead to force evaluation.
     * @param mapFn The mapping function.
     * @param seq The origin sequence.
     * @param <TIn> The type of values in the origin sequence.
     * @param <TOut> The type of values in the output sequence.
     * @return A new sequence.
     * @see Seqs#forEach
     */
    public static <TIn, TOut> Seq<TOut> map(Function<? super TIn, ? extends TOut> mapFn,
                                            Seq<TIn> seq) {
        return lazy(() -> {
            if (seq.empty())
                return empty();
            return cons(mapFn.apply(seq.first()), map(mapFn, seq.rest()));
        });
    }

    // filter-derived operations

    /**
     * Creates a new sequence by dropping all elements from another sequence
     * that do not satisfy a predicate.
     * <p>
     * The returned sequence is <i>lazy</i>.
     * <p>
     * To find any value that satisfies a predicate, use {@link Seqs#find}.
     * @param predFn The predicate function.
     * @param seq The origin sequence.
     * @param <T> The type of values in the new sequence.
     * @return A new sequence.
     * @see Seqs#find
     */
    public static <T> Seq<T> filter(Predicate<? super T> predFn,
                                    Seq<? extends T> seq) {
        return lazy(() -> {
            if (seq.empty())
                return empty();
            T first = seq.first();
            if (predFn.test(first))
                return cons(first, filter(predFn, seq.rest()));
            return filter(predFn, seq.rest());
        });
    }

    /**
     * Creates a new sequence by removing duplicates from another sequence.
     * <p>
     * The returned sequence is <i>lazy</i>.
     * @param seq The sequence.
     * @param <T> The type of values in the new sequence.
     * @return The new sequence.
     */
    public static <T> Seq<T> distinct(Seq<? extends T> seq) {
        Set<T> visited = new HashSet<>();
        return filter(visited::add, seq);
    }

    // foldLeft-derived operations

    /**
     * Combines a sequence into a result via a left-associative function.
     * <p>
     * This function is iterative and thus can handle arbitrarily large
     * sequences.
     * <p>
     * If there is no initial value or sensible default for empty sequences,
     * consider using {@link Seqs#reduce} instead.
     * @param reduceFn The combining function.
     * @param initial The initial value.
     * @param seq The sequence.
     * @param <TIn> The type of the sequence.
     * @param <TOut> The type of the result.
     * @return The combined result.
     * @see Seqs#reduce
     */
    public static <TIn, TOut> TOut foldLeft(BiFunction<? super TOut, ? super TIn, ? extends TOut> reduceFn,
                                            TOut initial,
                                            Seq<TIn> seq) {
        TOut result = initial;
        while (!seq.empty()) {
            result = reduceFn.apply(result, seq.first());
            seq = seq.rest();
        }
        return result;
    }

    /**
     * Reduces a sequence into a single value via a left-associative function.
     * <p>
     * It is a programming error to call this function with an empty sequence.
     * If the sequence may be empty, consider using {@link Seqs#foldLeft} to
     * provide an initial value (and thus result).
     * @param reduceFn The reducing function.
     * @param seq The sequence.
     * @param <T> The type of values in the sequence.
     * @return The reduced value.
     * @throws java.util.NoSuchElementException If the sequence is empty.
     * @see Seqs#foldLeft
     */
    public static <T> T reduce(BiFunction<? super T, ? super T, ? extends T> reduceFn,
                               Seq<T> seq) {
        return foldLeft(reduceFn, seq.first(), seq.rest());
    }

    /**
     * Calls a function once for every element in the sequence for side-effects.
     * @param fn The function.
     * @param seq The sequence.
     * @param <T> The type of values in the sequence.
     */
    public static <T> void forEach(Consumer<? super T> fn, Seq<T> seq) {
        // map is lazy and creates more temporaries than foldLeft
        foldLeft((unused, first) -> {
            fn.accept(first);
            return unused;
        }, new Object(), seq);
    }

    /**
     * Creates a new sequence that has the same values but in reversed order.
     * @param seq The origin sequence.
     * @param <T> The type of values in the new sequence.
     * @return The new sequence.
     */
    public static <T> Seq<T> reverse(Seq<? extends T> seq) {
        return foldLeft((rest, first) -> cons(first, rest), empty(), seq);
    }

    // foldRight-derived operations

    /**
     * Combines a sequence into a result via a right-associative function.
     * <p>
     * <b>Important:</b> This function is recursive and thus a large sequence
     * may cause a {@link StackOverflowError} to be thrown. If the combining
     * function is an associative operator, for example {@link Integer#sum},
     * then considering using {@link Seqs#foldLeft} instead.
     * @param reduceFn The combining function.
     * @param initial The initial value.
     * @param seq The sequence.
     * @param <TIn> The type of the sequence.
     * @param <TOut> The type of the result.
     * @return The combined result.
     * @see Seqs#foldLeft
     */
    public static <TIn, TOut> TOut foldRight(BiFunction<? super TIn, ? super TOut, ? extends TOut> reduceFn,
                                             Seq<TIn> seq,
                                             TOut initial) {
        if (seq.empty())
            return initial;
        TOut result = foldRight(reduceFn, seq.rest(), initial);
        return reduceFn.apply(seq.first(), result);
    }

    // find-derived operations

    /**
     * Finds the first value in a sequence that satisfies a predicate.
     * <p>
     * To find all values that satisfy a predicate, use {@link Seqs#filter}.
     * @param predFn The predicate function.
     * @param seq The sequence.
     * @param <T> The type of the value found.
     * @return An {@link Optional} with the first value found, or an empty
     *         {@link Optional} if is found.
     * @see Seqs#filter
     */
    public static <T> Optional<T> find(Predicate<? super T> predFn,
                                       Seq<? extends T> seq) {
        // foldLeft with short circuit
        while (!seq.empty()) {
            T first = seq.first();
            if (predFn.test(first))
                return Optional.of(first);
            seq = seq.rest();
        }
        return Optional.empty();
    }

    /**
     * Checks if any value in a sequence satisfies a predicate.
     * <p>
     * To find any value that satisfies a predicate, use {@link Seqs#find}.
     * <p>
     * To check if all values satisfy a predicate, use {@link Seqs#all}.
     * @param predFn The predicate.
     * @param seq The sequence.
     * @param <T> The type of values in the sequence.
     * @return {@code true} if any value satifies the predicate.
     * @see Seqs#find
     * @see Seqs#all
     */
    public static <T> boolean any(Predicate<? super T> predFn, Seq<T> seq) {
        return find(predFn, seq).isPresent();
    }

    /**
     * Checks if all values in a sequence satisfy a predicate.
     * <p>
     * To check if any value satisfies a predicate, use {@link Seqs#any}.
     * <p>
     * To find all values that satisfy a predicate, use {@link Seqs#filter}.
     * @param predFn The predicate.
     * @param seq The sequence.
     * @param <T> The type of values in the sequence.
     * @return {@code true} if all value satify the predicate.
     * @see Seqs#any
     * @see Seqs#filter
     */
    public static <T> boolean all(Predicate<? super T> predFn, Seq<T> seq) {
        // find value that does not satisfy
        return !find(predFn.negate(), seq).isPresent();
    }

    // min-derived operations

    /**
     * Finds the smallest value in a sequence.
     * <p>
     * If multiple values are considered equally the smallest, the first such
     * value will be returned.
     * <p>
     * It is a programming error to call this function with an empty sequence.
     * @param compFn The comparing function.
     * @param seq The sequence.
     * @param <T> The type of values in the sequence.
     * @return The smallest value.
     * @throws java.util.NoSuchElementException If the sequence is empty.
     */
    public static <T> T min(Comparator<? super T> compFn, Seq<T> seq) {
        T result = seq.first();
        for (seq = seq.rest(); !seq.empty(); seq = seq.rest()) {
            T first = seq.first();
            if (compFn.compare(first, result) < 0) {
                result = first;
            }
        }
        return result;
    }

    /**
     * Finds the largest value in a sequence.
     * <p>
     * If multiple values are considered equally the largest, the first such
     * value will be returned.
     * <p>
     * It is a programming error to call this function with an empty sequence.
     * @param compFn The comparing function.
     * @param seq The sequence.
     * @param <T> The type of values in the sequence.
     * @return The largest value.
     * @throws java.util.NoSuchElementException If the sequence is empty.
     */
    public static <T> T max(Comparator<? super T> compFn, Seq<T> seq) {
        return min(compFn.reversed(), seq);
    }

    // takeWhile-derived operations

    /**
     * Creates a sequence of all values in another sequence until the predicate
     * fails once.
     * <p>
     * The returned sequence is <i>lazy</i>.
     * @param predFn The predicate.
     * @param seq The origin sequence.
     * @param <T> The type of values in the new sequence.
     * @return The new sequence.
     */
    public static <T> Seq<T> takeWhile(Predicate<? super T> predFn,
                                       Seq<? extends T> seq) {
        return lazy(() -> {
            if (seq.empty())
                return empty();
            T first = seq.first();
            if (predFn.test(first))
                return cons(first, takeWhile(predFn, seq.rest()));
            return empty();
        });
    }

    /**
     * Creates a sequence with the first N values of another sequence.
     * <p>
     * The returned sequence is <i>lazy</i>.
     * @param n The number of values to include.
     * @param seq The origin sequence.
     * @param <T> The type of values in the new sequence.
     * @return The new sequence.
     * @throws IllegalArgumentException If n is negative.
     */
    public static <T> Seq<T> take(int n, Seq<? extends T> seq) {
        if (n < 0)
            throw new IllegalArgumentException("n < 0");
        AtomicInteger count = new AtomicInteger(n);
        return takeWhile(unused -> count.getAndDecrement() > 0, seq);
    }

    // dropWhile-derived operations

    /**
     * Creates a sequence of all values in another sequence after the predicate
     * fails once.
     * @param predFn The predicate.
     * @param seq The origin sequence.
     * @param <T> The type of values in the new sequence.
     * @return The new sequence.
     */
    public static <T> Seq<T> dropWhile(Predicate<? super T> predFn,
                                       Seq<? extends T> seq) {
        while (!seq.empty() && predFn.test(seq.first()))
            seq = seq.rest();
        return upcast(seq);
    }

    /**
     * Creates a sequence without the first N values of another sequence.
     * @param n The number of values to exclude.
     * @param seq The origin sequence.
     * @param <T> The type of values in the new sequence.
     * @return The new sequence.
     * @throws IllegalArgumentException If n is negative.
     */
    public static <T> Seq<T> drop(int n, Seq<? extends T> seq) {
        if (n < 0)
            throw new IllegalArgumentException("n < 0");
        AtomicInteger count = new AtomicInteger(n);
        return dropWhile(unused -> count.getAndDecrement() > 0, seq);
    }

    /**
     * Returns the Nth value in a sequence, using zero-based indexing.
     * @param n The index of the value.
     * @param seq The sequence.
     * @param <T> The type of the Nth value.
     * @return An {@link Optional} with the Nth value, or an empty
     *         {@link Optional} if the sequence has less than N values.
     */
    public static <T> Optional<T> nth(int n, Seq<? extends T> seq) {
        seq = drop(n, seq);
        return seq.empty() ? Optional.empty() : Optional.of(seq.first());
    }

    // concat-derived operations

    /**
     * Creates a new sequence by concatenating two sequences.
     * <p>
     * The returned sequence is <i>lazy</i>.
     * @param first The first origin sequence.
     * @param second The second origin sequence.
     * @param <T> The type of values in the output sequence.
     * @return The new sequence.
     */
    public static <T> Seq<T> concat(Seq<? extends T> first,
                                    Seq<? extends T> second) {
        return lazy(() -> {
            if (first.empty())
                return upcast(second);
            return cons(first.first(), concat(first.rest(), second));
        });
    }

    // iterate-derived operations

    /**
     * Creates an infinite sequence by iterating over a function.
     * <p>
     * In mathematics, iterating a function refers to applying the a function
     * repeatedly using the previous output as the next input. The output
     * sequence has the form: <code>(x, f(x), f(f(x)), ...)</code>
     * @param fn The function to generate values.
     * @param initial The first value of the sequence.
     * @param <T> The type of values in the sequence.
     * @return A new sequence.
     */
    public static <T> Seq<T> iterate(Function<? super T, ? extends T> fn,
                                     T initial) {
        return lazy(() -> cons(initial, iterate(fn, fn.apply(initial))));
    }

    /**
     * Creates a sequence of integers increasing by one, starting at zero.
     * <p>
     * The returned sequence is <i>lazy</i>.
     * <p>
     * This function is equivalent to <code>range(0, end)</code>.
     * @param end The ending bound, exclusive.
     * @return A new sequence.
     */
    public static Seq<Integer> range(int end) {
        return range(0, end, 1);
    }

    /**
     * Creates a sequence of integers increasing by one, between two numbers.
     * <p>
     * The returned sequence is <i>lazy</i>.
     * <p>
     * This function is equivalent to <code>range(start, end, 1)</code>.
     * @param start The first value of the sequence.
     * @param end The ending bound, exclusive.
     * @return A new sequence.
     */
    public static Seq<Integer> range(int start, int end) {
        return range(start, end, 1);
    }

    /**
     * Creates a sequence of integers increasing by a constant value, between
     * two numbers.
     * <p>
     * The returned sequence is <i>lazy</i>.
     * @param start The first value of the sequence.
     * @param end The ending bound, exclusive.
     * @param step The increment between values in the sequence.
     * @return A new sequence.
     */
    public static Seq<Integer> range(int start, int end, int step) {
        return takeWhile(x -> x < end, iterate(x -> x + step, start));
    }
}
