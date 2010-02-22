package cuke4duke.examples

import cuke4duke.{English, ScalaDsl}

class Hooks extends ScalaDsl with English {
  Before("@never", "@ever") {
     throw new RuntimeException("Never ever get here")
  }
}
