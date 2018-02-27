package org.korz.yas;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.korz.yas.Seqs.*;
import static org.korz.yas.Seqs.empty;

public class SeqsTest {
    // TODO: is this generally useful?
    private static <T1, T2> Pair<T1, T2> pair(T1 first, T2 second) {
        return new Pair<>(first, second);
    }

    @Test
    public void emptyTest() {
        Seq<Object> seq = empty();
        assertThat(seq.empty(), is(true));
        assertThat(seq.rest(), is(seq));
    }

    @Test
    public void consTest() {
        Seq<Integer> seq = cons(1, empty());
        assertThat(seq.empty(), is(false));
        assertThat(seq.first(), is(1));
        assertThat(seq.rest(), is(empty()));
    }

    @Test
    public void lazyTest() {
        AtomicInteger called = new AtomicInteger();
        AtomicReference<Seq<Integer>> ref = new AtomicReference<>();
        Seq<Integer> seq = lazy(() -> {
            called.incrementAndGet();
            return ref.get();
        });
        assertThat(called.get(), is(0));

        // verify deferred execution
        ref.set(cons(1, empty()));
        assertThat(seq.empty(), is(false));
        assertThat(seq.first(), is(1));
        assertThat(seq.rest(), is(empty()));
        assertThat(called.get(), is(1));

        // verify memoization
        ref.set(empty());
        assertThat(seq.empty(), is(false));
        assertThat(seq.first(), is(1));
        assertThat(seq.rest(), is(empty()));
        assertThat(called.get(), is(1));
    }

    @Test
    public void lazyTestEmpty() {
        Seq<Integer> seq = lazy(Seqs::empty);
        assertThat(seq.empty(), is(true));
    }

    @Test
    public void repeatTest() {
        Seq<Integer> expected = cons(1, cons(1, cons(1, empty())));
        Seq<Integer> result = take(3, repeat(1));
        assertThat(result, is(expected));
    }

    @Test
    public void seqTestIterable() {
        Seq<Integer> result = seq(Arrays.asList(1, 2, 3));
        assertThat(result, is(list(1, 2, 3)));
    }

    @Test
    public void seqTestArray() {
        Seq<Integer> result = seq(new Integer[] {1, 2, 3});
        assertThat(result, is(list(1, 2, 3)));
    }

    @Test
    public void seqTestEnumeration() {
        Seq<Integer> result = seq(Collections.enumeration(Arrays.asList(1, 2, 3)));
        assertThat(result, is(list(1, 2, 3)));
    }

    @Test
    public void seqTestStream() {
        Seq<Integer> result = seq(Stream.of(1, 2, 3));
        assertThat(result, is(list(1, 2, 3)));
    }

    @Test
    public void toStringTest() {
        Seq<String> seq = cons("a", cons("b", cons("c", empty())));
        assertThat(seq.toString(), is("(a, b, c)"));
    }

    @Test
    public void listTest() {
        Seq<String> result = list("a", "b", "c");
        Seq<String> expected = cons("a", cons("b", cons("c", empty())));
        assertThat(result, is(expected));
    }

    @Test
    public void toStringEmptyTest() {
        assertThat(empty().toString(), is("()"));
    }

    @Test
    public void toStringSingleTest() {
        Seq<String> seq = cons("a", empty());
        assertThat(seq.toString(), is("(a)"));
    }

    @Test
    public void equalsTest() {
        Seq<String> a = cons("a", cons("b", cons("c", empty())));
        Seq<String> b = cons("a", cons("b", cons("c", empty())));
        assertThat(a, is(b));
    }

    @Test
    public void hashCodeTest() {
        Seq<String> a = cons("a", cons("b", cons("c", empty())));
        Seq<String> b = cons("a", cons("b", cons("c", empty())));
        assertThat(a.hashCode(), is(b.hashCode()));
    }

