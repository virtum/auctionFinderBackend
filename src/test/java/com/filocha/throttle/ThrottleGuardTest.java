package com.filocha.throttle;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ThrottleGuardTest {

    @Test
    public void shouldNotAddNewItemAboveLimit() {
        ThrottleGuard guard = new ThrottleGuard();
        PublishSubject<Integer> input = PublishSubject.create();

        Observable<Integer> output = guard.throttle(input, 1000, 2);

        ReplaySubject<Integer> listener = ReplaySubject.create();
        output.subscribe(listener);

        input.onNext(1);
        input.onNext(2);
        input.onNext(3);

        List<Integer> results = listener.buffer(100, TimeUnit.MILLISECONDS).toBlocking().first();

        Assertions.assertThat(results).containsExactly(1, 2);
    }

    @Test
    public void shouldDelayNextElementsIfLimitIsExceeded() {
        ThrottleGuard guard = new ThrottleGuard();
        PublishSubject<Integer> input = PublishSubject.create();

        Observable<Integer> output = guard.throttle(input, 1000, 2);

        ReplaySubject<Integer> listener = ReplaySubject.create();
        output.subscribe(listener);

        input.onNext(1);
        input.onNext(2);
        input.onNext(3);
        input.onNext(4);
        input.onNext(5);

        List<Integer> results = listener.buffer(1100, TimeUnit.MILLISECONDS).toBlocking().first();
        Assertions.assertThat(results).containsExactly(1, 2, 3, 4);
    }
}
