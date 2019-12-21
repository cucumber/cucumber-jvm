package io.cucumber.spring.threading;

import io.cucumber.core.cli.Main;
import org.junit.jupiter.api.RepeatedTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class RunParallelCucumberTest {

    @RepeatedTest(100)
    void test() throws ExecutionException, InterruptedException {
        Callable<Byte> runCucumber = () -> {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String[] args = {
                "--glue", "io.cucumber.spring.threading",
                "classpath:io/cucumber/spring/threadingCukes.feature",
                "--strict"
            };
            return Main.run(args, classLoader);
        };


        ExecutorService executorService = newFixedThreadPool(ThreadingStepDefs.concurrency);
        List<Future<Byte>> results = new ArrayList<>();
        for (int i = 0; i < ThreadingStepDefs.concurrency; i++) {
            results.add(executorService.submit(runCucumber));
        }

        for (Future<Byte> result : results) {
            assertThat(result.get(), is((byte) 0x0));
        }
        ThreadingStepDefs.map.clear();
    }

}
