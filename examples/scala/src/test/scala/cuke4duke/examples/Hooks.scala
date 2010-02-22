package cuke4duke.examples

import cuke4duke.scala.{Dsl, EN}

class Hooks extends Dsl with EN {
  Before("@never", "@ever") {
     throw new RuntimeException("Never ever get here")
  }
}
