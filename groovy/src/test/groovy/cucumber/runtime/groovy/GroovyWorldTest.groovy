package cucumber.runtime.groovy

import org.junit.Before
import org.junit.Test


class GroovyWorldTest  {
    def world

    class WorldWithPropertyAndMethod {
        def aProperty
        void aMethod(List<Integer> args) {}
    }

    @Before
    void setUp() {
       world = new GroovyWorld()
    }

    @Test(expected = RuntimeException)
    void "should not register pure java object"() {
        world.registerWorld(new String("JAVA"))
    }

    @Test
    void "should support more then one World"() {
        world.registerWorld(new CustomWorld())
        world.registerWorld(new AnotherCustomWorld())

        world.lastAte = "groovy"
        assert world.lastAte == "groovy"

        world.aProperty = 1
        assert world.aProperty == 1

        world.aMethod([1,2])
        assert world.methodArgs == [1,2]

        world.aMethod()
        assert world.methodArgs == "no args"
    }

    @Test(expected = RuntimeException)
    void "should detect double property definition"() {
        world.registerWorld(new WorldWithPropertyAndMethod())
        world.registerWorld(new AnotherCustomWorld())

        world.aProperty
    }

    @Test(expected = RuntimeException)
    void "should detect double method definition"() {
        world.registerWorld(new WorldWithPropertyAndMethod())
        world.registerWorld(new AnotherCustomWorld())

        world.aMethod([1,2])
    }
}