    @Test
    public void hashCodeTestDifferent() {
        Seq<String> a = cons("a", cons("b", cons("c", empty())));
        Seq<String> b = cons("x", cons("y", cons("z", empty())));
        assertThat(a.hashCode(), not(b.hashCode()));
    }

    // from here on, we can assume Seqs.list works correctly

    @Test
    public void mapTest() {
        Seq<String> result = list("a", "b", "c")
                .apply(map(String::toUpperCase));
        assertThat(result, is(list("A", "B", "C")));
    }

    @Test
    public void unzipTest() {
        Pair<Seq<String>, Seq<Integer>> result = list(pair("a", 1), pair("b", 2))
                .apply(unzip());
        assertThat(result.first(), is(list("a", "b")));
        assertThat(result.second(), is(list(1, 2)));
    }

    @Test
    public void filterTest() {
        Seq<Integer> result = list(1, 2, 3)
                .apply(filter(x -> x % 2 == 1));
        Seq<Integer> expected = list(1, 3);
        assertThat(result, is(expected));
    }

    @Test
    public void distinctTest() {
        Seq<Integer> result = list(1, 2, 1)
                .apply(distinct());
        assertThat(result, is(list(1, 2)));
    }

    @Test
    public void foldLeftTest() {
        String result = list("a", "b", "c")
                .apply(foldLeft(String::concat, "_"));
        assertThat(result, is("_abc"));
    }

    @Test
    public void reduceTest() {
        int result = list(1, 2, 3)
                .apply(reduce(Integer::sum));
        assertThat(result, is(6));
    }

    @Test
    public void forEachTest() {
        StringBuilder str = new StringBuilder();
        list("a", "b", "c")
                .apply(forEach(str::append));
        assertThat(str.toString(), is("abc"));
    }

    @Test
    public void reverseTest() {
        Seq<String> result = list("a", "b", "c")
                .apply(reverse());
        assertThat(result, is(list("c", "b", "a")));
    }

    @Test
    public void foldRightTest() {
        String result = list("a", "b", "c")
                .apply(foldRight(String::concat,"_"));
        assertThat(result, is("abc_"));
    }

    @Test
    public void findTest() {
        Optional<Integer> result = list(1, 2, 3)
                .apply(find(i -> i % 2 == 1));
        assertThat(result, is(Optional.of(1)));
    }

