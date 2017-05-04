package cucumber.api;

import java.util.Collection;

/**
 * Before or After Hooks that declare a parameter of this type will receive an instance of this class.
 * It allows writing text and embedding media into reports, as well as inspecting results (in an After block).
 * Use @Before with a call to getFeature() on the Scenario object to get the instance of the Feature
 * e.g.
 * {@code
 * public void get_feature_name(Scenario scenario) {
 *     Feature feature = scenario.getFeature();
 *     featureName = feature.getName();
 *     tags = feature.getSourceTagNames();
 * }}
 */
public interface Feature {
    /**
     * @return source_tag_names. Needed for compatibility with Capybara.
     */
    Collection<String> getSourceTagNames();

    /**
     * @return the <em>most severe</em> status of the Scenario's Steps. One of "passed", "undefined", "pending", "skipped", "failed"
     */
    String getStatus();

    /**
     * @return true if and only if {@link #getStatus()} returns "failed"
     */
    boolean isFailed();

    /**
     * Embeds data into the report(s). Some reporters (such as the progress one) don't embed data, but others do (html and json).
     * Example:
     *
     * <pre>
     * {@code
     * // Embed a screenshot. See your UI automation tool's docs for
     * // details about how to take a screenshot.
     * scenario.embed(pngBytes, "image/png");
     * }
     * </pre>
     *
     * @param data     what to embed, for example an image.
     * @param mimeType what is the data?
     */
    void embed(byte[] data, String mimeType);

    /**
     * Outputs some text into the report.
     *
     * @param text what to put in the report.
     */
    void write(String text);

    /**
     *
     * @return the name of the Scenario
     */
    String getName();

    /**
     * @return the id of the Scenario.
     */
    String getId();


}
