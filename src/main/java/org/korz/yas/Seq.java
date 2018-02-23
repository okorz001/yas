package org.korz.yas;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An immutable ordered sequence of values.
 * <p>
 * A Seq is similar to an {@link java.util.Iterator} but unlike iterators,
 * sequences are immutable and thus thread-safe. While an iterator mutates
 * internal state to return values, a sequence advances by returning
 * immutable sub-sequences.
 * <p>
 * Since a sequence is immutable, there is no facility for removing values.
 * <p>
 * A basic example of consuming a Seq:
 * <pre><code>
 * Seq&lt;T&gt; seq;
 * while (!seq.empty()) {
 *     System.out.println(seq.first());
 *     seq = seq.rest();
 * }
 * </code></pre>
 * Seq is a primitive abstraction with a minimal API. More interesting and
 * commonly used functions can be found in {@link Seqs}.
 * @param <T> The type of values in the sequence.
 * @see Seqs
 */
public interface Seq<T> {
    /**
     * Is this the empty sequence?
     * <p>
     * If this method returns true, it is guaranteed that {@link Seq#first}
     * will throw. Conversely, if this method returns false, it is guaranteed
     * that {@link Seq#first} will return a value.
     * @return {@code true} if this sequence has no values.
     */
    boolean empty();

    /**
     * Returns the first value in the sequence.
     * @return The first value.
     * @throws java.util.NoSuchElementException If this sequence is empty.
     * @see Seq#empty
     */
    T first();

    /**
     * Returns the remaining values as a sub-sequence.
     * @return The remaining values.
     */
    Seq<T> rest();

    /**
     * Applies a function to this sequence.
     * <p>
     * This is a convenience function to help make large compositions of
     * functions more readable.
     * <p>
     * This method is equivalent to: <code>f.apply(this)</code>
     * @param f The function to apply.
     * @param <TOut> The result type of the function.
     * @return The result of the function.
     */
    default <TOut> TOut apply(Function<Seq<T>, TOut> f) {
        return f.apply(this);
    }

    /**
     * Applies a terminal function (returning void) to this sequence.
     * <p>
     * This is a convenience function to help make large compositions of
     * functions more readable.
     * <p>
     * This method is equivalent to: <code>f.accept(this)</code>
     * @param f The function to apply.
     * @see Seq#apply(Function)
     */
    default void apply(Consumer<Seq<T>> f) {
        f.accept(this);
    }

    /**
     * Applies a predicate to this sequence.
     * <p>
     * This is a convenience function to help make large compositions of
     * functions more readable.
     * <p>
     * This method is equivalent to: <code>f.test(this)</code>
     * @param f The predicate to apply.
     * @return The result of the predicate.
     * @see Seq#apply(Function)
     */
    default boolean apply(Predicate<Seq<T>> f) {
        return f.test(this);
    }

    /**
     * Returns the string representation of this sequence.
     * <p>
     * @return The string representation of this sequence.
     */
    @Override // Object
    String toString();

    /**
     * Compares an object for equality.
     * <p>
     * An object is equal to this sequence if all of the following conditions
     * are met:
     * <ul>
     * <li>The object is a {@link Seq}.</li>
     * <li>Both sequences are empty, or the {@link Seq#first} of each sequence
     * are equal.</li>
     * <li>The {@link Seq#rest} of each sequence are (recursively) equal.</li>
     * </ul>
     * @param o The object to compare for equality.
     * @return {@code true} if the object is equal to this {@link Seq}.
     */
    @Override // Object
    boolean equals(Object o);

    /**
     * Computes the hash code of this sequence.
     * <p>
     * <b>Important:</b> All hash codes should be computed by
     * {@link Seqs#hashCode} to guarantee that all equal {@link Seq} instances
     * have equal hash codes, regardless of implementation. The exact
     * implementation is not defined and may change in future releases.
     * @return The hash code for this sequence.
     */
    @Override // Object
    int hashCode();
}
