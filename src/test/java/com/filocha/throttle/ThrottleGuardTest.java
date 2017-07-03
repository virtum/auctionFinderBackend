package com.filocha.throttle;

import com.filocha.finder.RequestModel;
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
        PublishSubject<RequestModel> input = PublishSubject.create();

        Observable<RequestModel> output = guard.throttle(input, 1000, 2);

        ReplaySubject<RequestModel> listener = ReplaySubject.create();
        output.subscribe(listener);

        RequestModel model1 = new RequestModel(null, "test1");
        RequestModel model2 = new RequestModel(null, "test2");
        RequestModel model3 = new RequestModel(null, "test3");

        input.onNext(model1);
        input.onNext(model2);
        input.onNext(model3);

        List<RequestModel> results = listener.buffer(100, TimeUnit.MILLISECONDS).toBlocking().first();

        Assertions.assertThat(results).containsExactly(model1, model2);
    }

    @Test
    public void shouldDelayNextElementsIfLimitIsExceeded() {
        ThrottleGuard guard = new ThrottleGuard();
        PublishSubject<RequestModel> input = PublishSubject.create();

        Observable<RequestModel> output = guard.throttle(input, 1000, 2);

        ReplaySubject<RequestModel> listener = ReplaySubject.create();
        output.subscribe(listener);

        RequestModel model1 = new RequestModel(null, "test1");
        RequestModel model2 = new RequestModel(null, "test2");
        RequestModel model3 = new RequestModel(null, "test3");
        RequestModel model4 = new RequestModel(null, "test4");
        RequestModel model5 = new RequestModel(null, "test5");

        input.onNext(model1);
        input.onNext(model2);
        input.onNext(model3);
        input.onNext(model4);
        input.onNext(model5);

        List<RequestModel> results = listener.buffer(1100, TimeUnit.MILLISECONDS).toBlocking().first();

        Assertions.assertThat(results).containsExactly(model1, model2, model3, model4);
    }
}
