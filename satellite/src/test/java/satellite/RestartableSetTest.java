package satellite;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import info.android15.satellite.BuildConfig;
import rx.Notification;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import statemap.StateMap;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static rx.Observable.interval;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RestartableSetTest {

    public static final int RESTARTABLE_ID = 1;

    TestSubscriber<Notification<Long>> subscriber = new TestSubscriber<>();
    TestScheduler scheduler = new TestScheduler();

    @Test
    public void test_basic_usage() throws Exception {
        StateMap.Builder builder = StateMap.builder();
        RestartableSet set = launchRestartable(DeliveryMethod.SINGLE, builder, subscriber, scheduler);

        set.launch(1);
        subscriber.assertNoValues();
        assertEquals(1, ReconnectableMap.INSTANCE.keys().size());

        scheduler.advanceTimeBy(1500, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(singletonList(Notification.createOnNext(0L)));

        scheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS);
        subscriber.assertReceivedOnNext(singletonList(Notification.createOnNext(0L)));

        assertEquals(0, ReconnectableMap.INSTANCE.keys().size());
    }

    private static RestartableSet launchRestartable(DeliveryMethod method, StateMap.Builder builder, TestSubscriber<Notification<Long>> testSubscriber, final TestScheduler scheduler) {
        RestartableSet set = new RestartableSet(builder);
        set
            .restartable(RESTARTABLE_ID, method, new RestartableFactoryNoArg<Long>() {
                @Override
                public Observable<Long> call() {
                    return interval(1, 1, TimeUnit.SECONDS, scheduler);
                }
            })
            .subscribe(testSubscriber);
        return set;
    }

    @After
    public void tearDown() throws Exception {
        synchronized (ReconnectableMap.INSTANCE) {
            for (String key : new HashSet<>(ReconnectableMap.INSTANCE.keys()))
                ReconnectableMap.INSTANCE.dismiss(key);
        }
    }
}
