package cucumber.runtime.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

public class UTF8FileWriter extends OutputStreamWriter {
    public UTF8FileWriter(String fileName) throws FileNotFoundException {
        super(new FileOutputStream(fileName), Charset.forName("UTF-8"));
    }

    public UTF8FileWriter(File file) throws FileNotFoundException {
        super(new FileOutputStream(file), Charset.forName("UTF-8"));
    }
}
