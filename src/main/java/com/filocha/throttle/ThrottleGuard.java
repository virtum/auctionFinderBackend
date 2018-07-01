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

    /**
     * Limits specific number of incoming messages by given delay. After exceeding the limitation, messages are waiting
     * to be published.
     *
     * @param incomingMessages incoming message
     * @param delay            time added to the next message before sending
     * @param maxOfMessages    maximum number of messages that can be handled without delay
     * @return Observable stream of emitted requests
     */
    public static Observable<RequestModel> throttle(final Observable<RequestModel> incomingMessages, final long delay,
                                                    final int maxOfMessages) {
        final PublishSubject<Optional> tripod = PublishSubject.create();
        final PublishSubject<RequestModel> mergedSubject = PublishSubject.create();
        final List<RequestWithTimestamp> requests = new ArrayList<>();
        final PublishSubject<RequestModel> output = PublishSubject.create();

        mergedSubject.subscribe(item -> {
            if (requests.size() < maxOfMessages) {
                final RequestWithTimestamp request = new RequestWithTimestamp(item, new Date());

                requests.add(request);
                output.onNext(item);

                // Optional.empty() is used only to emit some value, rx2 forbid emitting null values
                tripod.onNext(Optional.empty());
            } else {
                removeOldestItemFromList(item, delay, requests, mergedSubject);
            }
        }, output::onError, output::onComplete);

        Observable
                .zip(incomingMessages, tripod, (sub1, sub2) -> sub1)
                .subscribe(mergedSubject);

        // Optional.empty() is used only to emit some value, rx2 forbid emitting null values
        tripod.onNext(Optional.empty());

        return output;
    }

    private static void removeOldestItemFromList(final RequestModel mock, final long delay, final List<RequestWithTimestamp> requests,
                                                 final PublishSubject<RequestModel> mergedSubject) {
        final RequestWithTimestamp temp = requests.get(0);
        requests.remove(0);

        long delayTime = (temp.getCreationDate().getTime() + delay) - new Date().getTime();

        delayTime = delayTime < 0 ? 0 : delayTime;

        Observable
                .timer(delayTime, TimeUnit.MILLISECONDS)
                .subscribe(it -> mergedSubject.onNext(mock));
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

