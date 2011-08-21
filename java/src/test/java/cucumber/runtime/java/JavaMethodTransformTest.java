package cucumber.runtime.java;

import cucumber.annotation.Pending;
import cucumber.annotation.Transform;
import cucumber.runtime.PendingException;
import junit.framework.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JavaMethodTransformTest {

    @Test
    public void shouldTransformToUser() throws Exception {
        HasATransformMethod hasATransformMethod = new HasATransformMethod();
        Method transformMethod = hasATransformMethod.getClass().getMethod("transformToUser", String.class, Integer.TYPE);
        ObjectFactory objectFactory = mock(ObjectFactory.class);
        when(objectFactory.getInstance(HasATransformMethod.class)).thenReturn(hasATransformMethod);
        JavaBackend backend = new JavaBackend(objectFactory, null);
        JavaMethodTransform javaMethodTransform = new JavaMethodTransform(transformMethod, backend);
        Object transformed = javaMethodTransform.transform(Locale.getDefault(), "Cucumber", "3");
        Assert.assertEquals(User.class, transformed.getClass());
        Assert.assertEquals("Cucumber", ((User) transformed).getName());
        Assert.assertEquals(3, ((User) transformed).getAge());
    }

    @Test(expected = PendingException.class)
    public void shouldThrowExceptionWhenUsingPendingTransform() throws Exception {
        HasAPendingTransformMethod hasATransformMethod = new HasAPendingTransformMethod();
        Method transformMethod = hasATransformMethod.getClass().getMethod("transformToUser", String.class);
        ObjectFactory objectFactory = mock(ObjectFactory.class);
        when(objectFactory.getInstance(HasAPendingTransformMethod.class)).thenReturn(hasATransformMethod);
        JavaBackend backend = new JavaBackend(objectFactory, null);
        new JavaMethodTransform(transformMethod, backend).transform(Locale.getDefault());
    }

    public class User {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public User setName(String name) {
            this.name = name;
            return this;
        }

        public User setAge(int age) {
            this.age = age;
            return this;
        }

        public int getAge() {
            return age;
        }
    }

    public class HasATransformMethod {
        @Transform
        public User transformToUser(String name, int age) {
            return new User().setName(name).setAge(age);
        }
    }

    public class HasAPendingTransformMethod {
        @Transform
        @Pending("Testing")
        public User transformToUser(String name) {
            return new User().setName(name);
        }
    }
}
