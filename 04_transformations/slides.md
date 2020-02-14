---
title: 4. Recursive datatypes and transformations
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

### Abstracting over recursion

Let's go back to our previous definition of `sum` for lists:

```scala
def sum(list: List[Int]): Int = {
  def iterate(current: List[Int], accumulator: Int): Int = {
    current match {
      case Cons(head, tail) => iterate(tail, accumulator + head)
      case Nil              => accumulator
    }

  iterate(list, 0)
}
```

This function is totally correct, but there is an opportunity for abstraction
here...

---

### Abstracting over recursion

Take for example the following functions:

```scala
def multiply(list: List[Int]): Double = {
  def iterate(current: List[Int], accumulator: Double): Double = {
    current match {
      case Cons(head, tail) => iterate(tail, accumulator * head)
      case Nil              => accumulator
    }

  iterate(list, 1.0)
}
```

---

### Abstracting over recursion

Take for example the following functions:

```scala
def countWords(list: List[String]): Int = {
  def iterate(current: List[String], accumulator: Int): Int = {
    current match {
      case Cons(head, tail) => iterate(tail, accumulator + head.split(" ").length)
      case Nil              => accumulator
    }

  iterate(list, 0)
}
```

---

### Abstracting over recursion

Take for example the following functions:

```scala
def length[A](list: List[A]): Int = {
  def iterate(current: List[A], accumulator: Int): Int = {
    current match {
      case Cons(head, tail) => iterate(tail, accumulator + 1)
      case Nil              => accumulator
    }

  iterate(list, 0)
}
```

---

### Abstracting over recursion

All of them (and more) apply the same recursion pattern:

* Start with an identity value (0, 1, etc) as the accumulator

* For each Cons, apply an aggregation between the head and the current
  accumulator, and recur with the updated accumulator value

* Once a Nil is reached, return the value of the accumulator

---

### List folds

We can abstract this scheme into a generic function (usually called **fold**)
that can serve as a building block for all of them:

```scala
sealed trait List[+A] {

  def fold[B](zero: B, operation: (B, A) => B): B = {
    def iterate(current: List[A], accumulator: B): B = {
      current match {
        case Cons(head, tail) => iterate(tail, operation(accumulator, head))
        case Nil              => accumulator
      }

    iterate(this, zero)
  }

}
```

---

### List folds

With our fold function in place, our previous recursion-based definitions can
be replaced with a call to this higher-order function:

```scala
def sum(list: List[Int]): Int =
  list.fold(0, (acc, value) => acc + value)

def multiply(list: List[Int]): Double =
  list.fold(1.0, (acc, value) => acc * value)

def countWords(list: List[String]): Int =
  list.fold(0, (acc, sentence) => acc + sentence.split(" ").length)

def length[A](list: List[A]): Int =
  list.fold(0, (acc, _) => acc + 1)
```

Folding provides a way to _summarize_ the values of a recursive ADT by
abstracting over a recursion pattern

---

### Folding direction

Consider now the two following possible alternative implementations of our fold
function:
 
```scala
sealed trait List[+A] {

  def foldLeft[B](zero: => B, operation: (B, A) => B): B =
    this match {
      case Cons(head, tail) => tail.foldleft(operation(zero, head), operation)
      case Nil              => zero
    }

  def foldRight[B](zero: => B, operation: (A, B) => B): B =
    this match {
      case Cons(head, tail) => operation(tail.foldRight(zero, operation), head)
      case Nil              => zero
    }

}
```

<small>Ignore the stack safety problems of these examples, they are naive
versions of the functions, not production-ready ones</small>

---

### Folding direction

In most scenarios both implementations will return the same value:

```scala
val aList = Cons(1, Cons(2, Cons(3, Cons(4, Nil))))

aList.foldLeft(0, _ + _)  // ((((0 + 1) + 2) + 3) + 4) = 10
aList.foldRight(0, _ + _) // ((((0 + 4) + 3) + 2) + 1) = 10
```

However, the "direction" of the recursion is inverted in each implementation.
It's easier to see it if we **fold a list into another list**:

```scala
val aList = Cons(1, Cons(2, Cons(3, Cons(4, Nil))))

aList.foldLeft(Nil, (acc, value) => Cons(value, acc))
//  Cons(4, Cons(3, Cons(2, Cons(1, Nil))))

aList.foldRight(Nil, (value, acc) => Cons(value, acc))
//  Cons(1, Cons(2, Cons(3, Cons(4, Nil))))
```

