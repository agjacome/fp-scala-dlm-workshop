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

<small>This is getting solved in Scala 3, see [the proof of
concept](https://github.com/lampepfl/dotty/pull/4672)</small>

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

### Properties of parametric types

* A generic function can be seen as a function taking a type as input, thus
  becoming a function from type to value

* There is no type-safe way to create a value of a generic type

* There is no type-safe way to introspect information about a value of a
  generic type

---

### Parametricity

> Write down the definition of a polymorphic function on a piece of paper. Tell
> me its type, but be careful not to let me see the function’s definition. I will
> tell you a theorem that the function satisfies
>
> Philip Wadler - [Theorems for free (1989)](https://people.mpi-sws.org/~dreyer/tor/papers/wadler.pdf)

---

<img src="/resources/images/03_parametricity.wadler.png" height="560px" />

---

### Parametricity: Exhibit One

Let's try it:

```scala
def irrelevant[A](as: List[A]): List[A]
```

* We can assert a lot of things about how this function works

* We can, more importantly, assert what the function **does not** do

---

### Parametricity: Exhibit One

```scala
def irrelevant[A](as: List[A]): List[A]
```

> **Theorem:**
>
> * Every element A in the output list appears also in the input list
> * Contraposed, if A is not in the input list, then it is not in the result

---

### Parametricity: Exhibit Two

Can you think of some theorem satisfied by the following definition?

```scala
def irrelevant[A, B](a: A): B
```

---

### Parametricity: Exhibit Two

```scala
def irrelevant[A, B](a: A): B
```

> **Theorem:**
>
> This function **never** returns because if it did, it would never have
> compiled

---

### Fast and Loose reasoning

> Functional programmers often reason about programs as if they were written in
> a total language, expecting the results to carry over to non-total (partial)
> languages. We justify such reasoning.
>
> Danielsson, Hughes, Jansson, Gibbons - [Fast and loose reasoning is morally
  correct (2006)](https://www.cs.ox.ac.uk/jeremy.gibbons/publications/fast+loose.pdf)

---

### Fast and Loose reasoning

In our discussion about purity and type cardinality we already covered a minor
problem in our reasoning. Consider:

```scala
def isEven(num: Int): Boolean
```

> **Theorem**:
>
> The isEven function returns either True or False

---

### Fast and Loose reasoning

But actually, the `isEven` function may not ever return anything:

```scala
def isEven(num: Int): Boolean = isEven(num)

def isEven(num: Int): Boolean = throw new IllegalArgumentException

def isEven(num: Int): Boolean = null
```

Yet, we casually exclude this possibility in discussion, because we presupose
that the function is pure and terminates at some point

---

### Fast and Loose reasoning

This kind of reasoning and exclusion of impure and non-terminating functions is
commonly referred as **fast and loose reasoning**

It is based on the assumption that even if the language is **partial**, we as
functional programmers will restrict the language, and thus benefit from the
same reasoning tools that **total** languages provide

---

### Parametricity and F&L: Challenger One

```scala
def irrelevant[A](x: A): Boolean
```

> Ignores its argument and consistently returns either True or False

---

### Parametricity and F&L: Challenger One

```scala
def irrelevant[A](x: A): Boolean = x.isInstanceOf[Int]
```

> ~~Ignores its argument and consistently returns either True or False~~

**Type-casing** (case-analysis on type) breaks parametricity

---

### Parametricity and F&L: Challenger Two

```scala
def irrelevant[A](as: List[A]): List[A]
```

> Every element A in the output list appears also in the input list

---

### Parametricity and F&L: Challenger Two

```scala
def irrelevant[A](as: List[A]): List[A] = List("abc".asInstanceOf[A])
```

> ~~Every element A in the output list appears also in the input list~~

**Type-casting** breaks parametricity

---

### Parametricity and F&L: Challenger Three

```scala
def irrelevant[A](a: A): Int
```

> Ignores its argument and consistently returns one of the possible 2^32 Int
> values

---

### Parametricity and F&L: Challenger Three

```scala
def irrelevant[A](a: A): Int = a.toString.length
```

> ~~Ignores its argument and consistently returns one of the possible 2^32 Int
> values~~

**Java's Object and Scala's Any** methods break parametricity

---

### Scala's escape hatches

* Scala does indeed have a lot of escape hatches

* This is totally OK from the language perspective

* Scala is purely Object-Oriented with functional capabilities, not the other
  way around

* But can we abandon those escape hatches whout too much penalty?

---

### The Scalazzi Safe Subset

* No **null**
* No **exceptions**
* No **side effects**
* No **type-casing** (`isInstanceOf`)
* No **type-casting** (`asInstanceOf`)
* No **classOf / getClass**
* No **equals / hashCode / toString**
* No **notify / wait**

---

### The Scalazzi Safe Subset

* The **reasoning gets improved**, but at what cost?

* Eliminating those hatches results in minimal, orthogonal, easily-managed
  penalties, resulting in a **significant language improvement** from the FP
  perspective

* All significant **pure functional programming projects** in Scala abide to
  this subset of the language

---

### The Limits of Parametricity

```scala
def thisIsNotReverse[A](as: List[A]): List[A]
```

We know that all the elements of the output appear on the input, but:

* How do we narrow it down?

* How do we rule all possible implementations but one?

* How do we specifically determine what the function does?

---

### The Limits of Parametricity

By Types (proofs) alone, it is not possible to narrow down to one single
possibility in the _general case_. However:

* We can provide one single inhabitant for some specific cases

* We have tools to assist us when coming upon these limitations

* **Types are proof-positive**

* **Tests are failed-proof negative**

---

### TODO

- Variance
- Existential types
- Dependent types
- Higher kinded types
