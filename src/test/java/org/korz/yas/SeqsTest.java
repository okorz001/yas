package org.korz.yas;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.korz.yas.Seqs.*;
import static org.korz.yas.Seqs.empty;

public class SeqsTest {
    @Test
    public void emptyTest() {
        Seq<Object> seq = empty();
        assertThat(seq.empty(), is(true));
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
    public void toStringTest() {
        Seq<String> seq = cons("a", cons("b", cons("c", empty())));
        assertThat(seq.toString(), is("(a, b, c)"));
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

    @Test
    public void mapTest() {
        Seq<String> seq = cons("a", cons("b", cons("c", empty())));
        Seq<String> expected = cons("A", cons("B", cons("C", empty())));
        Seq<String> result = map(String::toUpperCase, seq);
        assertThat(result, is(expected));
    }

    @Test
    public void filterTest() {
        Seq<Integer> seq = cons(1, cons(2, cons(3, empty())));
        Seq<Integer> expected = cons(1, cons(3, empty()));
        Seq<Integer> result = filter(x -> x % 2 == 1, seq);
        assertThat(result, is(expected));
    }

    @Test
    public void distinctTest() {
        Seq<Integer> seq = cons(1, cons(2, cons(1, empty())));
        Seq<Integer> expected = cons(1, cons(2, empty()));
        Seq<Integer> result = distinct(seq);
        assertThat(result, is(expected));
    }

    @Test
    public void foldLeftTest() {
        Seq<String> seq = cons("a", cons("b", cons("c", empty())));
        String result = foldLeft(String::concat, "_", seq);
        assertThat(result, is("_abc"));
    }

    @Test
    public void reduceTest() {
        Seq<Integer> seq = cons(1, cons(2, cons(3, empty())));
        Integer result = reduce(Integer::sum, seq);
        assertThat(result, is(6));
    }

    @Test
    public void forEachTest() {
        Seq<String> seq = cons("a", cons("b", cons("c", empty())));
        StringBuilder str = new StringBuilder();
        forEach(str::append, seq);
        assertThat(str.toString(), is("abc"));
    }

    @Test
    public void reverseTest() {
        Seq<String> seq = cons("a", cons("b", cons("c", empty())));
        Seq<String> expected = cons("c", cons("b", cons("a", empty())));
        Seq<String> result = reverse(seq);
        assertThat(result, is(expected));
    }

    @Test
    public void foldRightTest() {
        Seq<String> seq = cons("a", cons("b", cons("c", empty())));
        String result = foldRight(String::concat, seq, "_");
        assertThat(result, is("abc_"));
    }

    @Test
    public void findTest() {
        Seq<Integer> seq = cons(1, cons(2, cons(3, empty())));
        Optional<Integer> result = find(i -> i % 2 == 1, seq);
        assertThat(result, is(Optional.of(1)));
    }

    @Test
    public void findNoneTest() {
        Seq<Integer> seq = cons(1, cons(2, cons(3, empty())));
        Optional<Integer> result = find(i -> i > 4, seq);
        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void anyTest() {
        Seq<Integer> seq = cons(1, cons(2, cons(3, empty())));
        assertThat(any(i -> i > 1, seq), is(true));
    }

    @Test
    public void anyTestFalse() {
        Seq<Integer> seq = cons(1, cons(2, cons(3, empty())));
        assertThat(any(i -> i > 10, seq), is(false));
    }

    @Test
    public void allTest() {
        Seq<Integer> seq = cons(1, cons(2, cons(3, empty())));
        assertThat(all(i -> i > 0, seq), is(true));
    }

    @Test
    public void allTestFalse() {
        Seq<Integer> seq = cons(1, cons(2, cons(3, empty())));
        assertThat(all(i -> i > 1, seq), is(false));
    }

    @Test
    public void minTest() {
        Seq<Integer> seq = cons(2, cons(1, cons(3, empty())));
        Integer result = min(Integer::compareTo, seq);
        assertThat(result, is(1));
    }

    @Test
    public void maxTest() {
        Seq<Integer> seq = cons(1, cons(3, cons(2, empty())));
        Integer result = max(Integer::compareTo, seq);
        assertThat(result, is(3));
    }

    @Test
    public void takeWhileTest() {
        Seq<Integer> seq = cons(1, cons(2, cons(3, cons( 2, empty()))));
        Seq<Integer> expected = cons(1, cons(2, empty()));
        Seq<Integer> result = takeWhile(x -> x < 3, seq);
        assertThat(result, is(expected));
    }

    @Test
    public void takeTest() {
        Seq<String> seq = cons("a", cons("b", cons("c", empty())));
        Seq<String> expected = cons("a", cons("b", empty()));
        Seq<String> result = take(2, seq);
        assertThat(result, is(expected));
    }

    @Test
    public void takeTestTooMany() {
        Seq<String> seq = cons("a", cons("b", cons("c", empty())));
        Seq<String> result = take(100, seq);
        assertThat(result, is(seq));
    }

    @Test
    public void dropWhileTest() {
        Seq<Integer> seq = cons(1, cons(2, cons(3, cons( 2, empty()))));
        Seq<Integer> expected = cons(3, cons(2, empty()));
        Seq<Integer> result = dropWhile(x -> x < 3, seq);
        assertThat(result, is(expected));
    }

    @Test
    public void dropTest() {
        Seq<String> seq = cons("a", cons("b", cons("c", empty())));
        Seq<String> expected = cons("c", empty());
        Seq<String> result = drop(2, seq);
        assertThat(result, is(expected));
    }

    @Test
    public void dropTestTooMany() {
        Seq<String> seq = cons("a", cons("b", cons("c", empty())));
        Seq<String> result = drop(100, seq);
        assertThat(result, is(empty()));
    }

    @Test
    public void nthTest() {
        Seq<String> seq = cons("a", cons("b", cons("c", empty())));
        Optional<String> result = nth(2, seq);
        assertThat(result, is(Optional.of("c")));
    }

    @Test
    public void nthTestTooMany() {
        Seq<String> seq = cons("a", cons("b", cons("c", empty())));
        Optional<String> result = nth(100, seq);
        assertThat(result, is(Optional.empty()));
    }

    @Test
    public void concatTest() {
        Seq<String> a = cons("a", cons("b", empty()));
        Seq<String> b = cons("c", cons("d", empty()));
        Seq<String> expected = cons("a", cons("b", cons("c", cons( "d", empty()))));
        Seq<String> result = concat(a, b);
        assertThat(result, is(expected));
    }

    @Test
    public void iterateTest() {
        Seq<Integer> expected = cons(0, cons(1, cons(2, empty())));
        Seq<Integer> result = take(3, iterate(x -> x + 1, 0));
        assertThat(expected, is(result));
    }

    @Test
    public void range1Test() {
        Seq<Integer> expected = cons(0, cons(1, cons(2, empty())));
        Seq<Integer> result = range(3);
        assertThat(expected, is(result));
    }

    @Test
    public void range2Test() {
        Seq<Integer> expected = cons(2, cons(3, cons(4, empty())));
        Seq<Integer> result = range(2, 5);
        assertThat(expected, is(result));
    }

    @Test
    public void range3Test() {
        Seq<Integer> expected = cons(1, cons(3, cons(5, empty())));
        Seq<Integer> result = range(1, 6, 2);
        assertThat(expected, is(result));
    }
}