    @Test
    public void findNoneTest() {
        Optional<Integer> result = list(1, 2, 3)
                .apply(find(i -> i > 4));
        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void anyTest() {
        boolean result = list(1, 2, 3)
                .apply(any(i -> i > 1));
        assertThat(result, is(true));
    }

    @Test
    public void anyTestFalse() {
        boolean result = list(1, 2, 3)
                .apply(any(i -> i > 10));
        assertThat(result, is(false));
    }

    @Test
    public void allTest() {
        boolean result = list(1, 2, 3)
                .apply(all(i -> i > 0));
        assertThat(result, is(true));
    }

    @Test
    public void allTestFalse() {
        boolean result = list(1, 2, 3)
                .apply(all(i -> i > 1));
        assertThat(result, is(false));
    }

    @Test
    public void minTest() {
        int result = list(2, 1, 3)
                .apply(min(Integer::compareTo));
        assertThat(result, is(1));
    }

    @Test
    public void maxTest() {
        int result = list(1, 3, 2)
                .apply(max(Integer::compareTo));
        assertThat(result, is(3));
    }

    @Test
    public void takeWhileTest() {
        Seq<Integer> result = list(1, 2, 3, 2)
                .apply(takeWhile(x -> x < 3));
        assertThat(result, is(list(1, 2)));
    }

    @Test
    public void takeTest() {
        Seq<String> result = list("a", "b", "c")
                .apply(take(2));
        assertThat(result, is(list("a", "b")));
    }

    @Test
    public void takeTestTooMany() {
        Seq<String> result = list("a", "b", "c")
                .apply(take(100));
        assertThat(result, is(list("a", "b", "c")));
    }

    @Test
    public void dropWhileTest() {
        Seq<Integer> result = list(1, 2, 3, 2)
                .apply(dropWhile(x -> x < 3));
        assertThat(result, is(list(3, 2)));
    }

    @Test
    public void dropTest() {
        Seq<String> result = list("a", "b", "c", "d")
                .apply(drop(2));
        assertThat(result, is(list("c", "d")));
    }

    @Test
    public void dropTestTooMany() {
        Seq<String> result = list("a", "b", "c")
                .apply(drop(100));
        assertThat(result, is(empty()));
    }

    @Test
    public void nthTest() {
        Optional<String> result = list("a", "b", "c")
                .apply(nth(2));
        assertThat(result, is(Optional.of("c")));
    }

    @Test
    public void nthTestTooMany() {
        Optional<String> result = list("a", "b", "c")
                .apply(nth(100));
        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void concatTest() {
        Seq<Object> result = list("a", "b")
                .apply(concat(list(1, 2)));
        assertThat(result, is(list("a", "b", 1, 2)));
    }

    @Test
    public void cycleTest() {
        Seq<String> result = list("a", "b")
                .apply(cycle())
                .apply(take(5));
        assertThat(result, is(list("a", "b", "a", "b", "a")));
    }

    @Test
    public void flattenTest() {
        // ((a, b), 0, (c, d))
        Seq<Object> result = list(list("a", "b"), 0, list("c", "d"))
            .apply(flatten());
        assertThat(result, is(list("a", "b", 0, "c", "d")));
    }

    @Test
    public void flattenTestRecursive() {
        // (((a, b)), 0)
        Seq<Object> result = list(list(list("a", "b")), 0)
                .apply(flatten());
        assertThat(result, is(list("a", "b", 0)));
    }

    @Test
    public void flattenSafeTest() {
        // ((a, b), (c, d))
        Seq<String> result = list(list("a", "b"), list("c", "d"))
                .apply(flattenSafe());
        assertThat(result, is(list("a", "b", "c", "d")));
    }

    @Test
    public void flatMapTest() {
        Seq<String> result = list(0, 1)
                .apply(flatMap(x -> list(Integer.toString(x + 10),
                                         Integer.toString(x + 100))));
        assertThat(result, is(list("10", "100", "11", "101")));
    }

    @Test
    public void iterateTest() {
        Seq<Integer> result = iterate(x -> x + 1, 0)
                .apply(take(3));
        assertThat(result, is(list(0, 1, 2)));
    }

    @Test
    public void range1Test() {
        assertThat(range(3), is(list(0, 1, 2)));
    }

    @Test
    public void range2Test() {
        assertThat(range(2, 5), is(list(2, 3, 4)));
    }

    @Test
    public void range3Test() {
        assertThat(range(1, 6, 2), is(list(1, 3, 5)));
    }

    @Test
    public void zipTest() {
        Seq<Pair<String, Integer>> result = list("a", "b")
                .apply(zip(list(1, 2)));
        assertThat(result, is(list(pair("a", 1), pair("b", 2))));
    }

    @Test
    public void zipTestShort() {
        Seq<Pair<String, Integer>> result = list("a", "b")
                .apply(zip(list(1)));
        assertThat(result, is(list(pair("a", 1))));
    }

    @Test
    public void enumerateTest() {
        Seq<Pair<Integer, String>> result = list("a", "b")
                .apply(enumerate());
        assertThat(result, is(list(pair(0, "a"), pair(1, "b"))));
    }

    @Test
    public void interleaveTest() {
        Seq<?> result = list("a", "b")
                .apply(interleave(list(0, 1)));
        assertThat(result, is(list("a", 0, "b", 1)));
    }

    @Test
    public void interleaveTestEmpty() {
        Seq<String> result = list("a", "b")
                .apply(interleave(empty()));
        assertThat(result, is(list("a", "b")));
        result = Seqs.<String>empty()
                .apply(interleave(list("a", "b")));
        assertThat(result, is(list("a", "b")));
    }
}
