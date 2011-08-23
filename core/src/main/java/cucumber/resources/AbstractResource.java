package cucumber.resources;

import cucumber.runtime.CucumberException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;

public abstract class AbstractResource implements Resource {
    protected final PathWithLines pathWithLines;

    public AbstractResource(PathWithLines pathWithLines) {
        this.pathWithLines = pathWithLines;
    }

    public String getString() {
        return read(getReader());
    }

    @Override
    public List<Long> getLines() {
        return pathWithLines.lines;
    }

    public Reader getReader() {
        try {
            return new InputStreamReader(getInputStream(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CucumberException("Failed to open path " + getPath(), e);
        }
    }

    private String read(Reader reader) {
        try {
            StringBuilder sb = new StringBuilder();
            int n;
            while ((n = reader.read()) != -1) {
                sb.append((char) n);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new CucumberException("Failed to read", e);
        }
    }
}