Folding left reverses the list, while right preserves the same order!

---

### Folding direction

Let's try to "paint" the folding operations to see why this happens:

```scala
foldLeft:

Cons(1, Cons(2, Cons(3, Cons(4, Nil)))).foldLeft(zero, operation)
Cons(2, Cons(3, Cons(4, Nil))).foldLeft(operation(zero, 1), operation)
Cons(3, Cons(4, Nil)).foldLeft(operation(operation(zero, 1), 2), operation)
Cons(4, Nil).foldLeft(operation(operation(operation(zero, 1), 2), 3), operation)
Nil.foldLeft(operation(operation(operation(operation(zero, 1), 2), 3), 4), operation)

operation(operation(operation(operation(zero, 1), 2), 3), 4)
```

```scala
    Cons                        oper 
    /  \                        /  \ 
   1  Cons                    oper  4
      /  \                    /  \ 
     2  Cons       =>       oper  3
        /  \                /  \ 
       3  Cons            oper  2  
          /  \            /  \
         4   Nil        zero  1
```

---

### Folding direction

Let's try to "paint" the folding operations to see why this happens:

```scala
foldRight:

Cons(1, Cons(2, Cons(3, Cons(4, Nil)))).foldRight(zero, operation)
operation(1, Cons(2, Cons(3, Cons(4, Nil))).foldRight(zero, operation))
operation(1, operation(2, Cons(3, Cons(4, Nil)).foldRight(zero, operation)))
operation(1, operation(2, operation(3, Cons(4, Nil).foldRight(zero, operation))))
operation(1, operation(2, operation(4, operation(Nil.foldRight(zero, operation)))))

operation(1, operation(2, operation(3, operation(4, zero))))
```

```scala
    Cons                 oper         
    /  \                 /  \         
   1  Cons              1  oper       
      /  \                 /  \       
     2  Cons       =>     2  oper     
        /  \                 /  \     
       3  Cons              3   oper   
          /  \                  /  \   
         4   Nil               4   zero 
```

---

### Folding direction

**foldLeft** (left-associative) applies the operation in a **outside-in** style:

```scala
val aList = Cons(1, Cons(2, Cons(3, Cons(4, Nil))))

aList.foldLeft(0, (x, y) => add(x, y)) == add(add(add(add(0, 1), 2), 3), 4)

aList.foldLeft(Nil, (x, y) => Cons(y, x)) == Cons(4, Cons(3, Cons(2, Cons(1, Nil))))
```

**foldRight** (right-associative) applies the operation in a **inside-out** style:

```scala
val aList = Cons(1, Cons(2, Cons(3, Cons(4, Nil))))

aList.foldRight(0, (x, y) => add(x, y)) == add(add(add(add(0, 4), 3), 2), 1)

aList.foldRight(Nil, (x, y) => Cons(x, y)) ==  Cons(1, Cons(2, Cons(3, Cons(4, Nil))))
```

---

### foldRight

Folding right has an interesting property, it (intuitively) replaces the Data
Constructors of a List: Nil gets replaced with `zero`, and Cons gets replaced
with `operation`
 
```scala
def mult(x: Int, y: Int): Int = y * x

val list = Cons(1, Cons(2, Cons(3, Nil)))

// Nil ⇒ 1, Cons ⇒ mult    Cons(1, Cons(2, Cons(3, Nil)))
list.foldRight(1, mult) == mult(1, mult(2, mult(3,   1)))
```

<small>Another very important property of foldRight is that it can
theoretically work with infinite structures, while foldLeft not  
(but the Scala standard library has a ~~bug~~ feature which does not permit
it in any case)</small>

---

### Binary trees

Trees are another of the canonical recursive structures in programming. We can
define binary trees with a recursive ADT as following:

```scala
sealed trait Tree[+A]

case class  Node[+A](value: A, left: Tree[A], right: Tree[A]) extends Tree[A]
case object Empty extends Tree[Nothing]

def Leaf[A](value: A): Tree[A] = Node(value, Empty, Empty)
```

And can be instantiated like this:

```scala
val aTree: Tree[Int] = Node(3, Node(2, Leaf(1), Empty), Node(5, Leaf(4), Leaf(6)))

//     3
//    / \
//   2   5
//  /   / \
// 1   4   6
```

---

### Binary trees

Same as with Lists, the "natural" way to iterate over a recursive BinaryTree
is to use recursion

