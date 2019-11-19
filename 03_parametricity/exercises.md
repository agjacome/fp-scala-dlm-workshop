---
title: 3. Parametric Polymorphism
subtitle: Functional Programming in Scala
---

### Exercise 1

Try to reason what the following function can do:

```scala
def foo[A, B](a: A, b: B): A
```

```haskell
foo :: a -> b -> a
```

---

### Exercise 2

Try to reason what the following function can do:

```scala
def bar[A, B](f: A => B, as: List[A]): List[B]
```

```haskell
bar :: a -> b -> [a] -> [b]
```

---

### Exercise 3

Try to reason what the following function can do:

```scala
def baz[A, B, F[_]](f: A => B): F[A] => F[B]
```

```haskell
baz :: (a -> b) -> (f a -> f b)
```

---

### Exercise 4

Evolving from Exercise 3, what about this one:

```scala
trait Something[F[_]] {
  def doSomething[A, B](f: A => B, fa: F[A]): F[B]
}

def baz[A, B, F[_]](something: Something[F], f: A => B): F[A] => F[B]
```

```haskell
class Something f where
  doSomething :: (a -> b) -> f a -> f b

baz :: Something f => (a -> b) -> (f a -> f b)
```

---

### Exercise 5

Consider the following definitions:

```scala
sealed trait Mammal

final case class Zebra(name: String) extends Mammal
final case class Giraffe(name: String) extends Mammal
```

And the following (valid!) Java snippet:

```java
Zebra[] zebras = new Zebra[]{ new Zebra("a zebra") }; // array with 1 zebra
Mammals[] mammals = zebras;
mammals[0] = new Giraffe("a giraffe");
Zebra zebra = zebras[0];
```

What is the problem with the snippet? What could be causing it to compile?

---

### Exercise 6

Consider the `Option` type definition in Scala:

```scala
sealed trait Option[+A]

final case class Some[+A](value: A) extends Option[A]
case object None extends Option[Nothing]
```

What are the benefits and inconvenients that the covariant `A` creates?

Try to create an invariant `Option` type that behaves the same way as the
covariant one

---
