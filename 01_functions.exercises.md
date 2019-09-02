---
title: 1. Functions
subtitle: Functional Programming in Scala
---

### Exercise 1

Discuss if the following functions are pure or not

```scala
def plus(a: Int, b: Int): Int = a + b

def div(a: Int, b: Int): Int =
  if (b == 0) sys.error("Cannot divide by 0") else a / b

def boolToInt(b: Boolean): Int =
  if (b) 5 else Random.nextInt() / 2

val pi = 3.14
def circleArea(radius: Double): Double = radius * radius * pi
```

---

### Exercise 2

Discuss if the following functions is pure or not

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

TODO:

- Convert
