require 'erb'

module CucumberJavaMappings
  WORLD_VARIABLE_LOG_FILE        = "world_variable.log"
  WORLD_FUNCTION_LOG_FILE        = "world_function.log"
  DATA_TABLE_LOG_FILE            = "data_table.log"

  def features_dir
    "src/test/resources"
  end

  def run_scenario(scenario_name)
    write_pom
    write_test_unit_classes
    run_simple "mvn test", false
  end

  def run_feature
    write_pom
    write_test_unit_classes
    run_simple "mvn test", false
  end

  def write_pom
    write_file('pom.xml', <<-EOF)
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                      http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>info.cukes</groupId>
        <artifactId>cucumber-jvm</artifactId>
        <relativePath>../../pom.xml</relativePath>
        <version>1.0.0.RC2-SNAPSHOT</version>
    </parent>

    <artifactId>cucumber-picocontainer-test</artifactId>
    <version>1.0.0.RC2-SNAPSHOT</version>
    <packaging>jar</packaging>
    <name>Cucumber: PicoContainer Test</name>

    <dependencies>
        <dependency>
            <groupId>info.cukes</groupId>
            <artifactId>cucumber-picocontainer</artifactId>
            <version>1.0.0.RC2-SNAPSHOT</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>info.cukes</groupId>
            <artifactId>cucumber-junit</artifactId>
            <version>1.0.0.RC2-SNAPSHOT</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.10</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>1.7.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
EOF
  end

  @@mappings_counter = 1

  def write_passing_mapping(step_name)
    erb = ERB.new(<<-EOF, nil, '-')
package cucumber.test;

import cucumber.annotation.en.Given;

public class Mappings<%= @@mappings_counter %> {
    @Given("<%= step_name -%>")
    public void <%= step_name.gsub(/[\s:]/, '_') -%>() {
        // ARUBA_IGNORE_START
        try {
            new java.io.FileWriter("<%= step_file(step_name) %>");
        } catch(java.io.IOException e) {
            throw new RuntimeException(e);
        }
        // ARUBA_IGNORE_END
    }
}

EOF
    write_file("src/test/java/cucumber/test/Mappings#{@@mappings_counter}.java", erb.result(binding))
    @@mappings_counter += 1
  end

  def write_failing_mapping(step_name)
    write_failing_mapping_with_message(step_name, "bang!")
  end

  def write_failing_mapping_with_message(step_name, message)
    erb = ERB.new(<<-EOF, nil, '-')
package cucumber.test;

import cucumber.annotation.en.Given;

public class Mappings<%= @@mappings_counter %> {
    @Given("<%= step_name -%>")
    public void <%= step_name.gsub(/[\s:]/, '_') -%>() {
        // ARUBA_IGNORE_START
        try {
            new java.io.FileWriter("<%= step_file(step_name) %>");
        } catch(java.io.IOException e) {
            throw new RuntimeException(e);
        }
        // ARUBA_IGNORE_END
        throw new RuntimeException("<%= message %>");
    }
}

EOF
    write_file("src/test/java/cucumber/test/Mappings#{@@mappings_counter}.java", erb.result(binding))
    @@mappings_counter += 1
  end

  def write_pending_mapping(step_name)
    erb = ERB.new(<<-EOF, nil, '-')
package cucumber.test;

import cucumber.annotation.en.Given;
import cucumber.annotation.Pending;

public class Mappings<%= @@mappings_counter %> {
    @Pending
    @Given("<%= step_name -%>")
    public void <%= step_name.gsub(/[\s:]/, '_') -%>() {
        // ARUBA_IGNORE_START
        try {
            new java.io.FileWriter("<%= step_file(step_name) %>");
        } catch(java.io.IOException e) {
            throw new RuntimeException(e);
        }
        // ARUBA_IGNORE_END
    }
}

EOF
    write_file("src/test/java/cucumber/test/Mappings#{@@mappings_counter}.java", erb.result(binding))
    @@mappings_counter += 1
  end

  def write_mapping_receiving_data_table_as_raw(step_name)
    erb = ERB.new(<<-EOF, nil, '-')
package cucumber.test;

import cucumber.annotation.en.Given;
import cucumber.table.DataTable;

public class Mappings<%= @@mappings_counter %> {
  
    @Given("<%= step_name -%>")
    public void <%= step_name.gsub(/[\s:]/, '_') -%>(DataTable table) {
        // ARUBA_IGNORE_START
        try {
            java.io.Writer w = new java.io.FileWriter("<%= DATA_TABLE_LOG_FILE %>");
            w.write(new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(table.raw()));
            w.flush();
            w.close();
        } catch(java.io.IOException e) {
            throw new RuntimeException(e);
        }
        // ARUBA_IGNORE_END
    }
}

EOF
    write_file("src/test/java/cucumber/test/Mappings#{@@mappings_counter}.java", erb.result(binding))
    @@mappings_counter += 1
  end

  def write_mapping_receiving_data_table_as_hashes(step_name)
    erb = ERB.new(<<-EOF, nil, '-')
package cucumber.test;

