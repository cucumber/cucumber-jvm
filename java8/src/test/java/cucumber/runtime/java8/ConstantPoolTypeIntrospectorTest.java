package cucumber.runtime.java8;

import cucumber.runtime.CucumberException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sun.reflect.ConstantPool;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class ConstantPoolTypeIntrospectorTest {
    private final ConstantPoolTypeIntrospector constantPoolTypeIntrospector = new ConstantPoolTypeIntrospector();
    @Mock
    private ConstantPool constantPool;

    @Test(expected = CucumberException.class)
    public void should_throw_CucumberException_when_constant_pool_is_empty() {
        //given
        given(constantPool.getSize()).willReturn(0);

        //when
        constantPoolTypeIntrospector.getLambdaTypeString(constantPool);
    }

    @Test(expected = CucumberException.class)
    public void should_throw_CucumberException_when_all_elements_throw_IllegalArgumentException() {
        //given
        given(constantPool.getSize()).willReturn(3);
        givenThrowIllegalArgumentExceptionAt(0, 1, 2);

        //when
        constantPoolTypeIntrospector.getLambdaTypeString(constantPool);
    }

    private void givenThrowIllegalArgumentExceptionAt(int... indexes) {
        for (int i : indexes) {
            given(constantPool.getMemberRefInfoAt(i)).willThrow(new IllegalArgumentException());
        }
    }

    @Test(expected = CucumberException.class)
    public void should_throw_CucumberException_when_members_ref_has_less_than_3_elements() {
        //given
        given(constantPool.getSize()).willReturn(1);
        given(constantPool.getMemberRefInfoAt(0)).willReturn(new String[]{"a", "b"});

        //when
        String memberRef = constantPoolTypeIntrospector.getLambdaTypeString(constantPool);

        //then
        assertEquals("memberRef", memberRef);
    }

    @Test
    public void should_return_member_ref_when_some_element_returns_member_ref() {
        //given
        given(constantPool.getSize()).willReturn(3);
        givenThrowIllegalArgumentExceptionAt(0, 2);
        given(constantPool.getMemberRefInfoAt(1)).willReturn(new String[]{"a", "b", "memberRef"});

        //when
        String memberRef = constantPoolTypeIntrospector.getLambdaTypeString(constantPool);

        //then
        assertEquals("memberRef", memberRef);
    }

    @Test
    public void should_return_member_ref_when_first_element_returns_member_ref() {
        //given
        given(constantPool.getSize()).willReturn(3);
        given(constantPool.getMemberRefInfoAt(0)).willReturn(new String[]{"a", "b", "memberRef"});
        givenThrowIllegalArgumentExceptionAt(1, 2);

        //when
        String memberRef = constantPoolTypeIntrospector.getLambdaTypeString(constantPool);

        //then
        assertEquals("memberRef", memberRef);
    }

    @Test
    public void should_return_member_ref_when_last_element_returns_member_ref() {
        //given
        given(constantPool.getSize()).willReturn(3);
        given(constantPool.getMemberRefInfoAt(2)).willReturn(new String[]{"a", "b", "memberRef"});
        givenThrowIllegalArgumentExceptionAt(0, 1);

        //when
        String memberRef = constantPoolTypeIntrospector.getLambdaTypeString(constantPool);

        //then
        assertEquals("memberRef", memberRef);
    }
}
