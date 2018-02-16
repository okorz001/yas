package org.korz.yas;

/**
 * A cons cell, the trivial sequence implementation.
 * <p>
 * A cons cell is a standalone sequence implementation that is not backed by
 * any other data structure. {@link Seq#first} and {@link Seq#rest} are
 * implemented as simple getter methods.
 * @param <T> The type of values in the sequence.
 */
public final class Cons<T> extends AbstractSeq<T> {
    private final T first;
    private final Seq<? extends T> rest;

    /**
     * Creates a new cons cell.
     * @param first The first value of the sequence.
     * @param rest The remaining values of the sequence.
     * @see Seqs#cons
     */
    public Cons(T first, Seq<? extends T> rest) {
        this.first = first;
        this.rest = rest;
    }

    @Override // Seq
    public T first() {
        return first;
    }

    @Override // Seq
    @SuppressWarnings("unchecked") // cannot insert T into Seq
    public Seq<T> rest() {
        return (Seq<T>) rest;
    }
}
