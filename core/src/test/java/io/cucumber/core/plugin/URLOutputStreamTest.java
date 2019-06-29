package io.cucumber.core.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;
import org.webbitserver.netty.NettyWebServer;
import org.webbitserver.rest.Rest;

public class URLOutputStreamTest {
    private static final URL CUCUMBER_STEPDEFS = createUrl("http://localhost:9873/.cucumber/stepdefs.json");

    private static URL createUrl(String s) {
        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private WebServer webbit;
    private final int threadsCount = 100;
    private final long waitTimeoutMillis = 30000L;
    private final List<File> tmpFiles = new ArrayList<File>();
    private final List<String> threadErrors = new ArrayList<String>();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

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
        URL urlWithoutParentDirectory = createUrl(baseURL + "/non/existing/directory");


        Writer w = TestUTF8OutputStreamWriter.create(new URLOutputStream(urlWithoutParentDirectory));
        w.write("Hellesøy");
        w.close();

        File testFile = new File(urlWithoutParentDirectory.toURI());
        assertEquals("Hellesøy", read(testFile));
    }

    @Test
    public void can_write_to_file() throws IOException {
        File tmp = File.createTempFile("cucumber-jvm", "tmp");
        Writer w = TestUTF8OutputStreamWriter.create(new URLOutputStream(tmp.toURI().toURL()));
        w.write("Hellesøy");
        w.close();
        assertEquals("Hellesøy", read(tmp));
    }

    @Test
    public void can_http_put() throws IOException, InterruptedException {
        final BlockingQueue<String> data = new LinkedBlockingDeque<String>();
        Rest r = new Rest(webbit);
        r.PUT("/.cucumber/stepdefs.json", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest req, HttpResponse res, HttpControl ctl) {
                data.offer(req.body());
                res.end();
            }
        });

        Writer w = TestUTF8OutputStreamWriter.create(new URLOutputStream(CUCUMBER_STEPDEFS));
        w.write("Hellesøy");
        w.flush();
        w.close();
        assertEquals("Hellesøy", data.poll(1000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void throws_fnfe_if_http_response_is_404() throws IOException {
        Writer w = TestUTF8OutputStreamWriter.create(new URLOutputStream(CUCUMBER_STEPDEFS));
        w.write("Hellesøy");
        w.flush();
        try {
            w.close();
            fail();
        } catch (FileNotFoundException expected) {
        }
    }

    @Test
    public void throws_ioe_if_http_response_is_500() throws IOException {
        Rest r = new Rest(webbit);
        r.PUT("/.cucumber/stepdefs.json", new HttpHandler() {
            @Override
            public void handleHttpRequest(HttpRequest req, HttpResponse res, HttpControl ctl) {
                res.status(500);
                res.content("something went wrong");
                res.end();
            }
        });

        Writer w = TestUTF8OutputStreamWriter.create(new URLOutputStream(CUCUMBER_STEPDEFS));
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

    @Test
    public void do_not_throw_ioe_if_parent_dir_created_by_another_thread() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        List<Thread> testThreads = getThreadsWithLatchForFile(countDownLatch, threadsCount);
        startThreadsFromList(testThreads);
        countDownLatch.countDown();
        waitAllThreadsFromList(testThreads);
        assertTrue("Not all parent folders were created for tmp file or tmp file was not created", isAllFilesCreated());
        assertTrue("Some thread get error during work. Error list:" + threadErrors.toString(), threadErrors.isEmpty());
    }

    private String read(File testFile) throws IOException {
        String result;
        try (BufferedReader br = new BufferedReader(openUTF8FileReader(testFile))) {
            result = br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        return result;
    }

    private Reader openUTF8FileReader(final File file) throws IOException {
        return new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
    }

    private List<Thread> getThreadsWithLatchForFile(final CountDownLatch countDownLatch, int threadsCount) {
        List<Thread> result = new ArrayList<Thread>();
        String ballast = "" + System.currentTimeMillis();
        for (int i = 0; i < threadsCount; i++) {
            final int curThreadNo = i;
            // It useful when 2-3 threads (not more) tries to create the same directory for the report
            final File tmp = (i % 3 == 0 || i % 3 == 2) ?
                new File(tempFolder.getRoot().getAbsolutePath() + "/cuce" + ballast + i + "/tmpFile.tmp") :
                new File(tempFolder.getRoot().getAbsolutePath() + "/cuce" + ballast + (i - 1) + "/tmpFile.tmp");
            tmpFiles.add(tmp);
            result.add(new Thread() {
                @Override
                public void run() {
                    try {
                        // Every thread should wait command to run
                        countDownLatch.await();
                        new URLOutputStream(tmp.toURI().toURL());
                    } catch (IOException e) {
                        threadErrors.add("Thread" + curThreadNo + ": parent dir not created. " + e.getMessage());
                    } catch (InterruptedException e) {
                        threadErrors.add("Thread" + curThreadNo + ": not started on time. " + e.getMessage());
                    }
                }
            });
        }
        return result;
    }

    private void startThreadsFromList(List<Thread> threads) {
        for (Thread thread : threads) {
            thread.start();
        }
    }

    private void waitAllThreadsFromList(List<Thread> threads) {
        long timeStart = System.currentTimeMillis();
        do {
            // Protection from forever loop
            if (System.currentTimeMillis() - timeStart > waitTimeoutMillis) {
                assertTrue("Some threads are still alive", false);
            }
        } while (hasListAliveThreads(threads));
    }

    private boolean hasListAliveThreads(List<Thread> threads) {
        for (Thread thread : threads) {
            if (thread.isAlive()) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllFilesCreated() {
        for (File tmpFile : tmpFiles) {
            if (tmpFile.getParentFile() == null || !tmpFile.getParentFile().isDirectory() || !tmpFile.exists()) {
                return false;
            }
        }
        return true;
    }
}
