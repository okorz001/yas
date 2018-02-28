package org.korz.yas;

/**
 * A persistent singly-linked list.
 * <p>
 * A {@link PList} is just a non-lazy {@link Seq} with a known size.
 * <p>
 * <b>Note:</b> Despite the name "list", PList does not implement
 * {@link java.util.List} because it supports neither random access nor
 * reverse iteration (as required by {@link java.util.ListIterator}). To
 * get the Nth element of a PList in linear time, use {@link Seqs#nth}.
 * @param <T> The type of values in the list.
 */
public interface PList<T> extends Seq<T>, PCol<T> {
    @Override // PCol
    PList<T> conj(T val);
}
