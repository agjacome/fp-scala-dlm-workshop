---
title: 2. Algebraic Data Types
subtitle: Functional Programming in Scala
---

### Let's do some counting

* How may values are there for the type **Boolean**?

* `Boolean = { true, false }`

* Boolean has only **2** possible **legal values**

---

### Let's do some counting

* How may values are there for the type **Byte**?

* `Byte = [ -128, 127 ]`

* Byte has **2^8** possible **legal values**

---

### Let's do some counting

* How may values are there for the type **Int**?

* `Int = [ -2147483648, 2147483647 ]`

* Int has **2^32** possible **legal values**

---

### Let's do some counting

* How may values are there for the type **String**?

* String is a sequence of UTF-16 characters

* _Theoretical_ length limit 2147483647 characters

* String has **MANY** possible **legal values**

---

### Cardinality of Types

* In mathematics, the cardinality of a set is defined as the number of elements
  of the set

* For example, `S = { a, b, c, d }` has cardinality `|S| = 4`

* Similarly, we define **cardinality** of a type as the **number of its possible
  legal values**

* For example, `|Boolean| = 2`, and `|Byte| = 2^8`

---

### Basic counting: One

* Scala has a special type called **Unit** (name not coincidental)

* Unit is a **singleton type**, meaning that there is only one value of that
  type

* `Unit = { () }`

* `|Unit| = 1`

---

### Basic counting: One

There is only one **legal** way to create a value of type `Unit`

```scala
val anUnitValue: Unit = ()
```

---

### Basic counting: Zero

* Scala has a another special type called **Nothing**

* Nothing is an **uninhabited type**, there does not exist any legal value for it

* `Nothing = ∅`

* `|Nothing| = 0`

* Also: `Nothing` is Scala's **bottom type**: it is a subtype of _all_ types

---

### Basic counting: Zero

There is no **legal** way to create a value of type `Nothing`:

```scala
val aNothingValue: Nothing = /* CANNOT PUT ANYTHING REASONABLE HERE */
```

---

### Basic counting: Infinite

* The counterpart of `Nothing` in Scala is `Any`

* Any is Scala's **top type**: it is a supertype of _all_ types

* This fact implies that a type Any can hold all possible Scala values

* `Any = { ..., 1, 42.5, false, "asd", Instant.now(), ... }`

* `Any = U`

* `|Any| = ∞`

---

### Basic counting: Infinite

**Every** value is a legal value for `Any`:

```scala
val anAny_1: Any = 1
val anAny_2: Any = 325.781
val anAny_3: Any = ExternalLibraryInstanceFactory.create()
val anAny_4: Any = JigsawProfile(anId, aName)
val anAny_5: Any = new IllegalArgumentException()
```

---

### Dynamically typed languages

* Statically typed languages use different types to restrict the possible
  values that an expression can take

* Dynamically typed languages are just static languages with a single top-level
  type

* Using only Scala's `Any` type (or `java.lang.Object`, or `System.Object`,
  ...) is the same as using a dynamically typed language

---

### Dynamically typed languages

Scala written _à la_ Dynamic Language (and with impure functions):

```scala
def increment(value: Any): Any =
  value match {
    case i: Int    => i + 1
    case d: Double => d + 1.0
    case s: String => s + "1"
  }

increment(5)     // res: Any = 6
increment(42.3)  // res: Any = 43.3
increment("asd") // res: Any = "asd1"

increment(Instant.now()) // MatchError
```

---

### Dynamically typed languages

Scala written _à la_ Dynamic Language (and with impure functions):

```scala
def increment(value: Any): Any = {
  if      (value.isInstanceOf[Int]   ) value.asInstanceOf[Int]    + 1
  else if (value.isInstanceOf[Double]) value.asInstanceOf[Double] + 1.0
  else if (value.isInstanceOf[String]) value.asInstanceOf[String] + "1"
  else                                 throw new MatchError()

increment(5)     // res: Any = 6
increment(42.3)  // res: Any = 43.3
increment("asd") // res: Any = "asd1"

increment(Instant.now()) // MatchError
```

---

### Function types cardinality

* Functions, because they are values, have a type just for themselves

* A type `A => B` denotes a function that takes `A` as input and produces `B`
  as output

