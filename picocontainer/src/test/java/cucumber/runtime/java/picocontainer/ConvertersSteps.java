package cucumber.runtime.java.picocontainer;

import java.util.List;

import junit.framework.Assert;
import cucumber.annotation.en.Given;
import cucumber.runtime.xstream.annotations.XStreamConverter;
import cucumber.runtime.xstream.converters.extended.ToStringConverter;

/**
 * tests various ways of mix and matching xstream converters on plain steps and data tables 
 */
public class ConvertersSteps {

	@Given("^I have some foo named \"([^\"]*)\"$")
	public void I_have_some_foo_named(Foo foo) {
		Assert.assertEquals("MyFoo", foo.getName());
	}
	
	@Given("^I have some bar named \"([^\"]*)\"$")
	public void I_have_some_bar_named(BarSubclass bar) {
		Assert.assertEquals("mybar", bar.getName());
	}
	
	@Given("^I have some baz named \"([^\"]*)\"$")
	public void I_have_some_baz_named(BazHolder bazHolder) {
		Assert.assertEquals("MyBaz", bazHolder.getBaz().getName());
	}

	@Given("^I have some stuff in a data table:$")
	public void I_have_some_stuff_in_a_data_table(List<Stuff> stuffs) {
		for (Stuff stuff : stuffs) {
			Assert.assertEquals("myfoo2", stuff.foo.getName());
			Assert.assertEquals("MyBar2", stuff.bar.getName());
			Assert.assertEquals("mybaz2", stuff.baz.getName());
		}
	}

	public static class Stuff {
		@XStreamConverter(LowerCaseToStringConverter.class)
		public Foo foo;
		@XStreamConverter(ToStringConverter.class)
		public Bar bar;
		@XStreamConverter(LowerCaseToStringConverter.class)
		public Baz baz;
	}
	
	@XStreamConverter(LowerCaseToStringConverter.class)
	public static class BarSubclass extends Bar {
		public BarSubclass(String name) {
			super(name);
		}
	}
	
	@XStreamConverter(ToStringConverter.class)
	public static class BazHolder {
		private Baz baz;
		public BazHolder(String name) {
			this.baz = new Baz(name);
		}
		public Baz getBaz() {
			return baz;
		}
	}
	
	@XStreamConverter(ToStringConverter.class)
	public static class Foo extends Thing {
		public Foo(String name) {
			super(name);
		}
	}
	public static class Bar extends Thing {
		public Bar(String name) {
			super(name);
		}
	}
	public static final class Baz extends Thing {
		public Baz(String name) {
			super(name);
		}
	}
	public static class Thing {
		private String name;
		public Thing(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		@Override
		public String toString() {
			return super.toString();
		}
	}
	
	public static class LowerCaseToStringConverter extends ToStringConverter {
		public LowerCaseToStringConverter(Class<?> clazz) throws NoSuchMethodException {
			super(clazz);
		}
		@Override
		public Object fromString(String str) {
			return super.fromString(str.toLowerCase());
		}
	}
}