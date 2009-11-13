package cuke4duke.examples

import cuke4duke.ScalaDsl

class Hooks extends ScalaDsl {
  Before("@never", "@ever") {
     throw new RuntimeException("Never ever get here")
  }
}
