package cucumber.resources;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class Decoder {
    public String decode(String string) throws UnsupportedEncodingException {
        return URLDecoder.decode(string, "UTF-8");
    }
}