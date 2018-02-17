package org.korz.yas;

/**
 * An infinite sequence of a single value.
 * @param <T> The type of the value.
 */
public final class Repeat<T> extends AbstractSeq<T> {
    private final T first;

    /**
     * Creates an infinite sequence of a value.
     * @param first The value.
     */
    public Repeat(T first) {
        this.first = first;
    }

    @Override // Seq
    public T first() {
        return first;
    }

    /**
     * Returns itself, causing an infinite sequence.
     * @return {@code this}
     */
    @Override // Seq
    public Seq<T> rest() {
        return this;
    }
}
