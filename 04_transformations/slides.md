---
title: 4. Structure-preserving transformations
subtitle: Functional Programming in Scala
---

### Linked Lists

Linked lists are one of the quintessential data structures in the programming
world

In their most basic form, they consist of a collection of nodes containing data
and a link to the next node in the sequence

![](/resources/images/04_transformations.linked_list_01.png)

---

### Linked Lists

The classical implementation in a 1970s imperative language* will look somewhat
like this:

```go
type Node struct {
    Value interface{}
    Next  *Node
}

type List struct {
    Head *Node
}

func (list *List) Prepend(value interface{}) {
    list.Head = &Node{value, list.Head}
}
```

```go
list := List{}
list.Prepend(3)
list.Prepend(2)
list.Prepend(1)

for current := list.Head; current != nil; current = current.Next {
    fmt.Println(current.Value)
}
```

<small>*Example in GO (2012)</small>

---

### Functional Data Structures

* Functional data structures, as expected, are operated using **only pure**
  functions

* Functional data structures need to be, by definition, immutable

* The previous definition of a List in Go is, unsurprinsingly, not functional.

* Even if pure functions could be written to operate upon it, the data
  structure itself does not promote nor prevent the opposite

* If we want to work in a pure functional environment, we need data structures
  that don't work against us

---

### Recursive ADTs

The usual functional linked list definition is based on a recursive Algebraic
Data Type:

```scala
sealed trait List[+A]

case object EmptyList extends List[Nothing]
case class ListElement[+A](head: A, tail: List[A]) extends List[A]
```

```haskell
data List a = EmptyList | ListElement a (List a)
```

This ADT is recursive because the ListElement data constructor contains itself
a List

---

### Recurisve ADTS

The data constructors ListElement and EmptyList in the previous definition are,
more often than not, called Cons and Nil, respectively

```scala
sealed trait List[+A]

case object Nil extends List[Nothing]
case class Cons[+A](head: A, tail: List[A]) extends List[A]
```

```haskell
data List a = Nil | Cons a (List a)
```

These names come from LISP (1958), where lists are created like this:

```lisp
(cons 1 (cons 2 (cons 3 nil)))
```

---

### Recurisve ADTS

The Scala standard library, however, uses a different name for Cons, the symbol
`::`

```scala
sealed trait List[+A]

case object Nil extends List[Nothing]
case class  ::[+A](head: A, tail: List[A]) extends List[A]
```

