package org.korz.yas;

import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * A skeletal implementation of the {@link PList} interface.
 * @param <T> The type of values in the list.
 */
public abstract class AbstractPList<T> extends AbstractCollection<T> implements PList<T> {

    private final int size;

    protected AbstractPList(int size) {
        this.size = size;
    }

    @Override // PCol
    public final PList<T> conj(T val) {
        return new PListCons<>(val, this);
    }

    @Override // Collection
    public final int size() {
        return size;
    }

    @Override // Collection
    public final Iterator<T> iterator() {
        return Seqs.iterator(this);
    }

    // cannot inherit from AbstractSeq too :(

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
