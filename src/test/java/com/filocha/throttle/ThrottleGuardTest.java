package com.filocha.throttle;

import com.filocha.finder.RequestModel;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ThrottleGuardTest {

    @Test
    public void shouldNotAddNewItemAboveLimit() {
        // given
        final PublishSubject<RequestModel> inputRequests = PublishSubject.create();
        final ReplaySubject<RequestModel> listener = ReplaySubject.create();

        final Observable<RequestModel> output = ThrottleGuard.throttle(inputRequests, 1000, 2);
        output.subscribe(listener);

        final RequestModel request1 = RequestModel
                .builder()
                .build();

        final RequestModel request2 = RequestModel
                .builder()
                .build();

        final RequestModel request3 = RequestModel
                .builder()
                .build();

        // when
        inputRequests.onNext(request1);
        inputRequests.onNext(request2);
        inputRequests.onNext(request3);

        // then
        final List<RequestModel> results = listener.buffer(100, TimeUnit.MILLISECONDS).blockingFirst();

        assertThat(results).containsExactly(request1, request2);
    }

    @Test
    public void shouldDelayNextElementsIfLimitIsExceeded() {
        // given
        final PublishSubject<RequestModel> inputRequests = PublishSubject.create();
        final ReplaySubject<RequestModel> listener = ReplaySubject.create();

        final Observable<RequestModel> output = ThrottleGuard.throttle(inputRequests, 1000, 2);
        output.subscribe(listener);

        final RequestModel request1 = RequestModel
                .builder()
                .build();

        final RequestModel request2 = RequestModel
                .builder()
                .build();

        final RequestModel request3 = RequestModel
                .builder()
                .build();

        final RequestModel request4 = RequestModel
                .builder()
                .build();

        final RequestModel request5 = RequestModel
                .builder()
                .build();

        // when
        inputRequests.onNext(request1);
        inputRequests.onNext(request2);
        inputRequests.onNext(request3);
        inputRequests.onNext(request4);
        inputRequests.onNext(request5);

        // then
        final List<RequestModel> results = listener.buffer(1100, TimeUnit.MILLISECONDS).blockingFirst();

        assertThat(results).containsExactly(request1, request2, request3, request4);
    }
}
