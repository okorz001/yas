package org.korz.yas;

import java.util.NoSuchElementException;

/**
 * The empty sequence.
 * <p>
 * The empty sequence has no values and is the terminal sub-sequence of every
 * sequence, including itself. The empty sequence is used instead of
 * {@code null} to reduce {@link NullPointerException}s.
 * <p>
 * There is only a single empty sequence instance. It is safe to reuse the
 * instance regardless of the type parameter since it does not actually
 * contain any values.
 * @param <T> Unused.
 */
public final class Empty<T> implements Seq<T> {
    private static final Empty INSTANCE = new Empty();

    /**
     * Returns the empty sequence.
     * @return The empty sequence.
     * @param <T> Unused.
     * @see Seqs#empty
     */
    @SuppressWarnings("unchecked") // T unused
    public static <T> Empty<T> instance() {
        return INSTANCE;
    }

    private Empty() {}

    /**
     * Throws {@link NoSuchElementException}.
     * <p>
     * The empty sequence has no values. It is a programming error to invoke
     * this method. Correct iteration of a sequence requires checking
     * {@link Seq#empty} before calling {@link Seq#first}.
     * @return never
     * @throws NoSuchElementException always
     */
    @Override // Seq
    public T first() {
        throw new NoSuchElementException("Empty sequence");
    }

    /**
     * Returns itself, the empty sequence.
     * @return {@code this}
     */
    @Override // Seq
    public Seq<T> rest() {
        return this;
    }

    /**
     * Returns true.
     * @return {@code true}
     */
    @Override // Seq
    public boolean empty() {
        return true;
    }

    /**
     * Returns the string representation of the empty sequence
     * @return {@code "()"}
     */
    @Override // Object
    public String toString() {
        return "()";
    }

    /**
     * Checks if an object is this object.
     * <p>
     * There is only one empty sequence object, thus it is only equal to
     * itself.
     * @param o The object to compare for equality.
     * @return {@code this == o}
     */
    @Override // Object
    public boolean equals(Object o) {
        // exactly one instance
        return this == o;
    }

    @Override // Object
    public int hashCode() {
        // exactly one instance
        return 5381;
    }
}
