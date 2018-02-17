package org.korz.yas;

import java.util.function.Supplier;

/**
 * Creates a lazy sequence that is generated on-demand.
 * <p>
 * The sequence is guaranteed to be generated no more than once. The generating
 * function will not be called until the first invocation of a {@link Seq}
 * method. The result of the function is cached and future invocations of
 * {@link Seq} methods will return the cached result. If no {@link Seq} methods
 * are never called, then the function will never be called, allowing the
 * creation of <i>infinite sequences</i>.
 * <p>
 * <b>Important</b>: The generating function must not depend on any external
 * state whatsoever. The return value must be deterministic since the execution
 * of the function is delayed indefinitely.
 * @param <T> The type of values in the sequence.
 */
public final class Lazy<T> extends AbstractSeq<T> {
    private final Supplier<Seq<? extends T>> supplier;

    /**
     * Creates a new lazy sequence.
     * @param supplier The function to generate the sequence.
     * @see Seqs#lazy
     */
    public Lazy(Supplier<Seq<? extends T>> supplier) {
        this.supplier = new MemoizedSupplier<>(supplier);
    }

    private Seq<T> get() {
        return Seqs.upcast(supplier.get());
    }

    @Override // Seq
    public boolean empty() {
        return get().empty();
    }

    @Override // Seq
    public T first() {
        return get().first();
    }

    @Override // Seq
    public Seq<T> rest() {
        return get().rest();
    }

    // avoid repeated invocations and release memory when possible
    private static final class MemoizedSupplier<T> implements Supplier<T> {
        private Supplier<T> fn;
        private T result;

        public MemoizedSupplier(Supplier<T> fn) {
            this.fn = fn;
        }

        @Override // Supplier
        public T get() {
            if (result == null) {
                synchronized (this) {
                    if (result == null) {
                        result = fn.get();
                        // allow GC
                        fn = null;
                    }
                }
            }
            return result;
        }
    }
}
