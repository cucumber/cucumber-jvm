package cucumber.runtime.model;

import cucumber.api.event.TestSourceRead;
import cucumber.runner.EventBus;
import cucumber.runtime.CucumberException;
import cucumber.util.FixJava;
import io.cucumber.gherkin.SubprocessCucumberMessages;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.GherkinDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.singletonList;

public class CucumberFeature implements Serializable {
    private static final long serialVersionUID = 1L;
    private GherkinDocument gherkinDocument;
    private String gherkinSource;
    private List<Messages.Pickle> pickles;

    private CucumberFeature(GherkinDocument gherkinDocument, String gherkinSource, List<Messages.Pickle> pickles) {
        this.gherkinDocument = gherkinDocument;
        this.gherkinSource = gherkinSource;
        this.pickles = pickles;
    }

    public static CucumberFeature fromFile(File file) {
        SubprocessCucumberMessages cucumberMessages = new SubprocessCucumberMessages(singletonList(file.getAbsolutePath()), true, true, true);
        Messages.GherkinDocument gherkinDocument = null;
        String gherkinSource = null;
        List<Messages.Pickle> pickles = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();
        for (Messages.Wrapper wrapper : cucumberMessages.messages()) {
            if (wrapper.hasGherkinDocument()) {
                gherkinDocument = wrapper.getGherkinDocument();
            }
            if (wrapper.hasSource()) {
                gherkinSource = wrapper.getSource().getData();
            }
            if (wrapper.hasPickle()) {
                pickles.add(wrapper.getPickle());
            }
            if (wrapper.hasAttachment()) {
                // Parse error
                wrapper.getAttachment().getData();
                errorMessages.add(wrapper.getAttachment().getData());
            }
        }
        if (!errorMessages.isEmpty()) {
            String errorMessage = FixJava.join(errorMessages, "\n") + "\nSource:\n" + gherkinSource;
            throw new CucumberException(errorMessage);
        }
        return new CucumberFeature(gherkinDocument, gherkinSource, pickles);
    }

    public static CucumberFeature fromSourceForTest(final String path, final String source) {
        try {
            File tmpFile = File.createTempFile(path, "");
            tmpFile.deleteOnExit();
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tmpFile), "UTF-8");
            writer.write(source);
            writer.close();
            CucumberFeature cucumberFeature = fromFile(tmpFile);
            cucumberFeature.setUri(path);
            return cucumberFeature;
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

    private void setUri(String uri) {
        gherkinDocument = gherkinDocument.toBuilder().setUri(uri).build();
        List<Messages.Pickle> newPickles = new ArrayList<>();
        for (Messages.Pickle pickle : pickles) {
            newPickles.add(pickle.toBuilder().setUri(uri).build());
        }
        pickles = newPickles;
    }

    public GherkinDocument getGherkinFeature() {
        return gherkinDocument;
    }

    public String getUri() {
        return gherkinDocument.getUri();
    }

    public void sendTestSourceRead(EventBus bus) {
        bus.send(new TestSourceRead(bus.getTime(), gherkinSource));
    }

    public List<Messages.Pickle> getPickles() {
        return pickles;
    }

    public static class CucumberFeatureUriComparator implements Comparator<CucumberFeature> {
        @Override
        public int compare(CucumberFeature a, CucumberFeature b) {
            return a.getUri().compareTo(b.getUri());
        }
    }
}
