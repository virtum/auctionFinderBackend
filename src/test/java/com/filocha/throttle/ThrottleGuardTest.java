package com.filocha.throttle;

import com.filocha.finder.RequestModel;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.reactivex.Flowable.just;
import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(results).containsExactly(model1, model2);
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

        assertThat(results).containsExactly(model1, model2, model3, model4);
    }

    @Test
    public void tempObserveOnJust() {
        just("Some String")
                .map(it -> { // main
                    System.out.println("Map2 thread: " + Thread.currentThread().getName());
                    return it;
                })
                .observeOn(Schedulers.computation())
                .subscribe(number -> System.out.println("Subscribe thread: " + Thread.currentThread().getName())); // computation
    }

    @Test
    public void tempSubscribeOnJust() throws InterruptedException {
        just("Some String")
                .map(it -> { // computation
                    System.out.println("Map2 thread: " + Thread.currentThread().getName());
                    return it;
                })
                .subscribeOn(Schedulers.computation())
                .subscribe(number -> System.out.println("Subscribe thread: " + Thread.currentThread().getName())); // computation

        Thread.sleep(300);
    }

    @Test
    public void shouldProcessOnMainThread() {
        final PublishSubject<String> source = PublishSubject.create();
        throttle(source) // main
                .map(it -> {
                    System.out.println("Map thread: " + Thread.currentThread().getName()); // main
                    return it;
                })
                .subscribe(it -> System.out.println("Subscribe thread: " + Thread.currentThread().getName())); // main

        source.onNext("1");
    }


    @Test
    public void shouldSwitchThreadForSubscribeOnly() {
        final PublishSubject<String> source = PublishSubject.create();
        throttle(source) // main
                .map(it -> {
                    System.out.println("Map thread: " + Thread.currentThread().getName()); // main
                    return it;
                })
                .observeOn(Schedulers.computation())
                .subscribe(it -> System.out.println("Subscribe thread: " + Thread.currentThread().getName())); // computation

        source.onNext("1");
    }

    @Test
    public void shouldSwitchThreads() throws InterruptedException {
        final PublishSubject<String> source = PublishSubject.create();
        throttle(source) // main
                .map(it -> {
                    System.out.println("Map thread: " + Thread.currentThread().getName());
                    return it;
                })
                .subscribeOn(Schedulers.computation())
                .subscribe(it -> System.out.println("Subscribe thread: " + Thread.currentThread().getName())); // ???

        source.onNext("1");

        Thread.sleep(3000);
    }

    private PublishSubject<String> throttle(final PublishSubject<String> source) {
        final PublishSubject<String> output = PublishSubject.create();

        source.subscribe(output);
        output.subscribe(it -> System.out.println("Throttle thread: " + Thread.currentThread().getName()));

        return output;
    }


}
