---
title: 2. Algebraic Data Types
subtitle: Functional Programming in Scala
---

### Exercise 1

Calculate the cardinality of the following types and try to come up with some
isomorphic types to them

* `(Boolean, Boolean, Boolean, Boolean)`

* `(Int, Int)`

* `Either[Unit, Either[Nothing, Option[Boolean]]]`

* `class Foo(x: Boolean, y: Boolean)`

* `sealed trait UserExistsResult`  
  `case class  UserExistsSuccess(exists: Boolean) extends UserExistsResult`  
  `case object UserExistsFailure                 extends UserExistsResult`

* `Int => UserExistsResult`

---

### Exercise 2

Implement the following `compareChar` function that behaves as described:

```scala
def compareChar(c1: Char, c2: Char): Int = ???

assert(compareChar('a', 'c') == -1)
assert(compareChar('c', 'c') ==  0)
assert(compareChar('c', 'a') ==  1)
```

What do you think of this function? What would you change about it?

---

### Exercise 3

Try to create types that fit exactly the following business requirements:

- An Order contains an id (`java.util.UUID`), a creation timestamp
  (`java.time.Instant`), an order status and a basket of items

- An Order Status is either draft, submitted, delivered or cancelled
 
- An Item consists of an id (UUID), a quantity and a price

- When an Order is in draft status, it may or may not contain a delivery
  address

- When an Order is submitted, it must always have a delivery dddress and a
  submitted timestamp

- When an Order is delivered, it must always have a delivery address, submitted
  timestamp and delivered timestamp

- When an Order is cancelled, it must always contain a cancellation timestamp

- Extra ball: a Basket of items can be empty if the status is in draft, otherwise it must
  contain at least one item

---

### Exercise 4

Given this function signature

```scala
def sign(x: Int): Boolean = ???
```

How many valid function implementations are there?

---

### Exercise 4

Given this function signature

```scala
def sign(x: Int): Boolean = ???
```

How may of them will pass the following test?

```scala
assert(sign(5) == true)
```

---

### Exercise 4

Given this function signature

```scala
def sign(x: Int): Boolean = ???
```

How many for the following set of tests?

```scala
assert(sign(-2) == false)
assert(sign( 0) == true )
assert(sign( 5) == true )
```

---

### Exercise 4

Given this function signature

```scala
def sign(x: Int): Boolean = ???
```

How many of them remain valid if I have the following property?

```scala
forAll { (x: Int) => sign(x) == !sign(-x) }
```