The choice of the name `::` is for enabling some syntax sugar on creation and
destructuring (pattern matching) of lists. See: [Scala operator
precedence](https://docs.scala-lang.org/tour/operators.html)

---

### Recurisve ADTs

Given the List type, we can create lists with the following syntax:

```scala
val anEmptyList: List[String]       = Nil
val aSingletonList: List[String]    = Cons("a", Nil)
val aMultiElementList: List[String] = Cons("a", Cons("b", Cons("c", Nil)))
```

Or with the Scala standard library syntax:

```scala
val anEmptyList: List[String]       = Nil
val aSingletonList: List[String]    = "a" :: Nil
val aMultiElementList: List[String] = "a" :: "b" :: "c" :: Nil
```

For the sake of making explicit that Lists are recursive, the Cons syntax is
used in these slides

---

### Recursive ADTs

We can now easily define pure functions operating on the List datatype:

```scala
def prepend[A](list: List[A], value: A): List[A] = Cons(value, list)

val xs = Nil
val ys = prepend(xs, 1) // => Cons(1, Nil)
val zs = prepend(ys, 2) // => Cons(1, Cons(2, Nil))
```

---

### Data sharing

The previous prepend function uses a concept derived from functional data
structures: safe data sharing. Since List is completely immutable, no safe copy
is needed to prevent problems

Consider the problematic scenario in a mutable List:

```scala
sealed trait MList[+A]

case object MNil extends MList[Nothing]
case class MCons[+A](var head: A, var tail: MList[A]) extends MList[A]
```

```scala
def prepend[A](list: MList[A], value: A): MList[A] = MCons(value, list)

val xs = MCons(1, MNil)
val ys = prepend(2, xs)
// NOW: xs = MCons(1, MNil), ys = MCons(2, MCons(1, MNil))

xs.head = 42
// NOW: xs = MCons(42, MNil), ys = MCons(2, MCons(42, MNil))
// ys has been mutated by just changing xs
```

---

### Purely functional iteration

We want to compute a sum of all the integers in a list. A possible naive
implementation of such function could be:

```scala
import sala.util.control.Breaks._

def sum(list: List[Int]): Int = {
  var accumulator = 0

  breakable { // necessary to use "break" in Scala
    var current = list

    while (true) {
      current match {
        case Cons(head, tail) => accumulator = accumulator + head; current = tail
        case Nil              => break
      }
    }
  }

  accumulator
}

sum(Cons(1, Cons(42, Cons(100, Nil)))) // => 143
```

This can be considered a pure function by its clients, as it does not produce
any observable effect

---

### Purely functional iteration

But since the datatype itself is recursive, what if we use recursion to iterate
over it instead?

```scala
def sum(list: List[Int]): Int =
  list match {
    case Cons(head, tail) => head + sum(tail)
    case Nil              => 0
  }

sum(Cons(1, Cons(42, Cons(100, Nil)))) // => 143
```

* It works, and is a pure function written without any internal mutability at all

* However, it will completely break with large enough lists

---

### Recursive processes vs iterative processes

Let's try to "draw" how the recursion tree of that previous function will look
like for a List with 3 elements:

```scala
Given Cons(1, Cons(42, Cons(100, Nil))):

sum(Cons(1, Cons(42 Cons(100, Nil))))
  sum(Cons(42, Cons(100, Nil)))
    sum(Cons(100, Nil))
      sum(Nil)
      0
    100 + 0 = 100
  42 + 100 = 142
1 + 142 = 143
```

* Each nested level will create a new Stack frame at the machine level

* As the list grows, also the number of stack frames created

* The JVM will crash with a **StackOverflowError** with big enough lists

* This kind of execution is sometimes referred as _recursive process_

---

### Recursive processes vs iterative processes

Let's draw instead the previous while-loop recursion tree:

```scala
Given Cons(1, Cons(42, Cons(100, Nil))):

sum(Cons(1, Cons(42, Cons(100, Nil))))
  accumulator = 0  ; current = Cons(1, Cons(42, Cons(100, Nil)))
  accumulator = 1  ; current = Cons(42, Cons(100, Nil))
  accumulator = 43 ; current = Cons(100, Nil)
  accumulator = 143; current = Nil
```

* No nesting is present, so there are no new stack frames allocated besides the
  top-level one

* It doesn't matter how long the list is, the JVM will not crash with
  StackOverflow errors

* This kind of execution is sometimes referred as _iterative process_

---

### Recursive processes vs iterative processes

What would be ideal is having the same style of _iterative process_, but
encoded as a recursive function. Let's try this instead:

```scala
def sum(list: List[Int]): Int = {
  def iterate(current: List[Int], accumulator: Int): Int = {
    current match {
      case Cons(head, tail) => iterate(tail, accumulator + head)
      case Nil              => accumulator
    }

  iterate(list, 0)
}

sum(Cons(1, Cons(42, Cons(100, Nil)))) // => 143
```

Instead of iterating over itself and adding up the results of the iteration,
`sum` now uses a helper function that has the accumulated sum as an argument,
addibg up the head to the accumulator before the recursive call happens

---

### Recursive processes vs iterative processes

Unfortunately, in most languages, the recursion tree generated will be almost
the same as before:

```scala
Given Cons(1, Cons(42, Cons(100, Nil))):

sum(Cons(1, Cons(42 Cons(100, Nil))))
  iterate(Cons(1, Cons(42, Cons(100, Nil))), 0)
    iterate(Cons(42, Cons(100, Nil)), 1)
      iterate(Cons(100, Nil), 43)
        iterate(Nil, 143)
        143
      143
    143
  143
143
```

* The same problem at the JVM level will occurr with large enough lists

* But the recursive call is the last meaningful instruction to happen at the
  machine level

* Each recursive call does not need to "wait" for the nested call to complete
  before aggregating the results in the accumulator, it just returns directly

* This kind of recursion is named **tail recursion**, because the recursive
  call happens at the very end of the function (aka tail position)

---

### Tail call optimization

The Scala compiler performs an optimization called **tail call elimination or
optimization** for that kind of recursive functions

Instead of creating a new Stack Frame for each recursive call, the compiler
will replace the existing one. So the actual recursion tree generated by the
tail-recursive version will be this:

```scala
Given Cons(1, Cons(42, Cons(100, Nil))):

sum(Cons(1, Cons(42 Cons(100, Nil))))
  iterate(Cons(1, Cons(42, Cons(100, Nil))), 0)
  iterate(Cons(42, Cons(100, Nil)), 1)
  iterate(Cons(100, Nil), 43)
  iterate(Nil, 143)
  143
143
```

* No nested stack frames are created, the existing one gets replaced each time

* No StackOverflowError happens anymore even if we are using recursion

* Tail call optimization executes a recursive function as an _iterative
  process_

---

### Tail call optimization

* Most languages (like Java or C#) don't perform this optimization at all,
  so the stack overflow problem will persist anyway

* Tail call optimization in Scala happens automatically, we don't need to do
  anything more than ensuring a tail position for the recursive call

* With this optimization, we can safely use recursion as a natural way to
  manipulate recursive data structures

* Scala provides the `@tailrec` to emit a compilation error if the recursive
  call **is not** in tail position

  ```scala
  def sum(list: List[Int]): Int = {

    @tailrec
    def iterate(current: List[Int], accumulator: Int): Int = {
      current match {
        case Cons(head, tail) => iterate(tail, accumulator + head)
        case Nil              => accumulator
      }

    iterate(list, 0)

  }
  ```

---

### General tail call optimization

* Scala, however, only optimizes self-recursive tail calls, not any other type
  of recursion scheme

* Let's consider an example: we want to check if the length of a list is Even
  or Odd

* We could compute the length of the list and do a modulo-2 operation

  ```scala
  def hasEvenLength[A](list: List[A]): Boolean = (list.length % 2) == 0
  ```

* ...but we want to be fancier

* A list has even length if it is Nil or if its Tail has an odd lenght

* A list has an odd length if its Tail has an even length

  ```python
  for all n >= 0:  
    if n = 0           : n is even  
    if (n - 1) is odd  : n is even  
    if (n - 1) is even : n is odd
  ```

---

### General tail call optimization

Let's try to implement that idea in Scala:

```scala
def hasEvenLength[A](list: List[A]): Boolean =
  list match {
    case Cons(_, tail) => hasOddLength(tail)
    case Nil           => true
  }

def hasOddLength[A](list: List[A]): Boolean =
  list match {
    case Cons(_, tail) => hasEvenLength(tail)
    case Nil           => false
  }

hasEvenLength(Cons(1, Cons(2, Cons(3, Cons(4, Nil))))) // => true
```

There is recursion in this example, but not self-recursive calls

The recursion here is mutual between the two functions, until a base case is
reached

---

### General tail call optimization

That mutual recursion is not optimized by the Scala compiler. So the generated
process will look like this:

```scala
Given Cons(1, Cons(2, Cons(3, Cons(4, Nil))):

hasEvenLength(Cons(1, Cons(2, Cons(3, Cons(4, Nil)))))
  hasOddLength(Cons(2, Cons(3, Cons(4, Nil))))
    hasEvenLength(Cons(3, Cons(4, Nil)))
      hasOddLength(Cons(4, Nil))
        hasEvenLength(Nil)
        true
      true
    true
  true
true
```

* So, even if all recursive calls are in tail position, this will generate a
  _recursive process_

* The JVM, just as before, will crash with StackOverflow errors with big enough
  lists

---

### General tail call optimization

* We want to let the compiler know that this can be optimized with tail-call
  elimination

* There is a clever solution based on making the functions build a description
  of the program instead of making the actual recursive calls themselves

* A mechanism is then needed to take that description and execute the calls

* The most common "pattern" to implement that kind of solution is called
  *Trampolining*

  <small>Trampolining is an example of [Continuation-Passing
  Style](https://en.wikipedia.org/wiki/Continuation-passing_style)
  programming</small>

---

### Trampolining

Trampolining in Scala is based upon a Recursive ADT that encodes two possible
scenarios:

* There is no more work to be done, the base case has been reached

* There is more work to be done, so a new recursive call needs to happen

* The encoding looks like this:

  ```scala
  sealed trait Trampoline[A]

  final case class Done[A](value: A) extends Trampoline[A]
  final case class More[A](call: () => Trampoline[A]) extends Trampoline[A]
  ```

  - Done holds the value of the finished computation

  - More holds a call to be made, that will return a new Trampoline instance
    itself

---

### Trampolining

Here is a program with two recursive calls and a final base case returning 42,
represented with both self-recursion and trampolining:

```scala
val recursiveProgram: Int = {
  def iterate(n: Int): Int =
    if (n == 2) 42 else iterate(n + 1)

  iterate(0)
}
```

```scala
val trampolinedProgram: Trampoline[Int] = More(() => More(() => 42))
```

* The first program executes the recursive calls directly and returns the final
  value

* The second one returns a **description of the computation** to be done, but
  nothing gets executed

* We sometimes say tha trampoline-like patterns **suspend** the computation

---

### Trampolining

And here is how it would look like with our previous even/odd checkers:

```scala
def hasEvenLength[A](list: List[A]): Trampoline[Boolean] =
  list match {
    case Cons(_, tail) => More(() => hasOddLength(tail))
    case Nil           => Done(true)
  }

def hasOddLength[A](list: List[A]): Trampoline[Boolean] =
  list match {
    case Cons(_, tail) => More(() => hasEvenLength(tail))
    case Nil           => Done(false)
  }

val program: Trampoline[Boolean] = hasEvenLength(Cons(1, Cons(2, Cons(3, Cons(4, Nil)))))
```

They now return a description of the computation instead of executing the
recursion directly

---

### Trampolined execution

Now we need a way to execute a Trampoline:

```scala
@tailrec
def run[A](trampoline: Trampoline[A]): A =
  trampoline match {
    case More(next)  => run(next()) // self-recursive call in tail position!
    case Done(value) => value
  }
```

```scala
val list: List[Int] = Cons(1, Cons(2, Cons(3, Cons(4, Nil))))

val program: Trampoline[Boolean] = hasEvenLength(list)

run(program) // => true
```

The even/odd checkers benefit now from tail-call optimization, even if not
being self-recursive functions themselves

---

### Trampolined execution

The trampolining technique will unfold the computation like this (considering
the tail call optimization that Scala will already do):

```scala
Given Cons(1, Cons(2, Cons(3, Cons(4, Nil))):

run(hasEvenLength(Cons(1, Cons(2, Cons(3, Cons(4, Nil))))))
  run(More(() => hasOddLength(Cons(2, Cons(3, Cons(4, Nil))))))
  run(More(() => hasEvenLength(Cons(3, Cons(4, Nil)))))
  run(More(() => hasOddLength(Cons(4, Nil))))
  run(More(() => hasEvenLength(Nil)))
  run(Done(true))
  true
true
```

* Trampolining makes the mutually recursive calls generate a _iterative
  process_, as the *run* function benefits from tail call optimization

* With Trampolining in place, we no longer have StackOverflow errors if using
  mutually recursive functions

  <small>Fore more in depth information and the problems that monadic
  structures provoke together with Trampoline (and a solution) see see
  [Stackless Scala with Free Monads (2012) - Runar
  Bjarnason](http://blog.higher-order.com/assets/trampolines.pdf)</small>


---

### Trampolined execution without tail-call optimization

If we didn't have tail-call elimination, the implementation of run could be
somewhat like this:

```scala
def run[A](trampoline: Trampoline[A]): A = {
  var current = trampoline

  while (true) {
    current match {
      case More(next)  => current = next()
      case Done(value) => return value
    }
  }
}
```

* This implies that we can use a trampoline-like mechanism to optimize
  recursive calls in languages without tail-call optimization (like in Java or
  C#)

---
