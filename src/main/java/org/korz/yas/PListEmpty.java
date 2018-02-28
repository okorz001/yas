package org.korz.yas;

import java.util.NoSuchElementException;

/**
 * The empty persistent singly-linked list.
 * <p>
 * The empty list has no values and is the terminal sub-sequence of every
 * sequence, including itself. The empty sequence is used instead of
 * {@code null} to reduce {@link NullPointerException}s.
 * <p>
 * There is only a single empty list instance. It is safe to reuse the
 * instance regardless of the type parameter since it does not actually
 * contain any values.
 * @param <T> Unused.
 */
public final class PListEmpty<T> extends AbstractPList<T> {
    private static final PListEmpty INSTANCE = new PListEmpty();

    /**
     * Returns the empty list.
     * @return The empty list.
     * @param <T> Unused.
     * @see Seqs#empty
     */
    @SuppressWarnings("unchecked") // T unused
    public static <T> PListEmpty<T> instance() {
        return INSTANCE;
    }

    private PListEmpty() {
        super(0);
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
     * Throws {@link NoSuchElementException}.
     * <p>
     * The empty list has no values. It is a programming error to invoke
     * this method. Correct iteration of a sequence requires checking
     * {@link Seq#empty} before calling {@link Seq#first}.
     * @return never
     * @throws NoSuchElementException always
     */
    @Override // Seq
    public T first() {
        throw new NoSuchElementException("Empty list");
    }

    /**
     * Returns itself, the empty list.
     * @return {@code this}
     */
    @Override // Seq
    public Seq<T> rest() {
        return this;
    }
}
