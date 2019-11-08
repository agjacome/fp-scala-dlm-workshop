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

### Upper type bounds

`<:` declares an **upper type bound**

`A <: B` means "any generic type A which is a subtype of type B or A itself"

```scala
def example[A <: java.time.Temporal](time: A): A

example(Instant.now())       // compiles
example(LocalDateTime.now()) // compiles
example("12:34:00")          // does not compile
```

---

### Lower type bounds

`>:` declares a **lower type bound**:

`A >: B` means "any generic type A which is a supertype of type B or type A
itself"

```scala
def example[A >: java.time.Instant](instant: A): A

example(Instant.now())                  // compiles
example(new java.time.Temporal { ... }) // compiles
example(LocalDateTime.now())            // does not compile
```

---

### Type variance

Consider the following definitions:

![](/resources/images/03_parametricity.variance_01.png)

```scala
final class VendingMachine[A]

def install(softDrinkVendingMachine: VendingMachine[SoftDrink]): VendingMachine[SoftDrink]
```

---

### Invariance

This VendingMachine, though, is invariant, which means that the following cases
will not work:

```scala
val colaVendingMachine: VendingMachine[Cola] = new VendingMachine[Cola]
val tonicWaterVendingMachine: VendingMachine[TonicWater] = new VendingMachine[TonicWater]

install(colaVendingMachine)       // does not compile
install(tonicWaterVendingMachine) // does not compile
```

Invariance implies that `VendingMachine[Cola]` is not a subtype of
`VendingMachine[SoftDrink]`, so wherever the second one is expected, then the
first one cannot be used

---

### Covariance

If we make VendingMachine covariant (by using the `+` operator):

```scala
final class VendingMachine[+A]

def install(softDrinkVendingMachine: VendingMachine[SoftDrink]): VendingMachine[SoftDrink]

val colaVendingMachine: VendingMachine[Cola] = new VendingMachine[Cola]
val tonicWaterVendingMachine: VendingMachine[TonicWater] = new VendingMachine[TonicWater]

install(colaVendingMachine)       // compiles!!
install(tonicWaterVendingMachine) // compiles!!
```

---

### Covariance

Covariant subtyping provides the generic class `class Foo[+A]` subtyping
information with respect to the generic type `A`:

* Given `Foo[+A]`
* When `AType` extends `AnotherType`
* Then `Foo[AType]` also extends `Foo[AnotherType]`

---

### Legal covariant positions

* In some languages instead of using the randomly-chosen `+` operator, they use
  a `out` keyword to define covariant type parameters.

* This is because contravariant type parameters can only occurr in "output"
  types from methods/functions

  ```scala
  final class VendingMachine[+A] {

    def foo: A          // LEGAL POSITION

    def bar(a: A): Unit // ILLEGAL POSITION, WON'T COMPILE

  }
  ```

---

### Legal covariant positions

To overcome this restriction, upper type bounds tend to be used together with
covariant types when an "input" possition is necessary:

```scala
final class VendingMachine[+A] {

  def bar[B >: A](b: B): Unit // THIS COMPILES!!

}
```

---

### Contravariance

Consider now the following example:

![](/resources/images/03_parametricity.variance_02.png)

```scala
final class GarbageCan[A]

def setGarbageCanForPlastic(can: GarbageCan[PlasticItem]): Unit
```

---

### Contravariance

Intuitivelly, a GarbageCan for Items can be set up as a GarbageCan for Plastic
too, but:

```scala
val garbageCanForItems: GarbageCan[Item] = new GarbageCan[Item]

setGarbageCanForPlastic(garbageCanForItems) // does not compile!!
```

---

### Contravariance

In order to make this work as expected, we need to make the generic type `A`
contravariant with the `-` operator:

```scala
final class GarbageCan[-A]

def setGarbageCanForPlastic(can: GarbageCan[PlasticItem]): Unit

val garbageCanForItems: GarbageCan[Item] = new GarbageCan[Item]

setGarbageCanForPlastic(garbageCanForItems) // it does compile now!!
```

---

### Contravariant

Contravariant subtyping provides the generic class `class Foo[-A]` subtyping
information with respect to the generic type `A`:

* Given `Foo[-A]`
* When `AType` is a supertype of `AnotherType`
* Then `Foo[AType]` extends `Foo[AnotherType]`

---

### Legal contravariant positions

* In some languages instead of using the randomly-chosen `-` operator, they use
  a `in` keyword to define contravariant type parameters.

* This is because contravariant type parameters can only occurr in "input"
  types from methods/functions

  ```scala
  final class GarbageCan[-A] {

    def foo(a: A): Unit // LEGAL POSITION, compiles

    def bar: A          // ILLEGAL POSITION, does not compile

  }
  ```

---

### Legal contravariant positions

To overcome this restriction, upper type bounds tend to be used together with
contravariant types when an "input" possition is necessary:

```scala
final class GarbageCan[-A] {

  def bar[B <: A]: B // THIS COMPILES!!

}
```

---

### Scala's Function Type

Scala's function types are defined somewhat like this:

```scala
trait Function[-Input, +Output] {

  def apply(input: Input): Output

}
```

Contravariant in the input, and covariant in the output

---

### Type holes

We could classify types by the number of "holes" that they have:

* No type holes: `Int`, `String`, `UUID`, ...

* One type hole: `List[A]`, `Set[A]`, `Option[A]`, `Try[A]`, ...

* Two type holes: `Map[A, B]`, `A => B`

