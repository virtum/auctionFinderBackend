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

        final RequestModel model1 = new RequestModel(null, "test1", "test");
        final RequestModel model2 = new RequestModel(null, "test2", "test");
        final RequestModel model3 = new RequestModel(null, "test3", "test");

        input.onNext(model1);
        input.onNext(model2);
        input.onNext(model3);

        final List<RequestModel> results = listener.buffer(100, TimeUnit.MILLISECONDS).blockingFirst();

        Assertions.assertThat(results).containsExactly(model1, model2);
    }

    @Test
    public void shouldDelayNextElementsIfLimitIsExceeded() {
        final ThrottleGuard guard = new ThrottleGuard();
        final PublishSubject<RequestModel> input = PublishSubject.create();

        final Observable<RequestModel> output = guard.throttle(input, 1000, 2);

        final ReplaySubject<RequestModel> listener = ReplaySubject.create();
        output.subscribe(listener);

        final RequestModel model1 = new RequestModel(null, "test1", "test");
        final RequestModel model2 = new RequestModel(null, "test2", "test");
        final RequestModel model3 = new RequestModel(null, "test3", "test");
        final RequestModel model4 = new RequestModel(null, "test4", "test");
        final RequestModel model5 = new RequestModel(null, "test5", "test");

        input.onNext(model1);
        input.onNext(model2);
        input.onNext(model3);
        input.onNext(model4);
        input.onNext(model5);

        final  List<RequestModel> results = listener.buffer(1100, TimeUnit.MILLISECONDS).blockingFirst();

        Assertions.assertThat(results).containsExactly(model1, model2, model3, model4);
    }
}
