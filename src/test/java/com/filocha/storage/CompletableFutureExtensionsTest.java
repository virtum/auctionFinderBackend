package com.filocha.storage;

import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class CompletableFutureExtensionsTest {

    @Test(expected = ExecutionException.class)
    public void shouldNotWaitForResponseAfterTimeout() throws ExecutionException, InterruptedException {
        CompletableFutureExtension
                .within(new CompletableFuture<>(), Duration.ofSeconds(1))
                .get();
    }

    @Test
    public void shouldConsumeCompletableFutureBeforeTimeout() throws ExecutionException, InterruptedException {
        final CompletableFuture<String> completableFuture = new CompletableFuture<>();

        final CompletableFuture<String> futureWithTimeout = CompletableFutureExtension.within(completableFuture, Duration.ofSeconds(10));

        final String result = "finished";
        completableFuture.complete(result);

        assertEquals(result, futureWithTimeout.get());
    }

}