---

### Kinds

Kinds are exactly that, but in a more "formal" way:

* Kind `*`: `Int`, `String`, `UUID`, ...

  * So `*` means that there are no type holes

* Kind `* => *`: `List[A]`, `Set[A]`, `Option[A]`, `Try[A]`, ...

  * So `* => *` means that there is one type hole

* Kind `* => * => *`: `Map[A, B]`, `A => B`

  * So `* => * => *` means that there are two type holes

---

### Kinds as type-level functions

Consider this example, a very basic ADT:

```scala
sealed trait Boolean

case object True  extends Boolean
case object False extends Boolean
```

```haskell
data Boolean = True | False
```

We say that `Boolean` is a type constructor and `True` and `False` the data
constructors of the `Boolean` type

---

### Kinds as type-level functions

The term "type constructor" is not coincidental, as `Boolean` does not return a
value, but a type

  ```scala
  val test = Boolean  // Illegal

  type test = Boolean // OK!
  ```

So, `Boolean` itself can be seen as a 0-arity type-level function.

This concept is expressed by its kind `*` (read it as `() => *`)

---

### Kinds as type-level functions

Now, consider Scala's `Option` ADT:

```scala
sealed trait Option[+A]

case class  Some[+A](value: A) extends Option[A]
case object None               extends Option[Nothing]
```

```haskell
-- Option in Haskell is called Maybe
data Maybe a = Just a | Empty
```

Again, `Option` is a type constructor, but both `Some` and `None` would be data
constructors

---

### Kinds as type-level functions

The same kind of reasoning applies:

```scala
val maybeWhat = Option      // Illegal
val maybeInt  = Option[Int] // Illegal

type maybeWhat = Option      // Illegal
type maybeInt  = Option[Int] // OK!
```

`Option` itself can be seen as a 1-arity type-level function. If we provide it
a complete type (like `Int` in the example), it will return another type

This concept is expressed by its kind `* => *`

---

### Types of a higher kind

* The most common generic type systems (Java, Kotlin, C#, TypeScript, etc) only
  have support for types of kind `*`

* Scala parametric type system, on the other hand, has support for **kinds
  higher than `*`**

* This feature is commonly known as HKTs (Higher Kinded Types), and is one of
  the most powerful features of Scala

---

### Types of a higher kind

```scala
class Foo[A]
```

`Foo` is a generic class over a type `A`

This type `A` needs to always have kind `*`

```scala
new Foo[Int]    // OK!
new Foo[String] // OK!

new Foo[List]   // KO!
new Foo[Option] // KO!
```

---

### Types of a higher kind

In Scala, though, we can make the generic type to have a **higher kind**:

```scala
class Bar[T[_]]
```

Now, this `Bar` is generic over a type `T` of kind `* => *`

```scala
new Bar[List]   // OK!
new Bar[Option] // OK!

new Bar[Int]    // KO!
new Bar[String] // KO!
```

---

### Types of a higher kind

And as expected, it can be made generic over kinds with multiple holes:

```scala
class Baz[T[_, _, _, _, _]]
```

Now, `Baz` is generic over a type `T` with 5 holes

`T` has kind `* => * => * => * => * => *`

<small>This smells like a big over-abstraction, don't do it at work</small>

---

### Types of a higher kind

Consider these definitions:

```scala
trait Combinable[T[_]] {
  def combine[A](t1: T[A], t2: T[A]): T[A]
}

object ListCombinable extends Combinable[List] {
  def combine[A](l1: List[A], l2: List[A]): List[A] = l1 ++ l2
}

object SetCombinable extends Combinable[Set] {
  def combine[A](s1: Set[A], s2: Set[A]): Set[A] = s1.union(s2)
}
```

<small>Combinable is named in FP circles as
[Semigroup](https://typelevel.org/cats/typeclasses/semigroup.html), in
reference to the [mathematical algebraic
structure](https://en.wikipedia.org/wiki/Semigroup)</small>

---

### Types of a higher kind

Now, abstracting over combinable things we could have the following:

```scala
def combineInts[T[_]](fst: T[Int], snd: T[Int], combiner: Combinable[F]): T[Int] =
  combiner.combine(fst, snd)

val listOne = List(1, 2, 3)
val listTwo = List(2, 3, 4)
combineInts(listOne, listTwo, ListCombinable)

val setOne = Set(1, 2, 3)
val setTwo = Set(2, 3, 4)
combineInts(setOne, setTwo, SetCombinable)
```

---

### Higher Kinds and Parametricity

In the previous definition, `combineInts` won't be able to create a new `T`,
so there are only six possible implementations:

```scala
def combineInts[F[_]](fst: T[Int], snd: T[Int], combiner: Combinable[F]): T[Int] =
// 1: fst
// 2: snd
// 3: combiner.combine(fst, fst)
// 4: combiner.combine(fst, snd)
// 5: combiner.combine(snd, snd)
// 6: combiner.combine(snd, fst)
```

Using `List` instead of `T` would make the solution space infinite

<small>[Rúnar Bjarnason - Constraints liberate, liberties constrain
(2015)](https://www.youtube.com/watch?v=GqmsQeSzMdw)</small>

---

### Parametric Polymorphism

Parametric (aka generic) types not only provide a way to avoid code
duplication, but also enable a reasoning method based upon parametricity

Higher kinded type systems provide a way to abstract over type constructors.
The more advanced FP concepts not only build upon this capability, but actually
require its existence
