---
title: 3. Parametric Polymorphism
subtitle: Functional Programming in Scala
---

### The problem of repetition

Consider the following example definitions

```scala
final case class BoxOfInt(content: Int)
def putInBoxOfInt(aContent: Int): BoxOfInt = BoxOfInt(aContent)

final case class BoxOfString(content: String)
def putInBoxOfString(aContent: String): BoxOfString = BoxOfString(aContent)

final case class BoxOfInstant(content: Instant)
def putInBoxOfInstant(aContent: Instant): BoxOfInstant = BoxOfInstant(aContent)
```

There is a lot of repetition even in this very small example

---

### Avoiding Repetition

The solution in a language without "generics" (like Go) would be to either live
with the repetition or do something like this:

```scala
final case class Box(content: Any)

def putInBox(aContent: Any): Box = Box(aContent)
```

---

### Avoiding Repetition

But then we completely lose track of type information

```scala
val aBoxOfInt: Box = putInBox(42)      // we know that content is Int, but
val content: Int   = aBoxOfInt.content // this won't compile, as content is now Any
```

So a need to typecast all over the place arises

```scala
val aBoxOfInt: Box = putInBox(42)
val content: Int   = aBoxOfInt.content.asInstanceOf[Int]
```

---

### Avoiding Repetition

Programming against top-level types like `Any` breaks all the typesafe
guarantees that the typechecker provides at compile-time

---

### Parametric polymorphism

The better solution in languages with parametric polymorphism (aka generics) is
to make the `Box` type take a **type parameter**:

```scala
final case class Box[A](content: A)

def putInBox[A](aContent: A): Box[A] = Box(aContent)
```

---

### Parametric polymorphism

The better solution in languages with parametric polymorphism (aka generics) is
to make the `Box` type take a **type parameter**:

```haskell
-- Haskell equivalent
data Box a = Box { content :: a }

putInBox :: a -> Box a
putInBox newContent = Box newContent
```

---

### Parametric polymorphism

With this generic version, type information won't be lost:

```scala
val aBoxOfInt: Box[Int] = putInBox[Int](42)
val acontent: Int       = aBoxOfInt.content
```

```haskell
aBox :: Box Integer
aBox = putInBox 42

aContent :: Integer
aContent = content aBox
```

---

### Parametric polymorphism

The types are also correctly inferred by the compiler without type ascriptions

```scala
val aBoxOfInt = putInBox(42)      // inferred as Box[Int]
val acontent  = aBoxOfInt.content // inferred as Int
```

```haskell
aBox     = putInBox 42  -- inferred as Box Integer
aContent = content aBox -- inferred as Integer
```

---

### Parametric functions

Scala functions are **monomorphic**, parametric polymorphism is only supported
on **methods**

```scala
def parametricMethod[A](a: A): A = ???  // ok

val parametricFunction[A]: A => A = ??? // ko, illegal syntax
```

---

### Parametric functions

Upon eta-expansion, the generic type gets fixed **at expansion site**:

```scala
def parametricMethod[A](a: A): A = ???

val f1             = parametricMethod _         // f1: Nothing => Nothing
val f2: Int => Int = parametricMethod _         // f2: Int => Int
val f3             = parametricMethod[String] _ // f3: String => String
```

---

### TODO