import cucumber.annotation.en.Given;
import cucumber.table.DataTable;
import java.util.List;
import java.util.Map;

public class Mappings<%= @@mappings_counter %> {

    @Given("<%= step_name -%>")
    public void <%= step_name.gsub(/[\s:]/, '_') -%>(List<Map<String, String>> maps) {
        // ARUBA_IGNORE_START
        try {
            java.io.Writer w = new java.io.FileWriter("<%= DATA_TABLE_LOG_FILE %>");
            w.write(new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(maps));
            w.flush();
            w.close();
        } catch(java.io.IOException e) {
            throw new RuntimeException(e);
        }
        // ARUBA_IGNORE_END
    }
}

EOF
    write_file("src/test/java/cucumber/test/Mappings#{@@mappings_counter}.java", erb.result(binding))
    @@mappings_counter += 1
  end

  def write_world_variable_with_numeric_value(value)
    erb = ERB.new(<<-EOF, nil, '-')
package cucumber.test;

public class SomeValue {
    public int value = <%= value %>;
}

EOF
    write_file("src/test/java/cucumber/test/SomeValue.java", erb.result(binding))
  end

  def write_mapping_incrementing_world_variable_by_value(step_name, increment_value)
    erb = ERB.new(<<-EOF, nil, '-')
package cucumber.test;

import cucumber.annotation.en.Given;

public class IncrementsSomeValue<%= @@mappings_counter %> {
    private final SomeValue someValue;

    public IncrementsSomeValue<%= @@mappings_counter %>(SomeValue someValue) {
        this.someValue = someValue;
    }
  
    @Given("<%= step_name -%>")
    public void <%= step_name.gsub(/[\s:]/, '_') -%>() {
        someValue.value += <%= increment_value %>;
    }
}

EOF
    write_file("src/test/java/cucumber/test/IncrementsSomeValue#{@@mappings_counter}.java", erb.result(binding))
    @@mappings_counter += 1
  end

  def write_mapping_logging_world_variable_value(step_name, time = "1")
    erb = ERB.new(<<-EOF, nil, '-')
package cucumber.test;

import cucumber.annotation.en.Given;

public class WritesSomeValue<%= @@mappings_counter %> {
    private final SomeValue someValue;

    public WritesSomeValue<%= @@mappings_counter %>(SomeValue someValue) {
        this.someValue = someValue;
    }

    @Given("<%= step_name -%>")
    public void <%= step_name.gsub(/[\s:]/, '_') -%>() {
        // ARUBA_IGNORE_START
        try {
            java.io.Writer w = new java.io.FileWriter("<%= WORLD_VARIABLE_LOG_FILE %>.<%= time %>");
            w.write(String.valueOf(someValue.value));
            w.flush();
            w.close();
        } catch(java.io.IOException e) {
            throw new RuntimeException(e);
        }
        // ARUBA_IGNORE_END
    }
}

EOF
    write_file("src/test/java/cucumber/test/WritesSomeValue#{@@mappings_counter}.java", erb.result(binding))
    @@mappings_counter += 1
  end

  def write_world_function
    erb = ERB.new(<<-EOF, nil, '-')
package cucumber.test;

public class SomeMethod {
    public void someMethod() {
        // ARUBA_IGNORE_START
        try {
            new java.io.FileWriter("<%= WORLD_FUNCTION_LOG_FILE %>");
        } catch(java.io.IOException e) {
            throw new RuntimeException(e);
        }
        // ARUBA_IGNORE_END
    }
}

EOF
    write_file("src/test/java/cucumber/test/SomeMethod.java", erb.result(binding))
  end

  def write_mapping_calling_world_function(step_name)
    erb = ERB.new(<<-EOF, nil, '-')
package cucumber.test;

import cucumber.annotation.en.Given;

public class InvokesSomeMethod {
    private final SomeMethod someMethod;

    public InvokesSomeMethod(SomeMethod someMethod) {
        this.someMethod = someMethod;
    }

    @Given("<%= step_name -%>")
    public void <%= step_name.gsub(/[\s:]/, '_') -%>() {
        someMethod.someMethod();
    }
}

EOF
    write_file("src/test/java/cucumber/test/InvokesSomeMethod.java", erb.result(binding))
  end

  def write_custom_world_constructor
    # Nothing special to do here.
  end

  def assert_world_function_called
    check_file_presence [WORLD_FUNCTION_LOG_FILE], true
  end

  def assert_world_variable_held_value_at_time(value, time)
    check_exact_file_content "#{WORLD_VARIABLE_LOG_FILE}.#{time}", value
  end

  def assert_data_table_equals_json(json)
    prep_for_fs_check do
      log_file_contents = IO.read(DATA_TABLE_LOG_FILE)
      actual_array      = JSON.parse(log_file_contents)
      expected_array    = JSON.parse(json)
      actual_array.should == expected_array
    end
  end

  def write_calculator_code
    code = <<-EOF
