package org.korz.yas;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A skeletal implementation of the {@link Seq} interface.
 * <p>
 * If the implementation can never be empty, consider subclassing
 * {@link AbstractNonEmptySeq} instead.
 * @param <T> The type of values in the sequence.
 */
public abstract class AbstractSeq<T> implements Seq<T> {
    @Override // Seq
    public final <TOut> TOut apply(Function<Seq<T>, TOut> f) {
        return f.apply(this);
    }

    @Override // Seq
    public final void apply(Consumer<Seq<T>> f) {
        f.accept(this);
    }

    @Override // Seq
    public final boolean apply(Predicate<Seq<T>> f) {
        return f.test(this);
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
