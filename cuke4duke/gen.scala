/*
 * Generates the evil looking apply methods in ScalaDsl#StepBody for Function1 to Function22
 */
for (i <- 1 to 22) {
  val ts = (1 to i).map("T" +).mkString(", ")
  val f = "(" + ts + ") => Any"
  val pf = "{ case List(" + (1 to i).map("a" +).mkString(", ") + ") => f(" + (1 to i).map(n => "t" + n + "(a" + n + ")").mkString(", ") + ") }"
  val p1 = "def apply[" + ts + "](f: " + f + ")"
  val p2 = "(implicit " + (1 to i).map(n => "m" + n + ":Manifest[T" + n + "], t" + n + ":Transformation[T" + n + "]").mkString(", ") + ")"
  val doHandle = "doHandle(List(" + (1 to i).map("m" +).mkString(", ") + "))" + pf

  println(p1 + p2 + " = " + doHandle)
}