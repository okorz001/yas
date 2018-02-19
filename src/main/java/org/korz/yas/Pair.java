package org.korz.yas;

/**
 * A pair of a values.
 * @param <T1> The type of the first value.
 * @param <T2> The type of the second value.
 */
public final class Pair<T1, T2> {
    private final T1 first;
    private final T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first value.
     * @return The first value.
     */
    public T1 first() {
        return first;
    }

    /**
     * Returns the second value.
     * @return The second value.
     */
    public T2 second() {
        return second;
    }

    @Override // Object
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    @Override // Object
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o instanceof Pair) {
            Pair other = (Pair) o;
            return first.equals(other.first()) &&
                    second.equals(other.second());
        }
        return false;
    }

    @Override // Object
    public int hashCode() {
        // djb2
        int hash = 5381;
        hash = 33 * hash + first.hashCode();
        hash = 33 * hash + second.hashCode();
        return hash;
    }
}
