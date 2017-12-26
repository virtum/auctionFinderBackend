package com.filocha.storage;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

class CompletableFutureExtension {

    // TODO http://www.nurkiewicz.com/2014/12/asynchronous-timeouts-with.html - add documentation based on url data
    public static <T> CompletableFuture<T> within(CompletableFuture<T> future, Duration duration) {
        final CompletableFuture<T> timeout = failAfter(duration);
        return future.applyToEither(timeout, Function.identity());
    }

    private static <T> CompletableFuture<T> failAfter(Duration duration) {
        final CompletableFuture<T> promise = new CompletableFuture<>();
        Executors.newScheduledThreadPool(1)
                .schedule(() -> {
                    final TimeoutException ex = new TimeoutException("Timeout after " + duration);
                    return promise.completeExceptionally(ex);
                }, duration.toMillis(), TimeUnit.MILLISECONDS);

        return promise;
    }
}
