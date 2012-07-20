package cucumber.runtime.groovy

class CustomWorld {
    private def cukes
    def lastAte

    def haveCukes(n) {
        cukes = n
    }

    def checkCukes(n) {
        assertEquals(cukes, n)
    }

    def lastAte(food) {
        lastAte = food
    }

    def getMood() {
        'cukes'.equals(lastAte) ? 'happy' : 'tired'
    }
}
