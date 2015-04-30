package cucumber.runtime.io;

import cucumber.runtime.Utils;
import gherkin.util.FixJava;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;
import org.webbitserver.netty.NettyWebServer;
import org.webbitserver.rest.Rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class URLOutputStreamTest {
    private WebServer webbit;

    @Before
    public void startWebbit() throws ExecutionException, InterruptedException {
        webbit = new NettyWebServer(Executors.newSingleThreadExecutor(), new InetSocketAddress("127.0.0.1", 9873), URI.create("http://127.0.0.1:9873")).start().get();
    }

    @After
    public void stopWebbit() throws ExecutionException, InterruptedException {
        webbit.stop().get();
    }

    @Test
    public void write_to_file_without_existing_parent_directory() throws IOException, URISyntaxException {
        Path filesWithoutParent = Files.createTempDirectory("filesWithoutParent");
        String baseURL = filesWithoutParent.toUri().toURL().toString();
        URL urlWithoutParentDirectory = new URL(baseURL + "/non/existing/directory");

        Writer w = new UTF8OutputStreamWriter(new URLOutputStream(urlWithoutParentDirectory));
        w.write("Hellesøy");
        w.close();

        File testFile = new File(urlWithoutParentDirectory.toURI());
        assertEquals("Hellesøy", FixJava.readReader(openUTF8FileReader(testFile)));
    }

    @Test
    public void can_write_to_file() throws IOException {
        File tmp = File.createTempFile("cucumber-jvm", "tmp");
        Writer w = new UTF8OutputStreamWriter(new URLOutputStream(tmp.toURI().toURL()));
        w.write("Hellesøy");
        w.close();
        assertEquals("Hellesøy", FixJava.readReader(openUTF8FileReader(tmp)));
    }

    @Test
    public void can_http_put() throws IOException, ExecutionException, InterruptedException {
        final BlockingQueue<String> data = new LinkedBlockingDeque<String>();
        Rest r = new Rest(webbit);
        r.PUT("/.cucumber/stepdefs.json", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest req, HttpResponse res, HttpControl ctl) throws Exception {
                data.offer(req.body());
                res.end();
            }
        });

        Writer w = new UTF8OutputStreamWriter(new URLOutputStream(new URL(Utils.toURL("http://localhost:9873/.cucumber"), "stepdefs.json")));
        w.write("Hellesøy");
        w.flush();
        w.close();
        assertEquals("Hellesøy", data.poll(1000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void throws_fnfe_if_http_response_is_404() throws IOException, ExecutionException, InterruptedException {
        Writer w = new UTF8OutputStreamWriter(new URLOutputStream(new URL(Utils.toURL("http://localhost:9873/.cucumber"), "stepdefs.json")));
        w.write("Hellesøy");
        w.flush();
        try {
            w.close();
            fail();
        } catch (FileNotFoundException expected) {
        }
    }

    @Test
    public void throws_ioe_if_http_response_is_500() throws IOException, ExecutionException, InterruptedException {
        Rest r = new Rest(webbit);
        r.PUT("/.cucumber/stepdefs.json", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest req, HttpResponse res, HttpControl ctl) throws Exception {
                res.status(500);
                res.content("something went wrong");
                res.end();
            }
        });

        Writer w = new UTF8OutputStreamWriter(new URLOutputStream(new URL(Utils.toURL("http://localhost:9873/.cucumber"), "stepdefs.json")));
        w.write("Hellesøy");
        w.flush();
        try {
            w.close();
            fail();
        } catch (IOException expected) {
            assertEquals("PUT http://localhost:9873/.cucumber/stepdefs.json\n" +
                    "HTTP 500\nsomething went wrong", expected.getMessage());
        }
    }

    private Reader openUTF8FileReader(final File file) throws IOException {
        return new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
    }
}
