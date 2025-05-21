package io.cucumber.examples.calaculator

class RpnCalculator {
    private val stack: ArrayDeque<Number> = ArrayDeque()

    private val OPS = setOf("+", "-", "*", "/")

    fun push(arg: Any) {
        if (arg in OPS) {
            val y = stack.removeLast()
            val x = if (stack.isEmpty()) 0 else stack.removeLast()
            val valResult = when (arg) {
                "-" -> x.toDouble() - y.toDouble()
                "+" -> x.toDouble() + y.toDouble()
                "*" -> x.toDouble() * y.toDouble()
                "/" -> x.toDouble() / y.toDouble()
                else -> throw IllegalArgumentException("Unknown operation $arg")
            }
            push(valResult)
        } else {
            stack.addLast(arg as Number)
        }
    }

    fun peek(): Number? = stack.lastOrNull()

    fun clear() = stack.clear()
}

