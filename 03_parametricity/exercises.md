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
