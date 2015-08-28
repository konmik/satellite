package satellite.connections;

import android.util.Printer;

import java.util.HashMap;

import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func0;
import satellite.MissionControlCenter;

/**
 * SpaceStation represents... a space station! :D
 *
 * It connects started satellites with a land base {@link MissionControlCenter}.
 */
public enum SpaceStation {

    INSTANCE;

    private HashMap<String, Observable> subjects = new HashMap<>();
    private HashMap<String, Subscription> subscriptions = new HashMap<>();

    <T> Observable<Notification<T>> provideSubject(String key, Func0<Observable<Notification<T>>> factory) {
        if (!subjects.containsKey(key))
            subjects.put(key, factory.call());
        return subjects.get(key);
    }

    void takeSubscription(String key, Subscription subscription) {
        dropSubscription(key);
        subscriptions.put(key, subscription);
    }

    void dropSubject(String key) {
        subjects.remove(key);
    }

    void dropSubscription(String key) {
        if (subscriptions.containsKey(key)) {
            Subscription subscription = subscriptions.get(key);
            subscription.unsubscribe();
            subscriptions.remove(key);
        }
    }

    public void print(Printer printer) {
        printer.println("subjects:");
        for (String key : subjects.keySet())
            printer.println(key);
        printer.println("subscriptions:");
        for (String key : subscriptions.keySet())
            printer.println(key);
    }
}
