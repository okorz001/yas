package org.korz.yas;

import java.util.Collection;

/**
 * A persistent collection.
 * <p>
 * A persistent collection is immutable and thread-safe. "Adding" elements to
 * a persistent collection returns a new instance of the same type.
 * <p>
 * By implementing the {@link Collection} interface, this class gains many
 * mutating methods. However, all of these methods will throw
 * {@link UnsupportedOperationException}.
 * @param <T> The type of values in the collection.
 */
public interface PCol<T> extends Collection<T> {
    /**
     * Returns the size of this collection in constant time.
     * @return The size.
     */
    int size();

    /**
     * "Adds" the value to this collection by creating a new instance with the
     * same type and values as this one, plus the specified value.
     * <p>
     * The name conj is short for conjoin.
     * @param val Value to add.
     * @return A new collection.
     */
    // TODO: nasty name (from clojure), but add is already taken by java
    PCol<T> conj(T val);
}
