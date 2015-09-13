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
import static org.junit.Assert.assertNotNull;

public class ReconnectableMapTest {
    @After
    public void tearDown() throws Exception {
        for (String key : new HashSet<>(ReconnectableMap.INSTANCE.keys()))
            ReconnectableMap.INSTANCE.dismiss(key);
    }

    @Test
    public void testConnection() throws Exception {
        final BehaviorSubject subject = BehaviorSubject.create();
        final PublishSubject satellite = PublishSubject.create();

        SubjectFactory<Notification<Integer>> subjectFactory = new SubjectFactory<Notification<Integer>>() {
            @Override
            public Subject<Notification<Integer>, Notification<Integer>> call() {
                return subject;
            }
        };
        Func0<Observable<Integer>> satelliteFactory = new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                return satellite;
            }
        };
        Observable connection = ReconnectableMap.INSTANCE.connection("1", subjectFactory, satelliteFactory);

        assertNotNull(connection);
        assertEquals(0, ReconnectableMap.INSTANCE.keys().size());

        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();
        connection.subscribe(testObserver);

        assertEquals(1, ReconnectableMap.INSTANCE.keys().size());

        // second connection with the same key does not create a new connection
        ReconnectableMap.INSTANCE.connection("1", subjectFactory, satelliteFactory).subscribe();
        assertEquals(1, ReconnectableMap.INSTANCE.keys().size());

        // second connection with a different key creates a new connection
        ReconnectableMap.INSTANCE.connection("2", new SubjectFactory<Notification<Integer>>() {
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
        satellite.onNext(1);
        testObserver.assertReceivedOnNext(Collections.singletonList(Notification.createOnNext(1)));

        // secondary subscription works
        TestObserver<Notification<Integer>> testObserver2 = new TestObserver<>();
        ReconnectableMap.INSTANCE.connection("1", subjectFactory, satelliteFactory).subscribe(testObserver2);
        testObserver2.assertReceivedOnNext(Collections.singletonList(Notification.createOnNext(1)));
    }
}
