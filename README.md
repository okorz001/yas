# yas

Yet Another Sequence library

![YAS](https://i.giphy.com/media/3o7btWMurfV7OeRBpC/giphy-downsized.gif)

## What Are Sequences?

Sequences are immutable ordered lists of zero or more non-distinct values.

* __Ordered__: The order of values is well-defined and controlled by the
programmer.
* __Immutable__: Values can never be added to or removed a sequence, nor can
existing values be re-ordered.
* __Non-distinct__: A sequence may contain duplicate values.

The sequence interface has two primary abstractions:

* `first`: Returns the first value in the sequence.
* `rest`: Returns the remaining values in the sequence as a sub-sequence.

Traversing a sequence involves recursively calling `rest` until reaching the
empty sequence. This abstraction permits two special kinds of sequences:

* __Lazy__: A lazy sequence does not generate its value until the first time
it is needed.
* __Infinite__: An infinite sequence never ends. (`rest` never returns the
empty sequence.)

## Usage

Complete documentation is available in Javadoc format.

YAS requires Java 8 for the [`java.util.function`][j.u.f] package and is
designed to be used with [lambdas][lambdas].

A sequence implements the `Seq` interface, which is a minimal abstraction.
Generally, applications should use the higher level functions in `Seqs` (e.g.
`map`, `filter`) to create and manipulate `Seq` instances.

[j.u.f]: https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html
[lambdas]: https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html
