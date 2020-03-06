package io.cucumber.core.plugin;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;

@Disabled
class URLOutputStreamTest {

    private final List<File> tmpFiles = new ArrayList<>();
    private final List<String> threadErrors = new ArrayList<>();

    @TempDir
    Path tempDir;

    @Test
    void write_to_file_without_existing_parent_directory() throws IOException, URISyntaxException {
        Path filesWithoutParent = Files.createTempDirectory("filesWithoutParent");
        String baseURL = filesWithoutParent.toUri().toURL().toString();
        URL urlWithoutParentDirectory = new URL(baseURL + "/non/existing/directory");

        Writer w = new UTF8OutputStreamWriter(new URLOutputStream(urlWithoutParentDirectory));
        w.write("Hellesøy");
        w.close();

        File testFile = new File(urlWithoutParentDirectory.toURI());
        assertThat(read(testFile), is(equalTo("Hellesøy")));
    }

    @Test
    void can_write_to_file() throws IOException {
        File tmp = File.createTempFile("cucumber-jvm", "tmp");
        Writer w = new UTF8OutputStreamWriter(new URLOutputStream(tmp.toURI().toURL()));
        w.write("Hellesøy");
        w.close();
        assertThat(read(tmp), is(equalTo("Hellesøy")));
    }

    @Test
    void do_not_throw_ioe_if_parent_dir_created_by_another_thread() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        int threadsCount = 100;
        List<Thread> testThreads = getThreadsWithLatchForFile(countDownLatch, threadsCount);
        startThreadsFromList(testThreads);
        countDownLatch.countDown();
        waitAllThreadsFromList(testThreads);

        assertAll("Checking CountDownLatch",
            () -> assertThat("Not all parent folders were created for tmp file or tmp file was not created", isAllFilesCreated(), is(equalTo(true))),
            () -> assertThat("Some thread get error during work. Error list:" + threadErrors.toString(), threadErrors.isEmpty(), is(equalTo(true)))
        );
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
        List<Thread> result = new ArrayList<>();
        String ballast = "" + System.currentTimeMillis();
        for (int i = 0; i < threadsCount; i++) {
            final int curThreadNo = i;
            // It useful when 2-3 threads (not more) tries to create the same directory for the report
            final File tmp = (i % 3 == 0 || i % 3 == 2) ?
                new File(tempDir.toAbsolutePath().toString() + "/cuce" + ballast + i + "/tmpFile.tmp") :
                new File(tempDir.toAbsolutePath().toString() + "/cuce" + ballast + (i - 1) + "/tmpFile.tmp");
            tmpFiles.add(tmp);
            result.add(new Thread(() -> {
                try {
                    // Every thread should wait command to run
                    countDownLatch.await();
                    try (URLOutputStream urlOutputStream = new URLOutputStream(tmp.toURI().toURL())) {
                    }
                } catch (IOException e) {
                    threadErrors.add("Thread" + curThreadNo + ": parent dir not created. " + e.getMessage());
                } catch (InterruptedException e) {
                    threadErrors.add("Thread" + curThreadNo + ": not started on time. " + e.getMessage());
                }
            }));
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
            long waitTimeoutMillis = 30000L;
            if (System.currentTimeMillis() - timeStart > waitTimeoutMillis) {
                fail("Some threads are still alive");
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
