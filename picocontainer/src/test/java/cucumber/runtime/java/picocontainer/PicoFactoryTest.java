package cucumber.runtime.java.picocontainer;

import cucumber.api.java.ObjectFactory;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PicoFactoryTest {

    private final ObjectFactory factory = new PicoFactory();

    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
        factory.addClass(StepDefs.class);

        // Scenario 1
        factory.start();
        StepDefs o1 = factory.getInstance(StepDefs.class);
        factory.stop();

        // Scenario 2
        factory.start();
        StepDefs o2 = factory.getInstance(StepDefs.class);
        factory.stop();

        assertNotNull(o1);
        assertNotSame(o1, o2);
    }

    @Test
    public void shouldDisposeOnStop() {
        // Given
        factory.addClass(StepDefs.class);

        // When
        factory.start();
        StepDefs steps = factory.getInstance(StepDefs.class);

        // Then
        assertFalse(steps.getBelly().isDisposed());

        // When
        factory.stop();

        // Then
        assertTrue(steps.getBelly().isDisposed());
    }

    @Test
    public void shouldGetStringInstance() {
        // Given
        factory.addClass(String.class);

        // When
        factory.start();
        String str = factory.getInstance(String.class);

        // Then
        assertNotNull(str);
    }

    @Test
    public void shouldGetDoubleInstance() {
        // Given
        factory.addClass(Double.class);

        // When
        factory.start();
        Double aDouble = factory.getInstance(Double.class);

        // Then
        assertNotNull(aDouble);
    }

    @Test
    public void shouldGetIntegerInstance() {
        // Given
        factory.addClass(Integer.class);

        // When
        factory.start();
        Integer integer = factory.getInstance(Integer.class);

        // Then
        assertNotNull(integer);
    }

    @Test
    public void shouldGetLongInstance() {
        // Given
        factory.addClass(Long.class);

        // When
        factory.start();
        Long aLong = factory.getInstance(Long.class);

        // Then
        assertNotNull(aLong);
    }

    @Test
    public void shouldGetBooleanInstance() {
        // Given
        factory.addClass(Boolean.class);

        // When
        factory.start();
        Boolean aBoolean = factory.getInstance(Boolean.class);

        // Then
        assertNotNull(aBoolean);
    }

    @Test
    public void shouldGetCharacterInstance() {
        // Given
        factory.addClass(Character.class);

        // When
        factory.start();
        Character character = factory.getInstance(Character.class);

        // Then
        assertNotNull(character);
    }

    @Test
    public void shouldGetShortInstance() {
        // Given
        factory.addClass(Short.class);

        // When
        factory.start();
        Short aShort = factory.getInstance(Short.class);

        // Then
        assertNotNull(aShort);
    }

    @Test
    public void shouldGetByteInstance() {
        // Given
        factory.addClass(Byte.class);

        // When
        factory.start();
        Byte aByte = factory.getInstance(Byte.class);

        // Then
        assertNotNull(aByte);
    }

    @Test
    public void shouldGetFloatInstance() {
        // Given
        factory.addClass(Float.class);

        // When
        factory.start();
        Float aFloat = factory.getInstance(Float.class);

        // Then
        assertNotNull(aFloat);
    }
}
