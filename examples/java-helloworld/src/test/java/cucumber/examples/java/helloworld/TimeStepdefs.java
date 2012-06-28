package cucumber.examples.java.helloworld;

import cucumber.annotation.en.Given;
import cucumber.runtime.xstream.converters.Converter;
import cucumber.runtime.xstream.converters.MarshallingContext;
import cucumber.runtime.xstream.converters.UnmarshallingContext;
import cucumber.runtime.xstream.io.HierarchicalStreamReader;
import cucumber.runtime.xstream.io.HierarchicalStreamWriter;

public class TimeStepdefs {
    @Given("^I did laundry (.*) ago$")
    public void I_did_laundry_time_ago(HumanTime time) throws Throwable {
    }

    @cucumber.runtime.xstream.annotations.XStreamConverter(HumanTimeConverter.class)
    public static class HumanTime {
    }

    public static class HumanTimeConverter implements Converter {
        @Override
        public boolean canConvert(Class aClass) {
            return aClass.equals(HumanTime.class);
        }

        @Override
        public void marshal(Object o, HierarchicalStreamWriter hierarchicalStreamWriter, MarshallingContext marshallingContext) {

        }

        @Override
        public Object unmarshal(HierarchicalStreamReader hierarchicalStreamReader, UnmarshallingContext unmarshallingContext) {
            return null;
        }
    }
}
