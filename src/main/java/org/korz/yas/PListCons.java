package org.korz.yas;

/**
 * A non-empty persistent singly-linked list.
 * @param <T> The type of values in the list.
 */
public final class PListCons<T> extends AbstractPList<T> {

    // composition
    private final Cons<T> cons;

    /**
     * Creates a new list.
     * @param first The first value in the list.
     * @param rest The remaining sub-list.
     */
    public PListCons(T first, PList<? extends T> rest) {
        super(rest.size() + 1);
        cons = new Cons<>(first, rest);
    }

    @Override // Seq
    public boolean empty() {
        return false;
    }

    @Override // Seq
    public T first() {
        return cons.first();
    }

    @Override // Seq
    public Seq<T> rest() {
        return cons.rest();
    }
}
