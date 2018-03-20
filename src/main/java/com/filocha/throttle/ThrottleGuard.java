package com.filocha.throttle;

import com.filocha.finder.RequestModel;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import lombok.Value;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ThrottleGuard {

    public static Observable<RequestModel> throttle(final Observable<RequestModel> input, final long delay, final int maxOfItems) {
        final PublishSubject<Optional> tripod = PublishSubject.create();
        final PublishSubject<RequestWithFlag> mergedSubject = PublishSubject.create();
        final List<RequestWithTimestamp> requests = new ArrayList<>();
        final PublishSubject<RequestModel> output = PublishSubject.create();

        mergedSubject.subscribe(item -> {
            if (item.isFlag()) {
                if (requests.size() < maxOfItems) {
                    final RequestWithTimestamp request = new RequestWithTimestamp(item.getRequest(), new Date());

                    requests.add(request);
                    output.onNext(item.getRequest());

                    // Optional.empty() is used only to emit some value, rx2 forbid emitting null values
                    tripod.onNext(Optional.empty());
                } else {
                    final RequestWithFlag mock = new RequestWithFlag(item.getRequest(), false);

                    mergedSubject.onNext(mock);
                }
            } else {
                removeOldestItemFromList(item, delay, requests, mergedSubject);
            }
        }, output::onError, output::onComplete);

        final Observable<RequestModel> zip = Observable.zip(input, tripod, (sub1, sub2) -> sub1);

        zip.map(item -> new RequestWithFlag(item, true)).subscribe(mergedSubject);

        // Optional.empty() is used only to emit some value, rx2 forbid emitting null values
        tripod.onNext(Optional.empty());

        return output;
    }

    private static void removeOldestItemFromList(final RequestWithFlag mock, final long delay, final List<RequestWithTimestamp> requests,
                                                 final PublishSubject<RequestWithFlag> mergedSubject) {
        final RequestWithTimestamp temp = requests.get(0);
        requests.remove(0);

        long delayTime = (temp.getCreationDate().getTime() + delay) - new Date().getTime();

        if (delayTime < 0) {
            delayTime = 0;
        }

        Observable
                .timer(delayTime, TimeUnit.MILLISECONDS)
                .subscribe(it -> {
                    final RequestWithFlag mock1 = new RequestWithFlag(mock.getRequest(), true);

                    mergedSubject.onNext(mock1);
                });
    }
}

@Value
class RequestWithFlag {
    private RequestModel request;
    private boolean flag;

    RequestWithFlag(RequestModel value, boolean flag) {
        this.request = value;
        this.flag = flag;
    }
}

@Value
class RequestWithTimestamp {
    private RequestModel request;
    private Date creationDate;

    RequestWithTimestamp(RequestModel request, Date creationDate) {
        this.request = request;
        this.creationDate = creationDate;
    }
}

