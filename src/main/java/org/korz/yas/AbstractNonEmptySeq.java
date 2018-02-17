package org.korz.yas;

/**
 * A skeletal implementation of the {@link Seq} interface that is never empty.
 * <p>
 * If the implementation can be empty, subclass {@link AbstractSeq} instead.
 * @param <T> The type of values in the sequence.
 */
public abstract class AbstractNonEmptySeq<T> extends AbstractSeq<T> {
    /**
     * Returns false.
     * @return {@code false}
     */
    @Override // Seq
    public final boolean empty() {
        return false;
    }
}