* As any other type, there is a legal number of values that a function type can
  take

* The number of legal values for a function are the possible *pure*
  implementations of that function

---

### Function types cardinality

All possible implementations for a function `Boolean => Boolean`:

```scala
val alwaysTrue:  Boolean => Boolean = (b: Boolean) => true
val alwaysFalse: Boolean => Boolean = (b: Boolean) => false
val identity:    Boolean => Boolean = (b: Boolean) => b
val negation:    Boolean => Boolean = (b: Boolean) => !b
```

All possible implementations for a function `Boolean => Unit`:

```scala
val uniquePureImpl: Boolean => Unit = (b: Boolean) => ()
```

All possible implementations for a function `Unit => Boolean`:

```scala
val alwaysTrue:  Unit => Boolean = (u: Unit) => true
val alwaysFalse: Unit => Boolean = (u: Unit) => false
```

---

### Function types cardinality

* The cardinality of `A => B` is the cardinality of `B` to the power of the
  cardinality of `A`

* Symbolically: `|A => B| = |B| ^ |A|`

* `|Boolean => Boolean| = 2 ^ 2 = 4`

* `|Boolean => Unit| = 1 ^ 2 = 1`

* `|Unit => Boolean| = 2 ^ 1 = 2`

---

### Why should I care about this cardinality thing?

* Helps to reason about what things can a function do

* It determines if a function is implementable (or reasonable) at all:

  * `|String => Nothing| = 0 ^ many = 0`

  * `|Any => Any| = ∞ ^ ∞`

* The cardinality of the input establishes the exact amount of tests needed to
  **completely** verify that a function is correct

  * `Boolean => Something` implies that 2 tests are needed

  * `Int => Something` implies that 2^32 tests are needed

---

### Multi-argument functions

* The cardinality of a multi-argument function can be derived from its curried
  version

  * `|(A, B) => C| = |A => B => C| = (|C| ^ |B|) ^ |A|`

  * `|(Int, Unit) => Boolean| = |Int => Unit => Boolean| = (2 ^ 1) ^ (2^32)`

* The other possibility is seeing the input arguments as a Tuple

---

### Tuples

Scala has native support for Tuples

```scala
val pair: (Int, Int) = (12, 25)
val triplet: (Int, String, Instant) = (1, "asd", Instant.now())

val the1stValueInThePair: Int = pair._1
val the2ndValueInThePair: Int = pair._2

val the1stValueInTheTriplet: Int = triplet._1
val the2ndValueInTheTriplet: Int = triplet._2
val the3rdValueInTheTriplet: Int = triplet._3
```

For ~~random~~ historical reasons, the total limit of elements in Scala Tuples is **22***

<small>* The same restriction, not coincidentally, applies to function arguments</small>

---

### Tuples

* Tuples are a special case of the general concept of **product types**

* The cardinality of a product type is the product of the cardinality of its
  elements

* `|(A, B)| = |A| * |B|`

* `|(Int, Int)| = 2^32 * 2^32 = 2^64`

---

### Product types

* Product types have a direct correspondence with the `cardinal product`
  mathematical set operation

* Given a type `A` (equiv. set `A`) and a type `B` (equiv. set `B`):

* The product type `(A, B)` is then equivalent to the set `A × B`

---

### Multi-argument functions as tuples

* Instead of the curried version, cardinality of a multi-argument function can
  be calculated as:

  * `|(A, B) => C| = |C| ^ |(A, B)| = |C| ^ (|A| * |B|)`

  * `|(Int, Unit) => Boolean| = |Boolean| ^ (|Int| * |Unit|) = 2 ^ (2^32 * 1)`

---

### Curried and uncurried function equivalence

* `|(A, B) => C| = |C| ^ (|A| * |B|)`

* `|A => B => C| = (|C| ^ |B|) ^ |A|`

