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

```haskell
[a] -> [a]
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

```haskell
irrelevant :: a -> b
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

### Parametricity: Exhibit Three

Can you think of some theorem satisfied by the following definition?

```scala
def irrelevant[A](something: Option[A]): List[A]
```

```haskell
irrelevant :: Maybe a -> [a]
```

---

### Parametricity: Exhibit Three

```scala
def irrelevant[A](something: Option[A]): List[A]
```

> **Theorems:**
>
> * All the values in the returned list are the same
> * The `A` value in the returned list is the one in Some
> * If the argument is None, then an empty list is returned

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

---

### The Limits of Parametricity

By Types (proofs) alone, it is not possible to narrow down to one single
possibility in the _general case_. However:

* We can provide one single inhabitant for some specific cases

* We have tools to assist us when coming upon these limitations

* **Types are proof-positive**

* **Tests are proof-negative**

---

### Type boundaries

Scala allows to set some boundaries in polymorphic types. Consider the
following definitions:

```scala
sealed trait MediaticAnimal { def name: String }

final case class Pigeon (name: String) extends MediaticAnimal
final case class Cricket(name: String) extends MediaticAnimal
final case class Human  (name: String) extends MediaticAnimal
```

---

### Upper type bounds

The generic type of a function can be restricted to subtypes of another one by
using the `<:` operator in the generic type declaration

```scala
def doSomething[A <: MediaticAnimal](animal: A): Int = 42

doSomething(Human("A Bird Watcher")) // compiles correctly

doSomething(123) // does not compile, Int does not extend MediaticAnimal
```

`<:` declares an **uper type bound**

---

### Upper type bounds

Moreover, since the generic now has a bound, the compiler knows that all the
methods present in the supertype are also available in the generic type

```scala
def greet[A <: MediaticAnimal](animal: A): String =
  s"Hello ${animal.name}!!"
    
greet(Human("A Bird Watcher")) // "Hello A Bird Watcher!!"
```

---

### Lower type bounds

On the other hand, and being a bit less useful outside variance problems, a
generic type can also be restricted to supertypes of another one by using the
`>:` operator in the generic type declaration

```scala
def doSomething[A >: Human](animal: A): Int = 42

doSomething(Human(...))   // compiles

doSomething(Cricket(...)) // does not compile
```

`>:` declares a **lower type bound**

---

### Type variance

As Scala type system has support for subtyping (`A extends B`), some weird
situations can occurr in conjunction with parametric polymorphism

```scala
final case class Elevator[A](contents: List[A])

def isEmpty(elevator: Elevator[MediaticAnimal]): Boolean =
  box.contents.isEmpty

val e1: Elevator[Human]   = Elevator(List(Human(...), Human(...)))
val e2: Elevator[Cricket] = Elevator(List.empty)

isEmpty(e1) // does not compile
isEmpty(e2) // does not compile
```

---

### Invariant types

* The previous example fails because `Elevator` is defined as invariant in its
  type argument `A`

* This implies that `Elevator[Human]` is not a subtype of
  `Elevator[MediaticAnimal]` and thus cannot be passed as argument to the
  `isEmpty` function

* Invariance is good in scenarios where subtyping is forbidden, but not for
  most common object-oriented scenarios

---

### Covariance

Given a `class T[A]`, we say that **T is covariant in its type A** if given two
types such that `A extends B`, then `T[A] extends T[B]`

The way to make generic types covariant in scala is to use the `+` operator in
the generic type definition

```scala
class Foo[A]  // invariant in A
class Foo[+A] // covariant in A
```

---

### Covariance

```scala
final case class Elevator[+A](contents: List[A])

def isEmpty(elevator: Elevator[MediaticAnimal]): Boolean =
  box.contents.isEmpty

val e1: Elevator[Human]   = Elevator(List(Human(...), Human(...)))
val e2: Elevator[Cricket] = Elevator(List.empty)

isEmpty(e1) // compiles!!
isEmpty(e2) // compiles!!
```

---

### Contravariance

Given a `class T[A]`, we say that **T is contravariant in its type A** if given
two types such that `B extends A`, then `T[A] extends T[B]`

The way to make generic types contravariant in scala is to use the `-` operator
in the generic type definition

```scala
class Foo[A]  // invariant in A
class Foo[+A] // covariant in A
class Foo[-A] // contravariant in A
```

---

### Contravariance

TODO EXAMPLE

---

### 

In the general case, **contravariant** types are used for **input**, while
**covariant** types are used for **output**.

That's why Scala's function type definition is something like this:

```scala
trait Function[-Input, +Output] {

  def apply(input: Input): Output

}
```

---

### TODO

- Higher kinded types
- Links to further reading: existential types, dependent types
