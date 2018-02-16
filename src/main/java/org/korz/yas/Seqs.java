package org.korz.yas;

import java.util.Optional;
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
     * The remaining sub-sequence is guaranteed to be generated no more than
     * once. The generating function will not be called until the first
     * invocation of {@link Seq#rest}. The result of the function is cached
     * and future invocations of {@link Seq#rest} will return the cached
     * result. If {@link Seq#rest} is never called, then the function will
     * never be called, allowing the creation of <i>infinite sequences</i>.
     * <p>
     * <b>Important</b>: The generating function must not depend on any external
     * state whatsoever. The return value must be deterministic since the
     * execution of the function is delayed indefinitely.
     * @param value The first value of the new sequence.
     * @param seqFn A function to generate the remaining values of the new
     *              sequence.
     * @param <T> The type of values in the sequence.
     * @return A new sequence.
     */
    public static <T> Seq<T> lazy(T value, Supplier<Seq<? extends T>> seqFn) {
        return new Lazy<>(value, seqFn);
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

    // Seq primitive operations

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
        if (seq.empty())
            return empty();
        return lazy(mapFn.apply(seq.first()), () -> map(mapFn, seq.rest()));
    }

    /**
     * Creates a new sequence by dropping all elements from another sequence
     * that do not satisfy a predicate.
     * <p>
     * To find any value that satisfies a predicate, use {@link Seqs#find}.
     * @param predFn The predicate function.
     * @param seq The origin sequence.
     * @param <T> The type of values in the sequence.
     * @return A new sequence.
     * @see Seqs#find
     */
    public static <T> Seq<T> filter(Predicate<? super T> predFn, Seq<T> seq) {
        if (seq.empty())
            return empty();
        T first = seq.first();
        if (predFn.test(first))
            return lazy(first, () -> filter(predFn, seq.rest()));
        return filter(predFn, seq.rest());
    }

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

    /**
     * Finds the first value in a sequence that satisfies a predicate.
     * <p>
     * To find all values that satisfy a predicate, use {@link Seqs#filter}.
     * @param predFn The predicate function.
     * @param seq The sequence.
     * @param <T> The type of values in the sequence.
     * @return An {@link Optional} with the first value found, or an empty
     *         {@link Optional} if is found.
     * @see Seqs#filter
     */
    public static <T> Optional<T> find(Predicate<? super T> predFn, Seq<T> seq) {
        // foldLeft with short circuit
        while (!seq.empty()) {
            T first = seq.first();
            if (predFn.test(first))
                return Optional.of(first);
            seq = seq.rest();
        }
        return Optional.empty();
    }

    // Seq higher-level operations

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
     * @param <T> The type of values in the sequence.
     * @return The new sequence.
     */
    public static <T> Seq<T> reverse(Seq<T> seq) {
        return foldLeft((rest, first) -> cons(first, rest), empty(), seq);
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
}
