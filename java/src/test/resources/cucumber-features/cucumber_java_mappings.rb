require 'erb'

module CucumberJavaMappings
  def features_dir
    "src/test/resources"
  end

  def run_scenario(scenario_name)
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
        <groupId>cucumber</groupId>
        <artifactId>parent</artifactId>
        <relativePath>../../pom.xml</relativePath>
        <version>0.4.3-SNAPSHOT</version>
    </parent>

    <artifactId>cucumber-picocontainer-test</artifactId>
    <version>0.4.3-SNAPSHOT</version>
    <packaging>jar</packaging>
    <name>Cucumber: PicoContainer Test</name>

    <dependencies>
        <dependency>
            <groupId>cucumber</groupId>
            <artifactId>picocontainer</artifactId>
            <version>0.4.3-SNAPSHOT</version>
        </dependency>
    </dependencies>
</project>
EOF
  end

  @@mappings_counter = 1

  def write_passing_mapping(step_name)
    erb = ERB.new(<<-EOF, nil, '-')
package cucumber.test;

import cucumber.annotation.EN.Given;

public class Mappings<%= @@mappings_counter %> {
  @Given("<%= step_name -%>")
  public void <%= step_name.gsub(/ /, '_') -%>() {
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
    2 tests because we have 2 steps
    assert_partial_output("Tests run: 2, Failures: 0, Errors: 0, Skipped: 0", all_output)
    assert_success true
  end
end

World(CucumberJavaMappings)

Before do
  @aruba_timeout_seconds = 10
end
