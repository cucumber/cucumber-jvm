package io.cucumber.spring.threading;

import io.cucumber.core.cli.Main;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

public class RunParallelCukesTest {

    private final Callable<Byte> runCuke = new Callable<Byte>() {
        @Override
        public Byte call() throws Exception {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String[] args = {
                "--glue", "io.cucumber.spring.threading",
                "classpath:io/cucumber/spring/threadingCukes.feature",
                "--strict"
            };
            return Main.run(args, classLoader);
        }
    };

    @Test
    public void test() {
        ExecutorService executorService = newFixedThreadPool(2);
        Future<Byte> result1 = executorService.submit(runCuke);
        Future<Byte> result2 = executorService.submit(runCuke);

        assertAll("Checking executorService.submit()",
            () -> assertThat(result1.get().byteValue(), is(equalTo(0x0))),
            () -> assertThat(result2.get().byteValue(), is(equalTo(0x0)))
        );
    }

}
