package org.korz.yas;

/**
 * A skeletal implementation of the {@link Seq} interface.
 * <p>
 * If the implementation can never be empty, consider subclassing
 * {@link AbstractNonEmptySeq} instead.
 * @param <T> The type of values in the sequence.
 */
public abstract class AbstractSeq<T> implements Seq<T> {
    @Override // Object
    public final String toString() {
        return Seqs.toString(this);
    }

    @Override // Object
    public final boolean equals(Object o) {
        return o instanceof Seq && Seqs.equals(this, (Seq) o);
    }

    @Override // Object
    public final int hashCode() {
        return Seqs.hashCode(this);
    }
}
