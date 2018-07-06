package com.filocha.throttle;

import com.filocha.finder.RequestModel;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
        final PublishSubject<Tick> tripod = PublishSubject.create();
        final PublishSubject<RequestModel> mergedSubject = PublishSubject.create();
        final List<MessageWithTimestamp> messages = new ArrayList<>();
        final PublishSubject<RequestModel> output = PublishSubject.create();

        mergedSubject.subscribe(message -> {
            if (messages.size() < maxOfMessages) {
                processMessage(messages, message, output, tripod);
            } else {
                removeOldestItemFromList(messages, delay, mergedSubject, message);
            }
        }, output::onError, output::onComplete);

        Observable
                .zip(incomingMessages, tripod, (sub1, sub2) -> sub1)
                .subscribe(mergedSubject);

        // Initial tick
        tripod.onNext(Tick.TICK);

        return output;
    }

    /**
     * Handles incoming message by adding it to list with date, then emits this message to the output and notify tripod
     *
     * @param messages list of all sent messages with date
     * @param message  new message to send
     * @param output   observable stream for sending new messages
     * @param tripod   gate, waiting to be notified when new message was send
     */
    private static void processMessage(final List<MessageWithTimestamp> messages, final RequestModel message,
                                       final PublishSubject<RequestModel> output, final PublishSubject<Tick> tripod) {
        messages.add(MessageWithTimestamp
                .builder()
                .message(message)
                .creationDate(new Date())
                .build());

        output.onNext(message);

        tripod.onNext(Tick.TICK);
    }

    /**
     * Removes oldest message from list of sent messages, then calculates delay and waits until emit new message to be
     * send.
     *
     * @param messages      list of all sent messages with date
     * @param delay         delay for new message after which it will be send
     * @param mergedSubject observable stream gathering messages
     * @param message       new message waiting to be send
     */
    private static void removeOldestItemFromList(final List<MessageWithTimestamp> messages, final long delay,
                                                 final PublishSubject<RequestModel> mergedSubject, final RequestModel message) {
        final MessageWithTimestamp oldestMessage = messages.get(0);
        messages.remove(0);

        long delayTime = (oldestMessage.getCreationDate().getTime() + delay) - new Date().getTime();

        delayTime = delayTime < 0 ? 0 : delayTime;

        Observable
                .timer(delayTime, TimeUnit.MILLISECONDS)
                .subscribe(it -> mergedSubject.onNext(message));
    }
}

@Value
@Builder
class MessageWithTimestamp {
    private RequestModel message;
    private Date creationDate;
}


enum Tick {TICK}