```scala
// not tail-recursive, not optimized
def sum(tree: Tree[Int]): Int =
  tree match {
    case Node(value, left, right) => sum(left) + value + sum(right)
    case Empty                    => 0
  }

val aTree: Tree[Int] = Node(4, Node(2, Leaf(1), Empty), Node(5, Leaf(3), Leaf(6)))

sum(aTree) // 1 + 2 + 3 + 4 + 5 + 6 = 21
```

<small>Notice that, unlike Lists, the previous `sum` definition spawns two
recursive calls per subtree</small>

---

### Foldable trees

As with our List example, the previous `sum` function can also be implemented
in terms of a **fold**:

```scala
sealed trait Tree[+A] {

  def foldLeft[B](zero: B, operation: (B, A) => B): B

  def foldRight[B](zero: B, operation: (A, B) => B): B

}
```

```scala
def sum(tree: Tree[Int]): Int = tree.foldRight(0, _ + _)
```

---

### Foldable structures

In fact, folding is so fundamental as an operation that most ADTs can benefit
from it:

```scala
sealed trait Option[+A] {

  def foldRight[B](zero: => B, operation: (A, B) => B): B =
    this match {
      case Some(value) => operation(value, zero)
      case None        => zero
    }

  def foldLeft[B](zero: => B, operation: (B, A) => B): B =
    this.foldRight(zero, (a, b) => operation(b, a))

}

case class Some[A](value: A) extends Option[A]
case object None extends Option[Nothing]
```

---

### Foldable structures

In fact, folding is so fundamental as an operation that most ADTs can benefit
from it:

```scala
sealed trait Try[+A] {

  def foldRight[B](zero: => B, operation: (A, B) => B): B =
    this match {
      case Success(value) => operation(value, zero)
      case Failure(_)     => zero
    }

  def foldLeft[B](zero: => B, operation: (B, A) => B): B =
    this.foldRight(zero, (a, b) => operation(b, a))

}

case class Success[A](value: A)         extends Try[A]
case class Failure[A](error: Throwable) extends Try[A]
```

---

### Foldable structures

For non-recursive ADTs, foldLeft and foldRight will have exactly the same
semantics. Moreover, sometimes a more simple `fold` function is available,
with a signature-change in the operation function:

```scala
sealed trait Option[A] {

  def fold[B](zero: => B, operation: A => B): B =
    this match {
      case Some(value) => operation(value)
      case None        => zero
    }

}
```

```scala
val profile: Option[Profile] = profileRepository.find(ByName("Martin Fowler"))

val fowlerSalary = profile.fold(BigDecimal.ZERO, _.salary)
```

This signature is not possible for recursive ADTs, because we need a way to
aggregate multiple values into a single value

---

### Monoids

Monoids are algebraic structures composed of an **associative binary
operation** and **an identity value** over a fixed set (type):

```scala
trait Monoid[A] {

  def empty: A

  def combine(x: A, y: A): A

}
```

```scala
val AdditiveInt = new Monoid[Int] {
  def empty: Int = 0
  def combine(x: Int, y: Int): Int = x + y
}

val MultiplicativeInt = new Monoid[Int] {
  def empty: Int = 0
  def combine(x: Int, y: Int): Int = x * y
}

val ConcatenativeString = new Monoid[String] {
  def empty: String = ""
  def combine(x: String, y: String): Int = x + y
}
```

---

### Monoids

Monoids in Functional Programming are most than not associated with Foldable
structures, because they provide the `zero` element and the aggregation
`operation` that folds require:

```scala
sealed trait List[+A] {

  def foldRight[B](zero: B, operation: (A, B) => B): B

  def monoidalFold(monoid: Monoid[A]): A = this.foldRight(monoid.empty, monoid.combine)

}
```

```scala
val aList = Cons(1, Cons(2, Cons(3, Cons(4, Nil))))

aList.monoidalFold(AdditiveInt) // 10
```

---

### Monoids and foldMap

The previous `monoidalFold` does not allow to summarize the recursive ADT into a
value of a different type (if it's a List[Int], the monoidalFold needs to
return Int)

A more useful operation `foldMap` tends to be defined together in our Foldable
structures:

```scala
sealed trait List[+A] {

  def foldRight[B](zero: B, operation: (A, B) => B): B

  def foldMap[B](map: A => B, monoid: Monoid[B]): B =
    this.foldRight(monoid.empty, (x, y) => monoid.combine(x, map(y)))

  def fold(monoid: Monoid[A]): A =
    this.foldRight(monoid.empty, monoid.combine)

}
```

