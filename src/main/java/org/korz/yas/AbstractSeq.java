package org.korz.yas;

// this description is copied from JDK abstract collections
/**
 * This class provides a skeletal implementation of the {@link Seq} interface
 * to minimize the effort required to implement the interface.
 * @param <T> The type of values in the sequence.
 */
public abstract class AbstractSeq<T> implements Seq<T> {
    /**
     * Returns false.
     * @return {@code false}
     */
    @Override // Seq
    public final boolean empty() {
        return false;
    }

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
