package cucumber.runtime.groovy

class CustomWorld {
    private def cukes
    def lastAte

    def haveCukes(Integer n) {
        cukes = n
    }

    def checkCukes(Integer n) {
        assertEquals(cukes, n)
    }

    def lastAte(food) {
        lastAte = food
    }

    def getMood() {
        'cukes'.equals(lastAte) ? 'happy' : 'tired'
    }
}