```scala
Cons(1, Cons(2, Cons(3, Cons(4, Nil)))).foldMap(_.toString, ConcatenativeString) // "1234"
```

In this way, the summarized value can be of a different type without problem

---

### Monoids and foldMap

The monoidal versions of folds do not apply, as expected, only to recursive
ADTs:

```scala
sealed trait Option[+A] {

  def fold[B](zero: => B, operation: A => B): B = ...

  def foldRight[B](zero: B, operation: (A, B) => B): B = ...

  def foldMap[B](map: A => B, monoid: Monoid[B]): B =
    this.foldRight(monoid.empty, (x, y) => monoid.combine(x, map(y)))

  def fold(monoid: Monoid[A]): A =
    this.foldRight(monoid.empty, monoid.combine)

}
```

```scala
val just: Option[String]  = Some("42")
val empty: Option[String] = None

just.foldMap(_.toInt, AdditiveInt)  // 42
empty.foldMap(_.toInt, AdditiveInt) // 0
```

---

### Abstracting over Foldable

All the functions that we have covered can be written exclusively based on
`foldRight`, which implies a possible abstraction for all Foldable structures:

```scala
trait Foldable[F[_]] {

  def foldRight[A, B](structure: F[A])(zero: B, operation: (A, B) => B): B

  def foldLeft[A, B](structure: F[A])(zero: B, operation: (B, A) => B): B =
   // implementation based on foldRight

  def foldMap[A, B](structure: F[A])(map: A => B, monoid: Monoid[B]): B =
   // implementation based on foldRight

  def fold[A](structure: F[A])(monoid: Monoid[A]): A =
   // implementation based on foldRight

  // more derived functions based on the previous ones

}
```

We'll cover this abstraction more in depth when we discuss ad-hoc polymorphism
and typeclasses

---

### Foldables in Scala standard library

The Scala standard library has some differences regarding the signatures of
folds that we have covered. For example, for the standard List datatype:

![](/resources/images/04_transformations.fold_stdlib.png)

The semantics for `foldLeft` and `foldRight` are the expected ones, thankfully

---

### Foldables in Scala standard library

Moreover, the Scala standard library fold for Option looks like this:

![](/resources/images/04_transformations.fold_option_stdlib.png)

While for Either (for example), it looks like this:

![](/resources/images/04_transformations.fold_either_stdlib.png)

The stdlib does neither have a `Foldable` datatype that abstracts over folds,
nor a `Monoid` type (and no `foldMap` nor monoidal `fold`)

---

### Foldables in Scala standard library

In summary, the Scala standard library:

* Different signatures for `fold`, `foldRight` and `foldLeft` depending on the
  datatype, but available for most datatypes

* No abstract `Foldable` structure for all foldable datatypes

* No `Monoid` type nor instances for it. Consequently, no `foldMap` nor
  monoidal `fold`

* Consider using **Cats** or **Scalaz** libraries when doing pure FP
  programming in Scala, as they provide these features by default

---

### Structure-preserving transformations

Structure-preserving transformations are those transformations that, after
being applied, leave the structure of the data-type unchanged.

Take for example:

```scala
def multiplyAllByTwo(list: List[Int]): List[Double] = {
  def loop(xs: List[Int], acc: List[Double]): List[Double] =
    xs match {
      case Cons(head, tail) => loop(tail, acc.append(head * 2.0))
      case Nil              => acc
    }
    
  loop(list, Nil)
}

val input  = Cons(1, Cons(2, Cons(3, Nil)))
val output = multiplyAllByTwo(input)

output == Cons(2.0, Cons(4.0, Cons(6.0, Nil)))
```

`multiplyAllByTwo` preserves the original structure of the input list (same
`Cons` and `Nil` "positions"), and only changes the values of each `head`

---

### Mapping over lists

The most common structure-preserving transformation is, as you may be
expecting, `map`

Map can be naively-implemented (there are more efficient ways) in our List ADT
by using foldRight:

```scala
sealed trait List[+A] {

  def foldRight[B](zero: B, op: (A, B) => B): B

  def map[B](mappingFunction: A => B): List[B] =
    foldRight(Nil, (head, tail) => Cons(mappingFunction(head), tail))

}

case class Cons[A](head: A, tail: List[A]) extends List[A]
case object Nil extends List[Nothing]
```

Map is defined as a higher-order function that receives as argument the
adaptation to perform over each element of the list

---

### Mapping over lists

