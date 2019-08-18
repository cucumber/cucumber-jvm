package io.cucumber.spring.threading;

import io.cucumber.core.cli.Main;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class RunParallelCukesTest {

    @Test
    void test() {
        Callable<Byte> runCucumber = () -> {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String[] args = {
                "--glue", "io.cucumber.spring.threading",
                "classpath:io/cucumber/spring/threadingCukes.feature",
                "--strict"
            };
            return Main.run(args, classLoader);
        };


        ExecutorService executorService = newFixedThreadPool(2);
        Future<Byte> result1 = executorService.submit(runCucumber);
        Future<Byte> result2 = executorService.submit(runCucumber);

        assertAll("jobs completed successfully",
            () -> assertThat(result1.get(), is((byte) 0x0)),
            () -> assertThat(result2.get(), is((byte) 0x0))
        );
    }

}