package cucumber.test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class RpnCalculator {
    private final List<Number> stack = new ArrayList<Number>();
    private static final List<String> OPS = asList("-", "+", "*", "/");

    public void push(Object arg) {
        if (OPS.contains(arg)) {
            Number y = stack.remove(stack.size() - 1);
            Number x = stack.remove(stack.size() - 1);
            Double val = null;
            if (arg.equals("-")) {
                val = x.doubleValue() - y.doubleValue();
            } else if (arg.equals("+")) {
                val = x.doubleValue() + y.doubleValue();
            } else if (arg.equals("*")) {
                val = x.doubleValue() * y.doubleValue();
            } else if (arg.equals("/")) {
                val = x.doubleValue() / y.doubleValue();
            }
            push(val);
        } else {
            stack.add((Number) arg);
        }
    }

    public void PI() {
        push(Math.PI);
    }

    public Number value() {
        return stack.get(stack.size() - 1);
    }
}
EOF
    write_file("src/main/java/cucumber/test/RpnCalculator.java", code)
  end

  def write_mappings_for_calculator
    code = <<-EOF
package cucumber.test;

import cucumber.annotation.en.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CalculatorSteps {

    private RpnCalculator calc;

    @Given("^a calculator$")
    public void aCalculator() {
        calc = new RpnCalculator();
    }

    @When("^the calculator computes PI$")
    public void pi() {
        calc.PI();
    }
    
    @When("^the calculator adds up ([\\\\d\\\\.]+) and ([\\\\d\\\\.]+)$")
    public void addDoubles(String n1, String n2) {
        calc.push(Double.parseDouble(n1));
        calc.push(Double.parseDouble(n2));
        calc.push("+");
    }

    @When("^the calculator adds up \\"([^\\"]*)\\" and \\"([^\\"]*)\\"$")
    public void addInts(String n1, String n2) {
        calc.push(Integer.parseInt(n1));
        calc.push(Integer.parseInt(n2));
        calc.push("+");
    }

    @When("^the calculator adds up \\"([^\\"]*)\\", \\"([^\\"]*)\\" and \\"([^\\"]*)\\"$")
    public void addInts(String n1, String n2, String n3) {
        calc.push(Integer.parseInt(n1));
        calc.push(Integer.parseInt(n2));
        calc.push(Integer.parseInt(n3));
        calc.push("+");
        calc.push("+");
    }

    @When("^the calculator adds up the following numbers:")
    public void addInts(String numbers) {
        int pushed = 0;
        for (String number : numbers.split("\\n")) {
            calc.push(Integer.parseInt(number));
            pushed++;
            if(pushed > 1) {
                calc.push("+");
            }
        }
    }
    
    @Then("^the calculator returns PI$")
    public void returnsPI() {
        assertEquals(Math.PI, (Double) calc.value(), 0.00001);
    }

    @Then("^the calculator returns \\"([^\\"]*)\\"$")
    public void returns(String value) {
        assertEquals(Double.parseDouble(value), (Double) calc.value(), 0.00001);
    }

    @Then("^the calculator does not return ([\\\\d\\\\.]+)$")
    public void doesNotReturn(String value) {
        assertTrue(Math.abs(Double.parseDouble(value) - (Double) calc.value()) > 0.00001);
    }
}
EOF
    write_file("src/test/java/cucumber/test/CalculatorSteps.java", code)
  end

  def write_test_unit_classes
    features = in_current_dir do
      Dir.chdir(features_dir) do
        Dir["**/*.feature"]
      end
    end
    features.each do |feature|
      class_name = File.basename(feature).match(/(.*)\.feature$/)[1] + "_Test"
      write_file("src/test/java/cucumber/test/#{class_name}.java", <<-EOF)
package cucumber.test;

import cucumber.junit.Cucumber;
import cucumber.junit.Feature;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@Feature("#{feature}")
public class #{class_name} {}
EOF
    end
  end

  def assert_passing_scenario
    assert_matching_output("Tests run: [1-9]+, Failures: 0, Errors: 0, Skipped: 0", all_output)
    assert_success true
  end

  def assert_failing_scenario
    begin
      assert_matching_output("Tests run: [1-9]+, Failures: 0, Errors: [1-9]+, Skipped: [0-9]+", all_output)
    rescue
      assert_matching_output("Tests run: [1-9]+, Failures: [1-9]+, Errors: 0, Skipped: [0-9]+", all_output)
    end
    assert_success false
  end

  def assert_pending_scenario
    assert_matching_output("Tests run: [1-9]+, Failures: 0, Errors: 0, Skipped: [1-9]+", all_output)
    assert_success true
  end

  def assert_undefined_scenario
    assert_matching_output("Tests run: [1-9]+, Failures: 0, Errors: 0, Skipped: [1-9]+", all_output)
    assert_success true
  end

  def assert_scenario_reported_as_failing(scenario_name)
    # Maven JUnit output is too lame to grab the name...
    assert_failing_scenario
  end
  
  def assert_scenario_not_reported_as_failing(scenario_name)
    # TODO
    # We'd have to look inside surefire reports to determine this. Why is Maven so complicated??
  end

  def failed_output
    /Errors: [1-9]+/
  end
end

World(CucumberJavaMappings)

Before do
  @aruba_timeout_seconds = 30 # Maven is really slow...
end
