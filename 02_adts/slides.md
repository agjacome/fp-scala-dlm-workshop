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

---

### Function types cardinality

All possible implementations for a function `Boolean => Unit`:

```scala
val uniquePureImpl: Boolean => Unit = (b: Boolean) => ()
```

---

### Function types cardinality

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

---

### Why should I care about this cardinality thing?

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

### Product types

* Given sets `A = { a, b }` and set `B = { 0, 1 }`

  * Then `A × B = { (a, 0), (a, 1), (b, 0), (b, 1) }`

  * And `|A × B| = |A| * |B| = 2 * 2 = 4`

* Similary, given `Boolean = { true, false }` and `Bit = { 0, 1 }`

  * Then `(Boolean, Bit) = { (true, 0), (true, 1), (false, 0), (false, 1) }`

  * And `|(Boolean, Bit)| = |Boolean| * |Bit| = 2 * 2 = 4`

---

### Multi-argument functions as tuples

* A list of multiple arguments in a function can be seen as tuple

* Then the cardinality of a function can be computed as the cardinality of the
  output to the cardinality of a tuple with all the input arguments:

  * Symbolically: `|(A, B) => C| = |C| ^ |(A, B)| = |C| ^ (|A| * |B|)`

* Example:

  `|(Int, Unit) => Boolean| = |Boolean| ^ (|Int| * |Unit|) = 2 ^ (2^32 * 1)`

---

### Curried and uncurried function equivalence

* Uncurried version: `|(A, B) => C| = |C| ^ (|A| * |B|)`

* Curried version: `|A => B => C| = (|C| ^ |B|) ^ |A|`

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

val isAssignableToProject: (JigsawProfile, Int) => Boolean = ???
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

* This is _obviously_ feasible x / y

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

* `|Either[String, Int]| = many + 2^32`

---

### What is Either useful for?

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

---

### What is Either useful for?

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

### Sum types

* Given sets `A = { a, b }` and set `B = { 0, 1 }`

  * Then `A ⋃ B = { a, b, 0, 1 }`

  * And `|A ⋃ B| = |A| + |B| = 2 + 2 = 4`

