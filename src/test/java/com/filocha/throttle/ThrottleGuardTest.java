package com.filocha.throttle;

import com.filocha.finder.RequestModel;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ThrottleGuardTest {

    @Test
    public void shouldNotAddNewItemAboveLimit() {
        final PublishSubject<RequestModel> input = PublishSubject.create();
        final Observable<RequestModel> output = ThrottleGuard.throttle(input, 1000, 2);
        final ReplaySubject<RequestModel> listener = ReplaySubject.create();
        output.subscribe(listener);

        final RequestModel model1 = RequestModel
                .builder()
                .build();

        final RequestModel model2 = RequestModel
                .builder()
                .build();

        final RequestModel model3 = RequestModel
                .builder()
                .build();

        input.onNext(model1);
        input.onNext(model2);
        input.onNext(model3);

        final List<RequestModel> results = listener.buffer(100, TimeUnit.MILLISECONDS).blockingFirst();

        Assertions.assertThat(results).containsExactly(model1, model2);
    }

    @Test
    public void shouldDelayNextElementsIfLimitIsExceeded() {
        final PublishSubject<RequestModel> input = PublishSubject.create();

        final Observable<RequestModel> output = ThrottleGuard.throttle(input, 1000, 2);

        final ReplaySubject<RequestModel> listener = ReplaySubject.create();
        output.subscribe(listener);

        final RequestModel model1 = RequestModel
                .builder()
                .build();

        final RequestModel model2 = RequestModel
                .builder()
                .build();

        final RequestModel model3 = RequestModel
                .builder()
                .build();

        final RequestModel model4 = RequestModel
                .builder()
                .build();

        final RequestModel model5 = RequestModel
                .builder()
                .build();

        input.onNext(model1);
        input.onNext(model2);
        input.onNext(model3);
        input.onNext(model4);
        input.onNext(model5);

        final List<RequestModel> results = listener.buffer(1100, TimeUnit.MILLISECONDS).blockingFirst();

        Assertions.assertThat(results).containsExactly(model1, model2, model3, model4);
    }
}
