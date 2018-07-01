package com.filocha.throttle;

import com.filocha.finder.RequestModel;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import lombok.Builder;
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
        final List<RequestWithTimestamp> messages = new ArrayList<>();
        final PublishSubject<RequestModel> output = PublishSubject.create();

        mergedSubject.subscribe(message -> {
            if (messages.size() < maxOfMessages) {
                messages.add(RequestWithTimestamp
                        .builder()
                        .message(message)
                        .creationDate(new Date())
                        .build());

                output.onNext(message);

                // Tick, Optional.empty() is used only to emit some value, rx2 forbid emitting null values
                tripod.onNext(Optional.empty());
            } else {
                removeOldestItemFromList(message, delay, messages, mergedSubject);
            }
        }, output::onError, output::onComplete);

        Observable
                .zip(incomingMessages, tripod, (sub1, sub2) -> sub1)
                .subscribe(mergedSubject);

        // Initial tick, Optional.empty() is used only to emit some value, rx2 forbid emitting null values
        tripod.onNext(Optional.empty());

        return output;
    }

    private static void removeOldestItemFromList(final RequestModel newRequest, final long delay, final List<RequestWithTimestamp> messages,
                                                 final PublishSubject<RequestModel> mergedSubject) {
        final RequestWithTimestamp oldestRequest = messages.get(0);
        messages.remove(0);

        long delayTime = (oldestRequest.getCreationDate().getTime() + delay) - new Date().getTime();

        delayTime = delayTime < 0 ? 0 : delayTime;

        Observable
                .timer(delayTime, TimeUnit.MILLISECONDS)
                .subscribe(it -> mergedSubject.onNext(newRequest));
    }
}

@Value
@Builder
class RequestWithTimestamp {
    private RequestModel message;
    private Date creationDate;
}

