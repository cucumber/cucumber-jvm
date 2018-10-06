package cucumber.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import cucumber.api.CucumberOptions;
import cucumber.api.SnippetType;
import org.junit.Test;

public class PropertiesFileCucumberOptionsProviderTest {

  private PropertiesFileCucumberOptionsProvider target;

  @Test
  public void testExtraGlue() {
    System.setProperty(
        PropertiesFileCucumberOptionsProvider.PROPERTIES_FILE_PATH_KEY,
        PropertiesFileCucumberOptionsProviderTest.class
            .getResource("cucumberExtraGlue.properties")
            .getPath());
    target = new PropertiesFileCucumberOptionsProvider();
    CucumberOptions options = target.getOptions();
    assertThat(options.extraGlue())
        .as("verify glue")
        .isEqualTo(
            new String[] {"io.github.martinschneider.steps3", "io.github.martinschneider.steps4"});
  }

  @Test
  public void testMultipleFeaturePaths() {
    System.setProperty(
        PropertiesFileCucumberOptionsProvider.PROPERTIES_FILE_PATH_KEY,
        PropertiesFileCucumberOptionsProviderTest.class
            .getResource("cucumberMultipleFeaturePaths.properties")
            .getPath());
    target = new PropertiesFileCucumberOptionsProvider();
    CucumberOptions options = target.getOptions();
    assertThat(options.features())
        .as("verify feature paths")
        .isEqualTo(new String[] {"src/test/resources/features1", "src/test/resources/features2"});
  }

  @Test
  public void testMultipleGlue() {
    System.setProperty(
        PropertiesFileCucumberOptionsProvider.PROPERTIES_FILE_PATH_KEY,
        PropertiesFileCucumberOptionsProviderTest.class
            .getResource("cucumberMultipleGlue.properties")
            .getPath());
    target = new PropertiesFileCucumberOptionsProvider();
    CucumberOptions options = target.getOptions();
    assertThat(options.glue())
        .as("verify glue")
        .isEqualTo(
            new String[] {"io.github.martinschneider.steps1", "io.github.martinschneider.steps2"});
  }

  @Test
  public void testSingleFeaturesPath() {
    System.setProperty(
        PropertiesFileCucumberOptionsProvider.PROPERTIES_FILE_PATH_KEY,
        PropertiesFileCucumberOptionsProviderTest.class
            .getResource("cucumberSingleFeaturesPath.properties")
            .getPath());
    target = new PropertiesFileCucumberOptionsProvider();
    CucumberOptions options = target.getOptions();
    assertThat(options.features())
        .as("verify feature path")
        .isEqualTo(new String[] {"src/test/resources/features1"});
  }

  @Test
  public void testSingleGlue() {
    System.setProperty(
        PropertiesFileCucumberOptionsProvider.PROPERTIES_FILE_PATH_KEY,
        PropertiesFileCucumberOptionsProviderTest.class
            .getResource("cucumberSingleGlue.properties")
            .getPath());
    target = new PropertiesFileCucumberOptionsProvider();
    CucumberOptions options = target.getOptions();
    assertThat(options.glue())
        .as("verify feature path")
        .isEqualTo(new String[] {"io.github.martinschneider.steps1"});
  }

  @Test
  public void testStrict() {
    System.setProperty(
        PropertiesFileCucumberOptionsProvider.PROPERTIES_FILE_PATH_KEY,
        PropertiesFileCucumberOptionsProviderTest.class
            .getResource("cucumberStrict.properties")
            .getPath());
    target = new PropertiesFileCucumberOptionsProvider();
    CucumberOptions options = target.getOptions();
    assertThat(options.strict()).as("verify strict").isTrue();
  }

  @Test
  public void testNonStrict() {
    System.setProperty(
        PropertiesFileCucumberOptionsProvider.PROPERTIES_FILE_PATH_KEY,
        PropertiesFileCucumberOptionsProviderTest.class
            .getResource("cucumberNonStrict.properties")
            .getPath());
    target = new PropertiesFileCucumberOptionsProvider();
    CucumberOptions options = target.getOptions();
    assertThat(options.strict()).as("verify strict").isFalse();
  }

  @Test
  public void testSnippetsUnderscore() {
    System.setProperty(
        PropertiesFileCucumberOptionsProvider.PROPERTIES_FILE_PATH_KEY,
        PropertiesFileCucumberOptionsProviderTest.class
            .getResource("cucumberSnippetsUnderscore.properties")
            .getPath());
    target = new PropertiesFileCucumberOptionsProvider();
    CucumberOptions options = target.getOptions();
    assertThat(options.snippets()).as("verify snippet").isEqualTo(SnippetType.UNDERSCORE);
  }

  @Test
  public void testSnippetsCamelCase() {
    System.setProperty(
        PropertiesFileCucumberOptionsProvider.PROPERTIES_FILE_PATH_KEY,
        PropertiesFileCucumberOptionsProviderTest.class
            .getResource("cucumberSnippetsCamelCase.properties")
            .getPath());
    target = new PropertiesFileCucumberOptionsProvider();
    CucumberOptions options = target.getOptions();
    assertThat(options.snippets()).as("verify snippet").isEqualTo(SnippetType.CAMELCASE);
  }
}