With `map` in place we can then abstract over the recursion mechanism that we
used in our previous `multiplyAllByTwo`

```scala
val multiplyAllByTwo: List[Int] => List[Double] = xs => xs.map(_ * 2.0) 
```

And reuse the same pattern for all the structure-preserving transformations
that we may need:

```scala
sealed abstract class Country(population: Long)

case object Austria     extends Country(8_822_000)
case object Germany     extends Country(82_790_000)
case object Monaco      extends Country(38_695)
case object Switzerland extends Country(8_570_000)

val AllCountries = Cons(Austria, Cons(Germany, Cons(Monaco, Cons(Switzerland, Nil))))

AllCountries.map(c => c.population) // Cons(8_822_000, Cons(82_790_000, Cons(38_695, Cons(8_570_000, Nil))))
```

---

### Mapping over trees

Same as with folding, mapping is a useful and sound operation in all recursive
ADTs

```scala
sealed trait Tree[+A] {

  def map[B](f: A => B): Tree[B] = 
    this match {
      case Node(value, left, right) => Node(f(value), left.map(f), right.map(f))
      case Empty                    => Empty
    }

}

case class  Node[+A](value: A, left: Tree[A], right: Tree[A]) extends Tree[A]
case object Empty extends Tree[Nothing]

def Leaf[A](value: A): Tree[A] = Node(value, Empty, Empty)
```

```scala
val aTree: Tree[Int] = Node(4, Node(2, Leaf(1), Empty), Node(5, Leaf(3), Leaf(6)))

aTree.map(_ * 2) // Node(8, Node(4, Leaf(2), Empty), Node(5, Leaf(3), Leaf(12)))
```

---

### Mapping over non-recursive ADTs

And a useful operation also for non-recursive ADTs like Option:

```scala
sealed trait Option[+A] {

  def map[B](f: A => B): Option[B] =
    this match {
      case Some(a) => Some(f(a))
      case None    => None
    }

}

case class Some[A](value: A) extends Option[A]
case object None extends Option[Nothing]
```

---

### Functor

The general abstraction over `map` for all mappable structures is called
`Functor`:

```scala
trait Functor[Structure[_]] {

  def map[A, B](structure: Structure[A])(mappingFunction: A => B): Structure[B]

}
```

```scala
def countriesToPopulations[F[_]](countries: F[Country], functor: Functor[F]): F[Long] =
  functor.map(countries)(_.population)

countriesToPopulation(Cons(Austria, Cons(Germany, Nil)), ListFunctor) // Cons(8_822_000, Cons(82_790_000, Nil))
countriesToPopulation(Some(Austria), OptionFunctor)                   // Some(8_822_000)
```

Functor instances, to be considered mathematically correct, need to obey two laws:

```scala
Identity: map(functor)(id) == id
Composition: map(functor)(f andThen g) == map(map(functor)(f))(g)
```

---

### Derived functions

Given a map function, we can derive some other useful ones from it:

```scala
trait Functor[F[_]] {

  def map[A, B](fa: [A])(f: A => B): [B]

  def product[A, B](fa: [A])(f: A => B): [(A, B)] =
    this.map(fa)(a => (a, f(a)))

  def tupleRight[A, B](fa: [A])(right: => B): [(A, B)] =
    this.map(fa)(a => (a, right))

  def tupleLeft[A, B](fa: [A])(left: => B): [(B, A)] = 
    this.map(fa)(a => (left, a))

  def unzip[A, B](fab: [(A, B)]): ([A], [B]) =
    (this.map(fab)(_._1), this.map(fab)(_._2)) 

  def as[A, B](fa: [A])(b: => B): [B] =
    this.map(fa)(_ => b)

  def void[A](fa: [A]): [Unit] =
    this.map(fa)(_ => ())

}
```

---

### Functor namings

The commonly-known `map` function can also be found in different names through
different environments. Some of the most common ones:

```scala
trait Functor[F[_]] {

  def map[A, B](fa: [A])(f: A => B): [B]

  final def <$>[A, B](fa: [A])(f: A => B): [B] =
    this.map(fa)(f)

  final def fmap[A, B](fa: [A])(f: A => B): [B] =
    this.map(fa)(f)

  final def then[A, B](fa: [A])(f: A => B): [B] =
    this.map(fa)(f)

}

val xs = Cons(1, Cons(2, Cons(3, Nil)))
xs <$> (_ * 2) // Cons(2, Cons(4, Cons(6, Nil)))
```

