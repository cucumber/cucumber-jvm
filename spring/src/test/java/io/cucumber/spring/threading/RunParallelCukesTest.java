package io.cucumber.spring.threading;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.junit.Assert.assertEquals;

import cucumber.api.cli.Main;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


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
    public void test() throws InterruptedException, ExecutionException {
        ExecutorService executorService = newFixedThreadPool(2);
        Future<Byte> result1 = executorService.submit(runCuke);
        Future<Byte> result2 = executorService.submit(runCuke);
        assertEquals(result1.get().byteValue(), 0x0);
        assertEquals(result2.get().byteValue(), 0x0);
    }
}
