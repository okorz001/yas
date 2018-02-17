package org.korz.yas;

import java.util.function.Supplier;

/**
 * A lazy sequence that generates the remaining sub-sequence on-demand.
 * <p>
 * The remaining sub-sequence is guaranteed to be generated no more than once.
 * The generating function will not be called until the first invocation of
 * {@link Seq#rest}. The result of the function is cached and future
 * invocations of {@link Seq#rest} will return the cached result.
 * If {@link Seq#rest} is never called, then the function will never be called,
 * allowing the creation of <i>infinite sequences</i>.
 * <p>
 * <b>Important</b>: The generating function must not depend on any external
 * state whatsoever. The return value must be deterministic since the
 * execution of the function is delayed indefinitely.
 * <p>
 * This class is thread-safe and is visibly immutable.
 * @param <T> The type of values in the sequence.
 */
public final class Lazy<T> extends AbstractSeq<T> {
    private final T first;
    private final Supplier<Seq<? extends T>> restFn;

    /**
     * Creates a new lazy sequence.
     * @param first The first value.
     * @param restFn The function to generate the remaining sub-sequence.
     * @see Seqs#lazy
     */
    public Lazy(T first, Supplier<Seq<? extends T>> restFn) {
        this.first = first;
        this.restFn = new MemoizedSupplier<>(restFn);
    }

    @Override // Seq
    public T first() {
        return first;
    }

    @Override // Seq
    public Seq<T> rest() {
        return Seqs.upcast(restFn.get());
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
