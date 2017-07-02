package com.filocha.throttle;

import rx.Observable;
import rx.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ThrottleGuard {

    public static Observable<Integer> throttle(Observable<Integer> input, long delay, int maxOfItems) {
        PublishSubject<Void> tripod = PublishSubject.create();
        PublishSubject<TempMock> mergedSubject = PublishSubject.create();
        List<TempRequest> requests = new ArrayList<>();
        PublishSubject<Integer> output = PublishSubject.create();

        mergedSubject.subscribe(item -> {
            if (item.isFlag()) {
                if (requests.size() < maxOfItems) {
                    TempRequest request = new TempRequest(item.getValue(), new Date());

                    requests.add(request);
                    output.onNext(item.getValue());

                    tripod.onNext(null);
                } else {
                    TempMock mock = new TempMock(item.getValue(), false);

                    mergedSubject.onNext(mock);
                }
            } else {
                removeOldestItemFromList(item, delay, requests, mergedSubject);
            }
        }, output::onError, output::onCompleted);

        Observable<Integer> zip = Observable.zip(input, tripod, (sub1, sub2) -> sub1);

        zip.map(item -> new TempMock(item, true)).subscribe(mergedSubject);

        tripod.onNext(null);

        return output;
    }

    private static void removeOldestItemFromList(TempMock mock, long delay, List<TempRequest> requests, PublishSubject<TempMock> mergedSubject) {
        TempRequest temp = requests.get(0);
        requests.remove(0);

        long delayTime = (temp.getCreationDate().getTime() + delay) - new Date().getTime();

        if (delayTime < 0) {
            delayTime = 0;
        }

        Observable
                .timer(delayTime, TimeUnit.MILLISECONDS)
                .subscribe(it -> {
                    TempMock mock1 = new TempMock(mock.getValue(), true);

                    mergedSubject.onNext(mock1);
                });
    }
}

class TempMock {
    private int value;
    private boolean flag;

    public TempMock(int value, boolean flag) {
        this.value = value;
        this.flag = flag;
    }

    public int getValue() {
        return value;
    }

    public boolean isFlag() {
        return flag;
    }
}

class TempRequest {
    private int request;
    private Date creationDate;

    public TempRequest(int request, Date creationDate) {
        this.request = request;
        this.creationDate = creationDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }
}

