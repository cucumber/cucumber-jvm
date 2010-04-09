package cuke4duke.examples

import cuke4duke._

class Hooks extends ScalaDsl with EN {
  Before("@never", "@ever") {
     throw new RuntimeException("Never ever get here")
  }
}
