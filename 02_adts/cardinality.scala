// Native Nothing and Any types cannot be used directly in these examples
// because they are bottom and top types respectively, and cause implicit
// divergences. This is just a hack for teaching purposes.
sealed trait Any
sealed trait Nothing

final case class Cardinality[A](value: String)

object Cardinality {

  def of[A: Cardinality]: String = implicitly[Cardinality[A]].value

  implicit val nothingCardinality: Cardinality[Nothing] = Cardinality("0")
  implicit val unitCardinality: Cardinality[Unit]       = Cardinality("1")

  implicit val booleanCardinality: Cardinality[Boolean] = Cardinality("2")
  implicit val byteCardinality:Cardinality[Byte]        = Cardinality("2^8")
  implicit val shortCardinality: Cardinality[Short]     = Cardinality("2^16")
  implicit val intCardinality: Cardinality[Int]         = Cardinality("2^32")
  implicit val longCardinality: Cardinality[Long]       = Cardinality("2^64")
  implicit val floatCardinality: Cardinality[Float]     = Cardinality("many")
  implicit val doubleCardinality: Cardinality[Double]   = Cardinality("many")
  implicit val charCardinality: Cardinality[Char]       = Cardinality("2^16")
  implicit val stringCardinality: Cardinality[String]   = Cardinality("many")
  implicit val anyCardinality: Cardinality[Any]         = Cardinality("∞")

  implicit def optionCardinality[A: Cardinality]: Cardinality[Option[A]] = {
    val cardinalityOfA = Cardinality.of[A]
    Cardinality(s"($cardinalityOfA) + 1")
  }

  implicit def eitherCardinality[L: Cardinality, R: Cardinality]: Cardinality[Either[L, R]] = {
    val cardinalityOfL = Cardinality.of[L]
    val cardinalityOfR = Cardinality.of[R]
    Cardinality(s"($cardinalityOfL) + ($cardinalityOfR)")
  }

  implicit def tuple2Cardinality[A: Cardinality, B: Cardinality]: Cardinality[(A, B)] = {
    val cardinalityOfA = Cardinality.of[A]
    val cardinalityOfB = Cardinality.of[B]
    Cardinality(s"($cardinalityOfA) x ($cardinalityOfB)")
  }

  implicit def tuple3Cardinality[A: Cardinality, B: Cardinality, C: Cardinality]: Cardinality[(A, B, C)] = {
    val cardinalityOfA = Cardinality.of[A]
    val cardinalityOfB = Cardinality.of[B]
    val cardinalityOfC = Cardinality.of[C]
    Cardinality(s"($cardinalityOfA) x ($cardinalityOfB) + ($cardinalityOfC)")
  }

  implicit def function1Cardinality[A: Cardinality, B: Cardinality]: Cardinality[A => B] = {
    val cardinalityOfA = Cardinality.of[A]
    val cardinalityOfB = Cardinality.of[B]
    Cardinality(s"($cardinalityOfB) ^ ($cardinalityOfA)")
  }

  implicit def function2Cardinality[A: Cardinality, B: Cardinality, C: Cardinality]: Cardinality[(A, B) => C] = {
    val cardinalityOfA = Cardinality.of[A]
    val cardinalityOfB = Cardinality.of[B]
    val cardinalityOfC = Cardinality.of[C]
    Cardinality(s"($cardinalityOfC) ^ (($cardinalityOfA) * ($cardinalityOfB))")
  }

}

object Main {

  def main(args: Array[String]): Unit = {
    println(s"""
    >Basic:
    >|Nothing| = ${Cardinality.of[Nothing]}
    >|Unit|    = ${Cardinality.of[Unit]}
    >|Boolean| = ${Cardinality.of[Boolean]}
    >|Byte|    = ${Cardinality.of[Byte]}
    >|Short|   = ${Cardinality.of[Short]}
    >|Int|     = ${Cardinality.of[Int]}
    >|Long|    = ${Cardinality.of[Long]}
    >|Float|   = ${Cardinality.of[Float]}
    >|Double|  = ${Cardinality.of[Double]}
    >|Char|    = ${Cardinality.of[Char]}
    >|String|  = ${Cardinality.of[String]}
    >|Any|     = ${Cardinality.of[Any]}
    """.stripMargin('>'))

    println(s"""
    >Option:
    >|Option[Nothing]| = ${Cardinality.of[Option[Nothing]]}
    >|Option[Unit]|    = ${Cardinality.of[Option[Unit]]}
    >|Option[Boolean]| = ${Cardinality.of[Option[Boolean]]}
    >|Option[Int]|     = ${Cardinality.of[Option[Int]]}
    >|Option[Any]|     = ${Cardinality.of[Option[Any]]}
    """.stripMargin('>'))

    println(s"""
    >Either:
    >|Either[Nothing, Nothing]|     = ${Cardinality.of[Either[Nothing, Nothing]]}
    >|Either[Unit, Nothing]|        = ${Cardinality.of[Either[Unit, Nothing]]}
    >|Either[Nothing, Unit]|        = ${Cardinality.of[Either[Nothing, Unit]]}
    >|Either[Unit, Unit]|           = ${Cardinality.of[Either[Unit, Unit]]}
    >|Either[Nothing, Any]|         = ${Cardinality.of[Either[Nothing, Any]]}
    >|Either[Unit, Any]|            = ${Cardinality.of[Either[Unit, Any]]}
    >|Either[String, Int]|          = ${Cardinality.of[Either[String, Int]]}
    >|Either[Double, Option[Long]]| = ${Cardinality.of[Either[String, Option[Long]]]}
    """.stripMargin('>'))

    println(s"""
    >Tuples:
    >|(Nothing, String)|    = ${Cardinality.of[(Nothing, String)]}
    >|(Boolean, Unit)|      = ${Cardinality.of[(Boolean, Unit)]}
    >|(Int, Int, Double)|   = ${Cardinality.of[(Int, Int, Double)]}
    >|((Int, Int), Double)| = ${Cardinality.of[((Int, Int), Double)]}
    """.stripMargin('>'))

    println(s"""
    >Functions:
    >|String  => Int|           = ${Cardinality.of[String => Int]}
    >|Boolean => Int|           = ${Cardinality.of[String => Int]}
    >|(Unit, Boolean) => Int|   = ${Cardinality.of[(Unit, Boolean) => Int]}
    >|((Unit, Boolean)) => Int| = ${Cardinality.of[((Unit, Boolean)) => Int]}
    >|Unit => Boolean => Int|   = ${Cardinality.of[Unit => Boolean => Int]}
    >|String => Nothing|        = ${Cardinality.of[String => Nothing]}
    >|Nothing => Double|        = ${Cardinality.of[Nothing => Double]}
    """.stripMargin('>'))
  }

}
