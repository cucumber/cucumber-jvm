package cucumber.runtime;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Result;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

class ScenarioResultImpl implements ScenarioResult {
    private static final List<String> SEVERITY = asList("passed", "undefined", "pending", "skipped", "failed");
    private final List<Result> stepResults = new ArrayList<Result>();
    private final Reporter reporter;

    public ScenarioResultImpl(Reporter reporter) {
        this.reporter = reporter;
    }

    void add(Result result) {
        stepResults.add(result);
    }

    @Override
    public String getStatus() {
        int pos = 0;
        for (Result stepResult : stepResults) {
            pos = Math.max(pos, SEVERITY.indexOf(stepResult.getStatus()));
        }
        return SEVERITY.get(pos);
    }

    @Override
    public boolean isFailed() {
        return "failed".equals(getStatus());
    }

    private byte[] getBytes(InputStream data) throws IOException {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    	try {
	    	final byte[] buffer = new byte[16*1024];
	    	int available = 0;
	    	while ((available = data.read(buffer)) >= 0)
	    	{
	    	    outputStream.write(buffer, 0, available);
	    	}
	    	outputStream.close();
	    	return outputStream.toByteArray();
    	} finally {
    		outputStream.close();
    	}
    }
    
    @Override
    public void embed(InputStream data, String mimeType) {
    	try {
	        byte[] bytes = getBytes(data);
	    	reporter.embedding(mimeType, bytes);
    	} catch (IOException ioe) {
    		ioe.printStackTrace();//not much else to do here.
    	}
    }

    @Override
    public void write(String text) {
        reporter.write(text);
    }
}