The key relevant concept here is the function signature. As long as the types
match, we are referring to the operation of "adapting" while preserving the
structure, no matter the name that the library, environment or language choses

---

### Functors in Scala standard library

* As usual, the Scala standard library does not define the Functor abstraction

* But, at least, all the `map` functions defined in the standard library follow
  the same syntax and semantics that we have defined before

  ```scala
  List(1, 2, 3, 4, 5).map(_ * 2)
  Some(42).map(_ + 125)
  ```

* Some of the derived Functor functions are available in different names, and
  not accross all types. Look at the function signatures to identify their
  analogous!

* Cats, Scalaz and similar FP libraries do actually define the [Functor
  abstraction](https://typelevel.org/cats/api/cats/Functor.html), with all the
  derived functions and with instances for all the basic ADTs in the STD
  library

---

### Mapping variance

* The `map` signature (resp. Functor) that we have just seen is
  more formally defined as a **covariant map** (resp. Covariant Functor)

* If we recall from our discussion about subtype variance, we said that Covariant
  generic types are those that allow the relationship `A >: B` to be lifted
  into the structure F as `F[A] >: F[B]`

* Analogously, covariant map allows the relationship `A => B` to be lifted into
  the relationship of structures `F[A] => F[B]`. Meaning that if an adaptation
  from A to B exists, then an adaptation from F[A] to F[B] also exists

* This simetry allows us to create a "manual" covariant subtyping relationship
  for invariant types:

  ```scala
  sealed trait Option[B] {
    def widen[A >: B]: Option[A] = this.map(identity) 
  }

  val x: Option[LocalDate] = ???
  val y: Option[Temporal]  = x.widen[Temporal]
  val z: Option[Any]       = y.widen[Any]
  ```

---

### Mapping variance

* As you may have guessed, the dual **contravariant map** (resp. Contravariant
  Functor) operation also exist, usually called `contramap`

* Contravariant generic types allow the relationship `B >: A` to be lifted
  backwards into the structure F as `F[A] >: F[B]`

* Contravariant map, similarly, allows the relationship `B => A` to be lifted
  backwards into the relationship of structures `F[A] => F[B]`

* As with the covariant version, this property allows us to create another
  "manual" covariant subtyping relationship for invariant types

  ```scala
  trait Comparable[B] {
    def narrow[B <: A]: Comparable[A] = this.contramap(identity)
  }

  val x: Comparable[Any] = ???
  val y: Comparable[Int] = x.narrow[Any]
  ```

---

### Comparable

The previous Comparable interface (somewhat similar to Scala's Ordering and
Java's own Comparable) can have be defined like this:

```scala
sealed trait Comparison
case object GreaterThan extends Comparsion
case object Equal       extends Comparison
case object LessThan    extends Comparison

trait Comparable[A] {
  def compare(x: A, y: A): Comparison
}

val intComparable = new Comparable[Int] {

  def compare(x: Int, y: Int): Comparison =
    if (x > y) GreaterThan
    else if (x == y) Equal
    else LessThan

}

intComparable.compare(1, 2) // LessThan
intComparable.compare(2, 2) // Equal
intComparable.compare(2, 1) // GreaterThan
```

---

### Contravariant maps

These kind of structures like Comparable, that do not "produce" any value of
its generic type, but just "consume" them can not implement a covariant map in
any reasonable way:

```scala
trait Comparable[A] {

  def compare(x: A, y: A): Comparison

  final def map[B](f: A => B): Comparable[B] =
    new Comparable[B] {
      // now what??? I have two Bs, but no A to supply to f
      def compare(x: B, y: B): Comparison =
        throw Exception
    }

}
```

---

### Contravariant maps

But instead, a contravariant map can be easily defined:

```scala
trait Comparable[A] {

  def compare(x: A, y: A): Comparison

  final def contramap[B](f: B => A): Comparable[B] = {
    val self = this

    new Comparable[B] {
      // I know how to compare As, I have a transformation from B to A, and I have two Bs, yay!
      def compare(x: B, y: B): Comparison =
        self.compare(f(x), f(y))
    }
  }

}
```

```scala
val intComparable: Comparable[Int] = new Comparable[Int] { def compare(x: Int, y: Int): Comparison = ... }

val numericStringComparable = intComparable.contramap(str => str.toInt)

numericStringComparable.compare("1", "5") // LessThan
```

---

### Contravariant maps

* Structures that support contravariant maps are less common than ones that
  support covariant maps. However, they tend to be quite useful in practice

* Contravariant maps are also an structure-preserving transformation. It's
  easier to identify that property because almost no recursive structure is
  contravariant

* Almost all "consumer-like" interfaces like our previous `Comparable` have are
  capable of supporting contravariant maps. Think about formatters, encoders
  and similar interfaces

---

### Contravariant Functor

As we have said already before, the abstraction that "governs" all these
contravariant structures is called "Contravariant Functor" and is usually
defined as:

```scala
trait Contravariant[F[_]] {

  def contramap[A, B](fa: F[A])(f: B => A): F[B]

}

val contravariantComparable = new Contravariant[Comparable] {
  override def contramap[A, B](fa: Comparable[A])(f: B => A): Comparable[B] =
    new Comparable[B] {
      def compare(x: B, y: B): Comparison = fa.compare(f(x), f(y))
    }
}
```

Contravariant instances need to follow a couple of laws to be mathematically
sound:

```scala
Identity: contramap(fa)(identity) == fa
Composition: contramap(fa)(f andThen g) == contramap(contramap(fa)(f))(g)
```

---

### Contravariant in Scala standard library

* The Scala standard library does not contain any `Contravariant` abstraction

* Almost no interface in the standard library defines a contramap-like
  behaviour, and when it is their it has namings like
  ["on"](https://www.scala-lang.org/api/current/scala/math/Ordering.html#on[U](f:U=%3ET):scala.math.Ordering[U])

* Cats, Scalaz and similar FP libraries do actually define the [Contravariant
  abstraction](https://typelevel.org/cats/api/cats/Contravariant.html), with
  instances for all the basic interfaces in the STD library

---

### Lists of lists

Sometimes, we have the following pattern repeating over our code:

```scala
sealed abstract class Country(officialLanguages: List[Language])

case object Spain extends Country(List(Spanish, Catalan, Galician, Basque))
case object UK    extends Country(List(English, Welsh))

def aggregate(pending: List[List[Language]]): List[Language] =
pending match {
  case Cons(head, tail) => head.addAll(aggregate(tail))
  case Nil              => Nil 
}

val allCountries: List[Country] = List(Spain, UK)
val langs: List[List[Language]] = allCountries.map(_.officialLanguages)

val allOfficialLanguages: List[Language] = aggregate(langs)
```

The main concern of the `aggregate` function of the example is converting a
`List[List[Language]]` to just a `List[Language]` by aggregating all the
sublists into a single one

---

### List flattening

The concept of aggregating a List of Lists into a single un-nested List is
known as "list flattening" and can be generified to the following function:

```scala
// beware non-optimized naive implementations, they can be easily made tail-recursive

sealed trait List[+A] {

  def addAll(xs: List[A]): List[A] =
    this match {
      case Cons(head, tail) => Cons(head, tail.addAll(xs))
      case Nil              => xs
    }

}

def flatten[A](xss: List[List[A]]): List[A] =
  xss match {
    case Cons(head, tail) => head.addAll(flatten(tail))
    case Nil              => Nil
  }

val listOfListOfLanguages: List[List[Language]] = List(Spain, UK).map(_officialLanguages)

flatten(listOfListOfLanguages) // List[Language](Spanish, Catalan, Galician, Basque, English, Welsh)
```

---

### Flattening

As you may expect too, flattening is not a concept unique to Lists:

```scala
def flattenOption[A](opts: Option[Option[A]]): Option[A] =
  opts match {
    case Some(innerOption) => innerOption
    case None              => None
  }

def flattenTry[A](tries: Try[Try[A]]): Try[A] =
  tries match {
    case Success(innerTry) => innerTry
    case Failure(error)   => Failure(error)
  }
```

---

### Abstracting over flatten

We may think that there is another governing abstraction over all flatten-able
structures:

```scala
trait Flattenable[F[_]] {

  def flatten[A](fa: F[F[A]]): F[A]

}

val flattenableList: Flattenable[Option] = new Flattenable[Option] {
  override def flatten[A](fa: Option[Option[A]]): Option[A] =
    fa match {
      case Some(inner) => inner
      case None        => None
    }
}
```

We will see later, hoewever, a better abstraction than this one, that happens
to be mathematically sound. Flattenable is just an "invented" abstraction that
no FP library contains.

---

### Flatten in the Scala standard library

* The Scala standard library does not define any `Flattenable` abstraction, nor
  any FP-centric library like Cats, Scalaz et al.

* But `flatten` as an [individual
  operation](https://www.scala-lang.org/api/current/scala/collection/immutable/List.html#flatten[B](implicittoIterableOnce:A=%3Escala.collection.IterableOnce[B]):CC[B])
  appears in a lot of structures in the STD lib, encoded with an implicit type
  constraint:

  ```scala
  sealed trait List[A] {
    // the constraint will be similar to this, proving that A is a List of something else
    def flatten[B](implicit innerValueIsAList: A =:= List[B]): List[B] = ???
  }

  val xs = List(List(1, 2, 3), List(4, 5))
  xs.flatten // List(1, 2, 3, 4, 5)

  val ys = List(1, 2, 3)
  ys.flatten // DOES NOT COMPILE!
  ```

---

### Mapping, and then flattening Lists

Another common pattern that we have already seen before, is the concept of
first applying a map, and then flattening the result. This operation can be
reduced to a single one called `flatMap`:

```scala
sealed trait List[+A] {

  def map[B](f: A => B): List[B]

  def flatten[B](implicit evidence: A =:= List[B]): List[B]

  def flatMap[B](f: A => List[B]): List[B] =
    this.map(f).flatten

}
```

```scala
List(Spain, UK).flatMap(_.officialLanguages) // List[Language](Spanish, Catalan, Galician, Basque, English, Welsh)
```

---

### FlatMapping over other ADTs

As always, flat-mapping is not only limited to Lists nor only to recursive ADTs:

```scala
sealed trait Option[+A] {

  def map[B](f: A => B): Option[B]

  def flatten[B](implicit evidence: A =:= Option[B]): Option[B]

  def flatMap[B](f: A => Option[B]): Option[B] =
    this.map(f).flatten

}
```

```scala
Some(42).flatMap(x => Some(x * 3)) // Some(126)
None.flatMap(_ => Some(3))         // None
```

---

### Derived and fundamental operations

We have seen that `flatMap` can be implemented in terms of `map` and `flatten`,
but the other way around is also possible:

```scala
sealed trait List[A] {

  def flatMap[B](f: A => List[B]): List[B]

  def map[B](f: A => B): List[B] =
    this.flatMap(a => Cons(f(a), Nil))

  def flatten[B](implicit evidence: A =:= List[B]): List[B] =
    this.flatMap(identity)

}
```

With this encoding, we only need a way to abstract over the data constructor
`Cons`, and we will be able to provide `map` and `flatten` automatically for
all structures that implement `flatMap`

This abstraction of the data constructor with one value is commonly known as
`pure`, as it creates a new ADT from a pure value

---

### Abstracting over flatMap

The abstraction, given both a `pure` and a `flatMap` fundamental operations
looks like this:

```scala
trait FlatMappable[F[_]] {

  def pure[A](a: => A): F[A]

  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

  def map[A, B](fa: F[A])(f: A => B): F[B] =
    this.flatMap(a => pure(f(a)))

  def flatten[A](ffa: F[F[A]]): F[A] =
    this.flatMap(identity)

}

val flatMappableOption = new FlatMappable[Option] {

  override def pure[A](a: => A): Option[A] = Some(a)

  override def flatMap[A, B](optA: Option[A])(f: A => Option[B]): Option[B] =
    optA match {
      case Some(value) => f(value)
      case None        => None
    }

}
```

---

### FlatMappable <: Functor

The fact that we can define `map` in terms of flatMap allows us to define an
"inheritance" (it does not need to be inheritance, but it is the natural way to
do it in a subtyping language like Scala) like the following:

```scala
trait FlatMappable[F[_]] extends Functor[F] {

  def pure[A](a: => A): F[A]

  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

  def flatten[A](ffa: F[F[A]]): F[A] =
    this.flatMap(identity)

  override def map[A, B](fa: F[A])(f: A => B): F[B] =
    this.flatMap(a => pure(f(a)))

}
```

This way, defining a FlatMappable instance also provides us with a Functor
instance and all its functions

---

### Monad

The term `FlatMappable` is, of course, not the formal name, nor the widespread
one. The FP term that we use to refer to this abstract structure is Monad:

```scala
trait Monad[F[_]] extends Functor[F] {

  def pure[A](a: => A): F[A]

  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

}
```

All Monad instances, to be mathematically correct, need to abide to three laws:

```scala
Left identity: flatMap(pure(a))(f) == f(a)
Right identity: flatMap(fa)(pure) == fa
Associativity: flatMap(fa)(a => flatMap(f(a))(g)) == flatMap(flatMap(fa)(f))(g)
```

---