* Similary, given `Boolean = { true, false }` and `Bit = { 0, 1 }`

  * Then `Boolean | Bit = { true, false, 0, 1 }`

  * And `|Boolean | Bit| = |Boolean| + |Bit| = 2 + 2 = 4`

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
[Null references: the billion dollar mistake](https://www.infoq.com/presentations/Null-References-The-Billion-Dollar-Mistake-Tony-Hoare/)  
[Null vs pure reason](https://apocalisp.wordpress.com/2008/05/03/null-vs-pure-reason/)
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

But new subclasses can be created in any other place, making the sum type
completely open

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

* Objects do not have constructors nor require `new` invocations

* Objects are also `final` by default, they cannot be extended

* Simple usage syntax is similar to native Literal values

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

### Scala Objects

Bear in mind that `object` does not define a type alongside its declaration

```scala
object Something

val x: Something // WON'T COMPILE: type Something does not exist
```

If the type is actually needed (think twice), it is available in the `.type`
field of the object

```scala
object Something

val x: Something.type = Something // COMPILES: actuall type is defined in type field
```

This is in fact useful: we don't usually want to create a new type for each
possible sum type value, just for the sum type itself

---

### FP terminology: ADTs

* Sums and products are usually referred by the name of **Algebraic Data
  Types** (or ADTs)

  <small>[Wikipedia: Algebraic Data Type](https://en.wikipedia.org/wiki/Algebraic_data_type)</small>

* Algebraic means here that they are created by "algebraic" operations:

  * Sum is _alternation_: `A | B` meaning either `A` or `B` but not both

  * Product is _combination_: `(A, B)` meaning `A` and `B` together

---

### FP terminology: ADTs

* The term **constructor** tends to appear in ADT literature a lot, and not
  always with the same meaning as an object-oriented class constructor

* Given this example of a sum type:

  ```scala
  sealed trait Boolean

  object True  extends Boolean
  object False extends Boolean
  ```

* **Type constructor** refers to the top-level type, in this case `Boolean`

* **Data constructor** refers to the possible ADT values, in this case `True`
  and `False`

---

### Non-nullary data constructors

* Data constructors can take arguments too. For this we just use classes instead
  of objects

  ```scala
  sealed trait Shape

  final class Circle(val radius: Int)                    extends Shape
  final class Rectangle(val width: Int, val height: Int) extends Shape
  ```

* This creates some new hybrid types that combine both Sums and Products

* The total `Shape` cardinality then is a bit more complicated than just `2`:

  `|Shape| = |Circle| + |Rectangle| = (|Int|) + (|Int| * |Int|) = 2^32 + 2^64`

* If the data constructor classes are not marked `final`, problems may arise

---

### Pattern matching

* **Pattern matching** is a mechanism for checking a value against a pattern

* Example of a simple pattern-matching expression in Scala

  ```scala
  val aNumber: Int = Random.nextInt

  aNumber match {
    case 0 => "zero"
    case 1 => "one"
    case 2 => "two"
    case _ => "other"
  }
  ```

* The `aNumber` value will be checked for a match to each one of the possible
  _alternatives_

* The `case _`* expression is a catch-all case that serves as a fallback

  <center><small>* Yet another meaning for the `_` operator</small></center>

---

### Case classes and pattern matching

* Pattern matching expressions can also deconstruct a value into its
  constituent parts

* For this to work automatically*, we need to mark the types that we want to
  match upon as `case` (either class or object)

  <small>* There is no magic, see the not automatic way to do it: [extractor objects](https://docs.scala-lang.org/tour/extractor-objects.html)</small>

* Case classes have extra benefits

  * No `new` keyword needed for creating instances of them

  * No `val` needed to define its fields, are all `val` by default

  * Automatic `equals`, `hashCode` and `toString` method implemented using all
    fields

  * Automatic `copy` method implemented to create shallow copies

---

### Case classes and pattern matching

```scala
sealed trait Shape

final case class Circle(radius: Int)                extends Shape
final case class Rectangle(width: Int, height: Int) extends Shape
```

```scala
def computeArea(shape: Shape): Int =
  shape match {
    case Circle(r)       => Math.PI * r * r
    case Rectangle(w, h) => w * h
  }
```

```scala
val aCircle: Shape    = Circle(42)
val aRectangle: Shape = Rectangle(10, 5)

val area_1 = computeArea(aCircle)
val area_2 = computeArea(aRectangle)
```

---

### Pattern matching as just a visitor pattern

The classical OO implementation of `computeArea` can be a [Visitor
pattern](https://sourcemaking.com/design_patterns/visitor)

```scala
trait ShapeToIntVisitor {
  def visit(circle: Circle): Int
  def visit(rectangle: Rectangle): Int
}
```

```scala
trait Shape {
  def accept(visitor: ShapeToIntVisitor): Int
}

final class Circle(val radius: Int) extends Shape {
  override def accept(visitor: ShapeToIntVisitor): Int =
    visitor.visit(this)
}

final class Rectangle(val width: Int, val height: Int) extends Shape {
  override def accept(visitor: ShapeToIntVisitor): Int =
    visitor.visit(this)
}
```

---

### Pattern matching as just a visitor pattern

The `AreaShapeVisitor` here does the same thing as the previous match
expression

```scala
final class AreaShapeVisitor extends ShapeToIntVisitor {

 override def visit(circle: Circle): Int =
   Math.PI * circle.radius * circle.radius

 override def visit(rectangle: Rectangle): Int =
   rectangle.width * rectangle.height

}

def computeArea(shape: Shape): Int =
  (new AreaShapeVisitor).visit(shape)
```

```scala
val aCircle: Shape    = new Circle(42)
val aRectangle: Shape = new Rectangle(10, 5)

val area_1 = computeArea(aCircle)
val area_2 = computeArea(aRectangle)
```

---

### Evolving the Visitor pattern with HOFs

The classical Visitor can be encoded also with higher-order functions

```scala
trait Shape {
  def visit(visitCircle: Circle => Int, visitRectangle: Rectangle => Int): Int
}


final class Circle(val radius: Int) extends Shape {
  override def visit(visitCircle: Circle => Int, visitRectangle: Rectangle => Int): Int
    visitCircle(this)
}

final class Rectangle(val width: Int, val height: Int) extends Shape {
  override def visit(visitCircle: Circle => Int, visitRectangle: Rectangle => Int): Int
    visitRectangle(this)
}
```

---

### Evolving the Visitor pattern with HOFs

Here, the `computeArea` function looks a lot more like the match expression

```scala
def computeArea(shape: Shape): Int =
  shape.visit(
    circle    => Math.PI * circle.radius * circle.radius,
    rectangle => rectangle.width * rectangle.height
  )
```

```scala
val aCircle: Shape    = new Circle(42)
val aRectangle: Shape = new Rectangle(10, 5)

val area_1 = computeArea(aCircle)
val area_2 = computeArea(aRectangle)
```

<small>This way to encode a visitor or a match expression as a list of HOFs is
known as a [Church encoding](https://en.wikipedia.org/wiki/Church_encoding) or
[Scott encoding](https://en.wikipedia.org/wiki/Mogensen%e2%80%93Scott_encoding)</small>

---

### Pattern Matching vs Church Encoding vs Visitor

* Pattern matching and Church-encoding couple the client code to all the
  possible data constructors of a type

* Pattern matching requires language level support

* Church-encoding requires higher order function language support

* **There is no silver bullet**, use the tool that better fits your use case

* **Simpler doesn't mean easier**: sometimes the harder to program `Visitor` is
  the simplest solution

---

### Exhaustiveness

Pattern matching expressions can be non-exhaustive and produce a `MatchError`
runtime exception without any typecheck or compiler error

```scala
trait Month

case object January  extends NaturalNumber
case object February extends NaturalNumber
case object March    extends NaturalNumber
case object April    extends NaturalNumber
case object May      extends NaturalNumber
// etc
```

```scala
def days(m: Month): Int =
  m match {
    case January | March | May | July | August | October | December => 31
    case April | June | September | November                        => 30
  }

days(April)    // OK, returns 30
days(December) // OK, retursn 31
days(February) // KO, throws MatchError exception
```

---

### Exhaustiveness

The way to solve it is simple, and the same one to prevent spurious data
constructors created in other files, make the type constructor **sealed**

```scala
sealed trait Month

case object January  extends NaturalNumber
case object February extends NaturalNumber
// etc
```

The following won't compile now, because the match expression is not exhausive.
And the compiler error is going to provide information about which case is not
handled (here, `February`)

```scala
def days(m: Month): Int =
  m match {
    case January | March | May | July | August | October | December => 31
    case April | June | September | November                        => 30
  }
```

The only way to make the compiler check for exhaustiveness in Scala 2 is with
`sealed`

---

### Making illegal states unrepresentable

One of the end goals of ADTs is to **reduce the solution space**

```scala
def countryToCurrency(country: String): String =
  country match {
    case "Germany" | "Spain" | "France" => "Euro"
    case "United Kingdom"               => "British Pound"
    case "United States"                => "US Dollar"
    case _                              => "Unknown"
  }
```

* What is the cardinality of this function?

* Without diving into the implementation, what does the function type tell
  about it?

* How many tests would be required to completely verify that it is correct?

---

### Making illegal states unrepresentable

Now suppose the following definitions

```scala
sealed trait Country

object Country {

  case object France        extends Country
  case object Germany       extends Country
  case object Spain         extends Country
  case object UnitedKingdom extends Country
  case object UnitedStates  extends Country

}
```

```scala
sealed trait Currency

object Currency {

  case object BritishPound extends Currency
  case object Euro         extends Currency
  case object USDollar     extends Currency

}
```

---

### Making illegal states unrepresentable

* How much better would be if we reduce just the input of the previous function?

  ```scala
  def countryToCurrency(country: Country): String
  ```

* What about just the output?

  ```scala
  def countryToCurrency(country: String): Option[Currency]
  ```

* What about both input and output at the same time?

  ```scala
  def countryToCurrency(country: Country): Currency
  ```

---

### Making illegal states unrepresentable

* Types should **exactly** fit the business requirements

* Imprecise data leads to errors and misleading documentation

* Properly typed implementation:

  ```scala
  def countryToCurrency(country: Country): Currency =
    country match {
      case France | Germany | Spain => Euro
      case UnitedKingdom            => BritishPound
      case UnitedStates             => USDollar
    }
  ```

* Nothing of this is new nor FP-related (see [Value
  Objects](https://deviq.com/value-object/)), pure typed FP just makes it more
  obvious and strict

---

### Type isomorphisms

* **Different types**, with completely different shapes, can actually be encoding
  the **same exact information**

* The way to determine if two types are actually the same is looking at their
  cardinality

* Even if two types are semantically the same, one of them may be better suited
  to the use case than the other

* These type equivalences are often referred in FP literature as
  **isomorphisms*** (from [mathematical
  isomorphisms](https://en.wikipedia.org/wiki/Isomorphism))

  <small>* Because we like to use fancy and smart mathematician words</small>

---

### Type isomorphisms

Take the following sum type as an example:

```scala
sealed trait Result

object Result {
  case object Success extends Result
  case object Failure extends Result
}
```

```scala
|Result| = 2

|Option[Unit]| = 1 + 1 = 2

|Either[Unit, Unit]| = 1 + 1 = 2

|Boolean| = 2
```

All of the previous types have the same cardinality, and thus are isomorphic

---

### Type isomorphisms

Isomorphic types can always be converted between them without loss of
information

```scala
def resultToBoolean(result: Result): Boolean =
  result match {
    case Success => 1
    case Faiurel => 0
  }

def booleanToResult(boolean: Boolean): Result =
  if (boolean) Success else Failure
```

```scala
def optionUnitToEitherUnit(optionUnit: Option[Unit]): Either[Unit, Unit] =
  optionUnit match {
    case Some(_) => Right(())
    case None    => Left(())
  }

def eitherUnitToOptionUnit(eitherUnit: Either[Unit, Unit]): Option[Unit] =
  eitherUnit match {
    case Right(_) => Some(())
    case Left(_)  => None
  }
```

---

### Type isomorphisms

Another example of isomorphisms

```scala
|Option[Nothing]| = 0 + 1 = 1

|Either[Nothing, Unit]| = 0 + 1 = 1

|Either[Unit, Nothing]| = 1 + 0 = 1

|(Unit, Unit, Unit)| = 1 * 1 * 1 = 1 

|Unit| = 1
```

The corollaries here would be:

* `Option[Nothing]` can never be `Some`. As it is always `None`, it is the same
  as just `Unit`

* `Either[Nothing, R]` can never be `Left`. As it is always `Right`, it is the
  same as just `R`

* `Either[L, Nothing]` can never be `Right`. As it is always `Left`, it is the
  same as just `L`

---

### The Try datatype

* Scala std library has a sum type called `Try`

* Try has two data constructors: `Success` and `Failure`

* `Try[A]` is isomorphic to `Either[Throwable, A]`

  * `Success[A] = Right[A]`

  * `Failure = Left[Throwable]`

---

### The Try datatype

The main purpose of Try is to purify exception-throwing functions (e.g. from
Java libraries)

```scala
import java.net.URI

def unsafeParseUri(uri: String): URI =
  new URI(uri)

unsafeParseUri("http://thoughtworks.com") // OK, returns an URI
unsafeParseUri("http://")                 // KO, throws URISyntaxException
```

The constructor `Try` handles the exception like a try-catch and returns the
appropriate value

```scala
import java.net.URI
import scala.util.Try

def safeParseUri(uri: String): Try[URI] =
  Try(new URI(uri))

safeParseUri("http://thoughtworks.com") // OK, returns Success[URI]
safeParseUri("http://")                 // OK, returns Failure
```

---

### The Try datatype

The biggest rule of thumb about `Try`: use it **only for handling exceptions**,
and convert it immediately to an `Either` or an `Option`; don't expose `Try` in
public signatures

```scala
def safeParseUri(uri: String): Option[URI] =
  Try(new URI(uri)).toOption
```

```scala
def safeParseUri(uri: String): Either[Throwable, URI] =
  Try(new URI(uri)).toEither
```

Matching on `Throwable` is always non-exhaustive and thus not typesafe

---

### About testing

Suppose the following function

```scala
def divideTenBy(x: Int): Option[Int] =
  if (x != 0) Some(10 / x) else None
```

_Obviously_ testing 2^32 possibilities is not viable

The common classical solution is to test with range values, like (using
ScalaTest):

```scala
class DivideTest extends FunSuite {

  test("dividing by -100") { assert(divideTenBy(-100) == Some( 0)) }
  test("dividing by  -10") { assert(divideTenBy( -10) == Some(-1)) }
  test("dividing by   -2") { assert(divideTenBy(  -2) == Some(-5)) }
  test("dividing by    0") { assert(divideTenBy(   0) == None    ) }
  test("dividing by    2") { assert(divideTenBy(   2) == Some( 5)) }
  test("dividing by   10") { assert(divideTenBy(  10) == Some( 1)) }
  test("dividing by  100") { assert(divideTenBy( 100) == Some( 0)) }

}
```

---

### Parameterized tests

A way to avoid the previous `assert` repetition is to use **parameterized** tets:

```scala
class DivideTest extends FunSuite with TableDrivenPropertyChecks {

  private val values = Table(
    "input" -> "output",
    -2      -> Some(-5),
     0      -> None    ,
     2      -> Some(5),
     // more input -> output pairs here...
  )

  forAll(values) { (input, output) =>
    test(s"dividing by ${input}") { assert(dividTenBy(input) == output) }
  }
}
```

Extending the test scenarios then would be just a matter of adding them to the
`Table`

---

### Property-based testing

* In FP, the most common way to write this kind of tests is to just define
  which **properties** the function must hold

* With those properties, the test scenarios can be auto-generated

* There are multiple tools to do this in Scala, the most used one is
  [ScalaCheck](https://www.scalacheck.org/), but there is also
  [ScalaProps](https://github.com/scalaprops/scalaprops), [Scala
  Hedgehog](https://github.com/hedgehogqa/scala-hedgehog) and a miriad more*

  <small>* All of them copies from Haskell's
  [QuickCheck](https://en.wikipedia.org/wiki/QuickCheck) and
  [Hedgehog](https://hackage.haskell.org/package/hedgehog)</small>

---

### Property-based testing

The same previous test, writen in a **property-based testing** fashion:

```scala
class DivideTest extends PropSpec with GeneratorDrivenPropertyChecks {

  property("dividing by zero returns None") {
    divideTenBy(0) == None
  }

  property("dividing by non-zero number returns Some") {
    forAll { (number: Int) =>
      whenever(number != 0) {
        divideTenBy(number) == Some(10 / number)
      }
    }
  }

}
```

---

### Property-based testing

* The `forAll` expression of the previous example (ScalaTest + ScalaCheck)
  automatically generates `Int` values that get supplied to the test

* Range values are generated automatically, plus some more random ones

* Shrinking to the minimum failure scenario is performed automatically

* Not `2^32` scenarios are going to be generated in each run, but a very big
  number of them will, and without having to write them manually

* Together with a proper solution space restriction, property-based tests can
  make your life easier in terms of verification

---

### Algebraic Data Types

Algebraic data types together with first class functions are the basic building
blocks of modern typed functional programming

Lots of impure functions can become pure, self-documenting and unambiguous with
their help
