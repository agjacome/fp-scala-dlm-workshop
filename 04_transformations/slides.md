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

Just for the sake of making more explicit the fact that Lists are recursive,
we'll use the Cons syntax in these slides

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
def sum(list: List[Int]): Int = {
  var accumulator = 0

  var continue = true
  var current  = list

  while (continue) {
    current match {
      case Cons(head, tail) =>
        accumulator = accumulator + head
        current = tail

      case Nil =>
        continue = false
    }
  }

  accumulator
}

sum(Cons(1, Cons(42, Cons(100, Nil)))) // => 143
```

This is a pure function, as it does not produce any observable effect

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

```
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

* The JVM will crash with a **StackOverflowError** if the list is big enough,
  as the total memory for the stack is not infinite

* This kind of evaluation is sometimes referred as _recursive process_

---

### Recursive processes vs iterative processes

Let's draw instead the previous while-loop recursion tree:

```
Given Cons(1, Cons(42, Cons(100, Nil))):

sum(Cons(1, Cons(42, Cons(100, Nil))))
  acc = 0  ; current = Cons(1, Cons(42, Cons(100, Nil)))
  acc = 1  ; current = Cons(42, Cons(100, Nil))
  acc = 43 ; current = Cons(100, Nil)
  acc = 143; current = Nil
```

* No nesting is present, so there are no new stack frames allocated besides the
  top-level one

* It doesn't matter how long the list is, the JVM will not crash because
  StackOverflow errors

* This kind of evaluation is sometimes referred as _iterative process_

---

### Recursive processes vs iterative processes

What would be ideal is having the same kind of _iterative process_, but encoded
as a recursive function. Let's try this instead:

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
and will add up the head to the accumulator before the recursive call

---

### Recursive processes vs iterative processes

Unfortunately, in most languages, the recursion tree generated will be almost
the same as before:

```
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
```

* The same problem at the JVM level will occurr with large enough lists

* But the recursive call is the last meaningful instruction to happen at the
  machine level

* Each recursive call does not need to "wait" for the nested call to complete
  before aggregating the results in the accumulator, it just returns directly

* This kind of recursion is named **tail recursion**, because the recursion
  happens at the very end of the function (the tail)

---

### Tail call optimization

The Scala compiler performs an automatic optimization of the generated JVM
ByteCode commonly know as **tail call elimination or optimization** for that kind
of recursive functions

Instead of creating a new Stack Frame for each recursive call, the compiler
will just replace the existing one. So the actual recursion tree generated by
the previous tail-recursive version will be this:

```
Given Cons(1, Cons(42, Cons(100, Nil))):

sum(Cons(1, Cons(42 Cons(100, Nil))))
  iterate(Cons(1, Cons(42, Cons(100, Nil))), 0)
  iterate(Cons(42, Cons(100, Nil)), 1)
  iterate(Cons(100, Nil), 43)
  iterate(Nil, 143)
  143
```

* No nested stack frames are created, the previous one gets replaced each time

* No StackOverflowError happens anymore even if we are using recursion

* Tail call optimization execures a recursive function as an _iterative
  process_

---

### Tail call optimization

* Most languages (like Java or C#) don't perform this optimization at all,
  so the stack overflow problem will persist anyway

* Tail call optimization in Scala happens automatically, programmers don't need
  to do anything more than making sure that the recursive call is in tail
  position

* Given this optimization, we can safely use recursion as a natural way to
  manipulate recursive data structures

* Scala provides the `@tailrec` annotation to instruct the compiler to emit an
  error if the recursive cal **is not** in tail position
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

However, the Scala compiler is only able to optimize self-recursive tail calls,
but does not perfor tail call elimination in the general case. See this example
that checks if a given list has an Even or Odd length (considering 0 as even):

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

There is recursion in this example, but not self-recursive calls. Each
function will recursively call the other one until reaching the base case.

---

### General tail call optimization

That type of recursion is not optimized by the Scala compiler. So the generated
process will look like this:

```
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

### Trampolining

* What we want is to be able to use that tail-call elimination that the compiler
  already provides in this kind of scenarios too

* The solution is to make the functions build a description of the program,
  instead of actually making the recursive calls and have a mechanism that
  performs the iteration

* The method that is commonly used as a solution to this problem is called
  *Trampolining* and it consists of an ADT that can represent a recursive
  program

---

### Trampolining

```scala
sealed trait Trampoline[A]

final case class Done[A](value: A) extends Trampoline[A]
final case class More[A](call: () => Trampoline[A]) extends Trampoline[A]
```

* Done represents the case where there are no (more) computations to be done,
  and can return a value

* More represents the case where there is a recursive call to be made

---

### Trampolining

Here is an example of a program with 2 suspended computations (the recursive
calls) and a final return value of 42

```scala
val program: Trampoline[Int] = More(() => More(() => 42))
```

And here is how it would look like with oure previous even/odd checkers:

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

---

### Trampolining

So now, instead of directly executing the recursive calls, the functions return
themselves a Trampoline type that will hold the description of what calls need
to be made.

We will need way to actually execute the computation now:

```scala
def run[A](trampoline: Trampoline[A]): A =
  trampoline match {
    case More(next)  => run(next())
    case Done(value) => value
  }


val program: Trampoline[Boolean] = hasEvenLength(Cons(1, Cons(2, Cons(3, Cons(4, Nil)))))
run(program) // => true
```

---

### Trampolining

Let's see how the trampolining technique will unfold the computation
(considering the tail call optimization that Scala will already do):

```
Given Cons(1, Cons(2, Cons(3, Cons(4, Nil))):

run(hasEvenLength(Cons(1, Cons(2, Cons(3, Cons(4, Nil))))))
run(More(() => hasOddLength(Cons(2, Cons(3, Cons(4, Nil))))))
run(More(() => hasEvenLength(Cons(3, Cons(4, Nil)))))
run(More(() => hasOddLength(Cons(4, Nil))))
run(More(() => hasEvenLength(Nil)))
run(Done(true))
true
```

* Trampolining makes the mutually recursive calls generate a _iterative
  process_, because the *run* function benefits iself from tail call
  optimization

* With Trampolining in place, we will no longer have JVM StackOverflow errors
  when using mutually recursive functions to manipulate recursive data
  structures

* Fore more in depth information and the problems that arise when using monadic
  structures when using Trampoline (and a solution to it) see see [Stackless
  Scala with Free Monads (2012) - Runar Bjarnason](http://blog.higher-order.com/assets/trampolines.pdf)