* The power rules of exponents: `(a ^ m) ^ n = a ^ (m * n)`
  <small>Rules of exponents review: [mesacc.edu/~scotz47781/mat120/exponents](http://www.mesacc.edu/~scotz47781/mat120/notes/exponents/review/review.html)</small>

* **Curried and uncurried functions are always equivalent**

---

### Classes

* Classes, from the perspective of cardinality, are just **product types**

* The cardinality of a class is determined by the cardinality of all its
  fields, multiplied together

* Example:

  * Given `class JigsawProfile(val id: Int, val name: String)`

  * `|JigsawProfile| = |Int| * |String| = 2^32 * many`

---

### Methods

Methods are just functions that take a `this` argument of the type they are
defined on

```scala
class JigsawProfile(val id: Int, val name: String) {

  def isAssignableToProject(projectId: Int): Boolean = ???

}
```

```scala
class JigsawProfile(val id: Int, val name: String)

val isAssignableToProject: (JigsawProfile, Int) => Boolean =
    (this: JigsawProfile, projectId: Int) => ???
```

---

### Methods

* Cardinality of a method is thus the same one of a function taking a `this`
  argument

* Example, given previous definition of `JigsawProfile`:

  * `|JigsawProfile.isAssignableToProject|`  
    `= |Boolean| ^ |(JigsawProfile, Int)|`  
    `= 2 ^ ((2^32 * many) * 2^32)`  
    `= 2 ^ (2^64 * many)`

---

### Methods

* The number of tests needed to completely verify a method are, like functions,
  the same as the cardinality of its input (now counting `this` too).

* Then, to completely verify that `isAssignableToProject` works as intended,
  `(2^64 * many)` tests will be required

* This is _obviously_ feasible

  <center>![](/resources/images/02_adts.uncle_bob_exhibit_01.png)</center>

---

### More basic arithmetic: Sums

* Beside product types, Scala also has support for **sum types**

* The canonical example for sum types is **Either**

* The `Either` datatype has two possibilities: `Left` or `Right`

---

### Either

`Either` values can be **either** left or right, but never both at the same
time

```scala
// type aliases, Name is the same as String and JigsawId is the same as Int
type Name     = String
type JigsawId = Int

val nameOrJisgsawID_1: Either[Name, JigsawId] = Left("Rebecca Parsons")
val nameOrJisgsawID_2: Either[Name, JigsawId] = Right(10196)
```

---

### Either

* The cardinality of Either is the cardinality of their Left and Right sides,
  added together

* Symbolically: `|Either[L, R]| = |L| + |R|`

* `|Either[Byte, Boolean]| = 2^8 + 2`

* `|Either[Nothing, Int]| = 0 + 2^32`

* `|Either[Unit, Int]| = 1 + 2^32`

---

### What's Either useful for?

Either is mostly used for preserving purity while signaling errors

```scala
def parseAsJson(rawInput: String): Json =
  if (Json.isValid(rawInput)) {
    new Json(rawInput)
  } else {
    throw new IllegalArgumentException("Invalid JSON")
  }

parseAsJson("""{ "field": "valid" }""") // OK, returns Json value
parseAsJson("""gArb@ge""")              // KO, throws exception
```

```scala
type ErrorType = String

def parseAsJson(rawInput: String): Either[ErrorType, Json] =
  if (Json.isValid(rawInput)) {
    Right(new Json(rawInput))
  } else {
    Left("Invalid JSON")
  }

parseAsJson("""{ "field": "valid" }""") // OK, returns Right[Json] value
parseAsJson("""gArb@ge""")              // OK, returns Left[ErrorType] value
```

---

### Sum types

* Sum types have a direct correspondence with the `union` mathematical set
  operation

* Given a type `A` (equiv. set `A`) and a type `B` (equiv. set `B`):

* The sum type `A | B` is then equivalent to the set `A ⋃ B`

---

### Option

Option is the other canonical sum type in Scala, it can be *either* `Some` or
`None`

```scala
type Gender = Option[String]

val gender_1: Gender = Some("Cis Female")
val gender_2: Gender = Some("Trans Male")
val gender_3: Gender = Some("Fluid")
val gender_4: Gender = None
```

---

### Option

* The number of legal values for a type `Option[A]` is the total number of
  legal values of `A` (the `Some` case) plus one (the `None` case).

* Symbolically: `|Option[A]| = |A| + 1`

* `|Option[Boolean]| = 2 + 1 = 3`

* `|Option[Unit]| = 1 + 1 = 2`

* `|Option[Nothing]| = 0 + 1 = 1`

* `|Option[Byte]| = 2^8 + 1 = 257`

---

### What is Option useful for?

Option is generally used to signal the abscence of a valid value* while
preserving purity:

```scala
val divide: Int => Int => Float = x => y => x / y

divide(10)(5) // OK, returns Float value
divide(10)(0) // KO, throws java.lang.ArithmeticException
```

```scala
val divide: Int => Int => Option[Float] = x => y => if (y == 0) None else Some(x / y)

divide(10)(5) // OK, returns Some[Float] value
divide(10)(0) // OK, returns None value
```
<small>
* Which in other languages or scenarios would be solved with "null"  
- [Null references: the billion dollar mistake](https://www.infoq.com/presentations/Null-References-The-Billion-Dollar-Mistake-Tony-Hoare/)  
- [Null vs pure reason](https://apocalisp.wordpress.com/2008/05/03/null-vs-pure-reason/)
</small>

---

### Enums

* Most modern languages have the concept of `Enum` type

* In the vast majority of scenarios, they can be seen as sum types. See C#
  for example:

  ```csharp
  enum WeekDay { Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday }
  ```

* This defines a sum type called `WeekDay` with 7 possible legal
  values. Each variable of the type `WeekDay` can only be one of those.

* But underneath they tend to be just aliases over plain integers:

  ```csharp
  WeekDay foo = Enum.ToObject(typeof(WeekDay),  0); // OK, foo will be Monday
  WeekDay bar = Enum.ToObject(typeof(WeekDay), 10); // KO, will throw at runtime
  ```

---

### Sum types as subclasses

Another common way to create a similar concept to sum types is just using subclasses

```scala
trait WeekDay

final class Monday    extends WeekDay
final class Tuesday   extends WeekDay
final class Wednesday extends WeekDay
final class Thursday  extends WeekDay
final class Friday    extends WeekDay
final class Saturday  extends WeekDay
final class Sunday    extends WeekDay

val hopefullyToday: WeekDay = new Friday
```

But it cannot prevent that new subclasses are created in any other place,
extending the limit of possible values and breaking our beloved reasoning

```scala
class IBrokeYourCalendar extends WeekDay

val annualReviewDay: WeekDay = new IBrokeYourCalendar
```

---

### Sealed traits

To solve this problem, Scala includes the concept of `sealed` traits (or
classes). The `sealed` keyword prevents that new "extensions" are added outside
the defining file.

```scala
// --- file: WeekDay.scala
sealed trait WeekDay

final class Monday    extends WeekDay
final class Tuesday   extends WeekDay
final class Wednesday extends WeekDay
final class Thursday  extends WeekDay
final class Friday    extends WeekDay
final class Saturday  extends WeekDay
final class Sunday    extends WeekDay

// --- file: Hacker.scala
class IBrokeYourCalendar extends WeekDay // THIS WONT COMPILE
```

---

### Sealed traits as Sum Types

* The previously defined `sealed trait WeekDay` is a complete sum type on its
  own

* There are only those 7 possible values for the type `WeekDay`

* No integer is backing up the representation, so no way to convert an `Int` to
  a `Weekday`

* `WeekDay = { Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday }`

* `|WeekDay| = 7`

* Custom sum types can be easily created in Scala!

---

### Scala Objects

* Scala has embedded in the language the concept of "singleton" objects

* Scala objects do not have constructors nor require `new` invocations

* The syntax to use them is similar to native Literal values

    * Like `true` instead of `new True()`
    * Or `53.2` instead of `new 53.2()`

* Objects are also `final` by default, they cannot be extended

* Unlike `class`, they don't define a type automatically (makes sense)

  ```scala
  object Foo

  val foo_1: Foo      // DOES NOT COMPILE: Foo is not a type
  val foo_2: Foo.type // COMPILES: Object types are defined in their type field
  ```

---

### Scala Objects

With `object`, the `WeekDay` sum type can be re-written as follows:

```scala
sealed trait WeekDay

object Monday    extends WeekDay
object Tuesday   extends WeekDay
object Wednesday extends WeekDay
object Thursday  extends WeekDay
object Friday    extends WeekDay
object Saturday  extends WeekDay
object Sunday    extends WeekDay

val labourDays: List[WeekDay] = List(Monday, Tuesday, Wednesday, Thursday, Friday)
val partyDays: List[WeekDay]  = List(Friday, Saturday)
```

---

