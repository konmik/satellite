package satellite;

import org.junit.After;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import rx.Notification;
import rx.Observable;
import rx.functions.Func0;
import rx.observers.TestObserver;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;
import satellite.util.SubjectFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ReconnectableMapTest {
    @After
    public void tearDown() throws Exception {
        for (String key : new HashSet<>(ReconnectableMap.INSTANCE.keys()))
            ReconnectableMap.INSTANCE.dismiss(key);
    }

    @Test
    public void testChannel() throws Exception {
        final BehaviorSubject<Notification<Integer>> subject = BehaviorSubject.create();
        final PublishSubject<Integer> observable = PublishSubject.create();

        SubjectFactory<Notification<Integer>> subjectFactory = new SubjectFactory<Notification<Integer>>() {
            @Override
            public Subject<Notification<Integer>, Notification<Integer>> call() {
                return subject;
            }
        };
        Func0<Observable<Integer>> observableFactory = new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                return observable;
            }
        };
        Observable<Notification<Integer>> channel = ReconnectableMap.INSTANCE.channel("1", subjectFactory, observableFactory);

        assertNotNull(channel);
        assertEquals(0, ReconnectableMap.INSTANCE.keys().size());

        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        channel.subscribe(testObserver);

        assertEquals(1, ReconnectableMap.INSTANCE.keys().size());

        // second connection with the same key does not create a new connection
        ReconnectableMap.INSTANCE.channel("1", subjectFactory, observableFactory).subscribe();
        assertEquals(1, ReconnectableMap.INSTANCE.keys().size());

        // second connection with a different key creates a new connection
        ReconnectableMap.INSTANCE.channel("2", new SubjectFactory<Notification<Integer>>() {
            @Override
            public Subject<Notification<Integer>, Notification<Integer>> call() {
                return PublishSubject.create();
            }
        }, new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                return Observable.just(1);
            }
        }).subscribe();
        assertEquals(2, ReconnectableMap.INSTANCE.keys().size());

        // onNext values are passing up
        observable.onNext(1);
        testObserver.assertReceivedOnNext(Collections.singletonList(Notification.createOnNext(1)));

        // secondary subscription to the same connection works
        TestObserver<Notification<Integer>> testObserver2 = new TestObserver<>();
        ReconnectableMap.INSTANCE.channel("1", subjectFactory, observableFactory).subscribe(testObserver2);
        testObserver2.assertReceivedOnNext(Collections.singletonList(Notification.createOnNext(1)));

        // dismiss works
        ReconnectableMap.INSTANCE.dismiss("1");
        assertFalse(ReconnectableMap.INSTANCE.keys().contains("1"));
    }
}
