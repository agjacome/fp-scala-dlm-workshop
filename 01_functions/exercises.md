---
title: 1. Functions
subtitle: Functional Programming in Scala
---

### Exercise 1

Discuss if the following functions are pure or not

```scala
def plus(a: Int, b: Int): Int = a + b
```

```scala
def div(a: Int, b: Int): Int =
  if (b == 0) sys.error("Cannot divide by 0") else a / b
```

```scala
def boolToInt(b: Boolean): Int =
  if (b) 5 else Random.nextInt() / 2
```

```scala
val pi = 3.14
def circleArea(radius: Double): Double = radius * radius * pi
```

---

### Exercise 2

Discuss if the following function is pure or not

```scala
def sumNumbersBetween(start: Int, end: Int): Int = {
  var acc = 0

  var curr = start
  while (curr < end) {
    acc += curr
    curr += 1
  }

  acc
}
```

---

### Exercise 3

Use the tools explained in the session to create a `conditionally` function
that behaves similarly to a native `if` expression:

```scala
// this will print only "is true"
conditionally(true)(
  println("is true"),
  println("is false")
)
```

```scala
// this will print only "is false"
conditionally(false)(
  println("is true"),
  println("is false")
)
```

---

### Exercise 3

Consider the following syntax to the previous `conditionally` function:

```scala
conditionally(true) {
  println("is true")
} {
  println("is false")
)
```

What are the differences, from a semantic point of view, between this newly
created function and a native `if` expression?

---

### Exercise 4

Implement `curry2`, a function that given a non-curried 2-arity function,
transforms it to its curried version, and its reverse `uncurry2` function:

```scala
def curry2(f: (Int, Int) => Int): Int => Int => Int
```

```scala
def uncurry2(f: Int => Int => Int): (Int, Int) => Int
```

---

### Exercise 5

Implement `compose`, a function that is capable of composing two functions
together:

```scala
def compose(g: Double => String, f: Int => Double,): Int => String
```

```scala
// example usage:
val intToDouble: Int => Double    = i => i.toDouble
val doubleToStr: Double => String = d => s"The number is ${d}"

val intToStr: Int => String = compose(doubleToStr, intToDouble)
```

---

### Exercise 6

How would you refactor the following program in order to make it referentially
transparent?

```scala
var myCurrentBudget: Double = 1500.0

def discountPDBExpenses(expenses: Array[(String, Double)]): String = {
  for (expense <- expenses) {
    val expenseValue = expense._2

    if (myCurrentBudget < expenseValue)
        sys.error("Not enough PDB left for you")

    myCurrentBudget -= expenseValue
  }

  s"Money waiting to be spent: ${myCurrentBudget}"
}
```

```scala
// example client code:
println(discountPDBExpenses(Array("Meetup in Japan" -> 900.0, "Curry On" -> 500.0)))
println(discountPDBExpenses(Array("Lambda World" -> 250.0)))
```
