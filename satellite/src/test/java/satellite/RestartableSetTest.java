package satellite;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import info.android15.satellite.BuildConfig;
import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.functions.Func1;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import valuemap.ValueMap;

import static org.junit.Assert.assertEquals;
import static rx.Observable.interval;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
public class RestartableSetTest {

    public static final int RESTARTABLE_ID = 1;

    TestSubscriber<Notification<Long>> subscriber1 = new TestSubscriber<>();
    TestSubscriber<Notification<Long>> subscriber2 = new TestSubscriber<>();
    TestScheduler scheduler = new TestScheduler();

    private final int restartableI, methodI;
    private final ISubscribeRestartable subscribeRestartable;
    private final boolean noArg;
    private final DeliveryMethod method;
    private final int emit;

    public RestartableSetTest(int restartableI, int methodI) {
        this.restartableI = restartableI;
        this.methodI = methodI;

        this.subscribeRestartable =
            restartableI == 0 ? new SubscribeRestartable_NoArg_Infinitive() :
                restartableI == 1 ? new SubscribeRestartable_NoArg() :
                    restartableI == 2 ? new SubscribeRestartable_Infinitive() :
                        new SubscribeRestartable();
        this.noArg = restartableI < 2;
        this.method =
            methodI == 0 ? DeliveryMethod.SINGLE :
                methodI == 1 ? DeliveryMethod.LATEST :
                    methodI == 2 ? DeliveryMethod.REPLAY :
                        DeliveryMethod.PUBLISH;
        this.emit = this.method == DeliveryMethod.SINGLE ? 1 : subscribeRestartable.single() ? 1 : 5;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0} {1}")
    public static Collection<Object[]> parameters() {
        ArrayList<Object[]> variants = new ArrayList<>();
        for (int restartable = 0; restartable < 4; restartable++)
            for (int method = 0; method < 4; method++)
                variants.add(new Object[]{restartable, method});
        return variants;
    }

    @Test
    public void test_basic_usage() throws Exception {
        ValueMap.Builder builder = ValueMap.builder();
        RestartableSet set = restartableSet(method, builder, subscriber1, scheduler);

        launch(set);
        subscriber1.assertNoValues();
        assertEquals(1, ReconnectableMap.INSTANCE.keys().size());

        advanceEmission();
        verifyReceived(subscriber1);
        verifyNoLeakedObservables();
    }

    @Test
    public void test_dismiss() throws Exception {
        ValueMap.Builder builder = ValueMap.builder();
        RestartableSet set = restartableSet(method, builder, subscriber1, scheduler);
        launch(set);

        set.dismiss();

        advanceEmission();
        subscriber1.assertNoValues();
        verifyNoLeakedObservables();
    }

    @Test
    public void test_reconnect() throws Exception {
        ValueMap.Builder builder = ValueMap.builder();
        RestartableSet set1 = new RestartableSet(builder);

        Subscription subscription = subscribeRestartable(method, subscriber1, scheduler, set1);
        launch(set1);
        subscription.unsubscribe();

        RestartableSet set = new RestartableSet(builder.build(), builder);
        subscribeRestartable(method, subscriber2, scheduler, set);

        advanceEmission();
        verifyReceived(subscriber2);
        verifyNoLeakedObservables();
    }

    @Test
    public void test_emit_then_reconnect() throws Exception {
        ValueMap.Builder builder = ValueMap.builder();
        RestartableSet set1 = new RestartableSet(builder);

        Subscription subscription = subscribeRestartable(method, subscriber1, scheduler, set1);
        launch(set1);
        subscription.unsubscribe();

        advanceEmission();

        RestartableSet set = new RestartableSet(builder.build(), builder);
        subscribeRestartable(method, subscriber2, scheduler, set);

        if (method == DeliveryMethod.SINGLE) {
            if (subscribeRestartable.single())
                subscriber2.assertReceivedOnNext(valuesToNotifications(0L));
            else
                subscriber2.assertReceivedOnNext(valuesToNotifications(0L));
        }
        else if (method == DeliveryMethod.LATEST) {
            if (subscribeRestartable.single())
                subscriber2.assertReceivedOnNext(valuesToNotifications(0L));
            else
                subscriber2.assertReceivedOnNext(valuesToNotifications(4L));
        }
        else if (method == DeliveryMethod.REPLAY) {
            if (subscribeRestartable.single())
                subscriber2.assertReceivedOnNext(valuesToNotifications(0L));
            else
                subscriber2.assertReceivedOnNext(valuesToNotifications(0L, 1L, 2L, 3L, 4L));
        }
        else if (method == DeliveryMethod.PUBLISH) {
            subscriber2.assertNoValues();
        }
    }

    @Test
    public void test_restart() throws Exception {
        ValueMap.Builder builder = ValueMap.builder();
        RestartableSet set1 = new RestartableSet(builder);

        Subscription subscription = subscribeRestartable(method, subscriber1, scheduler, set1);
        launch(set1);
        subscription.unsubscribe();

        ValueMap state = builder.build();
        clearBackgroundObservables();

        RestartableSet set = new RestartableSet(state, state.toBuilder());
        subscribeRestartable(method, subscriber2, scheduler, set);

        advanceEmission();
        verifyReceived(subscriber2);
        verifyNoLeakedObservables();
    }

    private void launch(RestartableSet set) {
        if (noArg)
            set.launch(RESTARTABLE_ID);
        else
            set.launch(RESTARTABLE_ID, "0");
    }

    private void clearBackgroundObservables() {
        for (String key : ReconnectableMap.INSTANCE.keys())
            ReconnectableMap.INSTANCE.dismiss(key);
    }

    private void verifyReceived(TestSubscriber<Notification<Long>> subscriber2) {
        subscriber2.assertReceivedOnNext(emittedValue());
    }

    private void verifyNoLeakedObservables() {
        if (subscribeRestartable.single()) {
            assertEquals(0, ReconnectableMap.INSTANCE.keys().size());
        }
    }

    private ArrayList<Notification<Long>> emittedValue() {
        ArrayList<Notification<Long>> list = new ArrayList<>();
        for (long i = 0; i < emit; i++)
            list.add(Notification.createOnNext(i));
        return list;
    }

    private <T> ArrayList<Notification<T>> valuesToNotifications(T... values) {
        ArrayList<Notification<T>> list = new ArrayList<>();
        for (T t : values)
            list.add(Notification.createOnNext(t));
        return list;
    }

    private void advanceEmission() {
        scheduler.advanceTimeBy(emit * 1000, TimeUnit.MILLISECONDS);
    }

    private RestartableSet restartableSet(DeliveryMethod method, ValueMap.Builder builder, TestSubscriber<Notification<Long>> testSubscriber, final TestScheduler scheduler) {
        RestartableSet set = new RestartableSet(builder);
        subscribeRestartable(method, testSubscriber, scheduler, set);
        return set;
    }

    protected Subscription subscribeRestartable(DeliveryMethod method, TestSubscriber<Notification<Long>> testSubscriber, final TestScheduler scheduler, RestartableSet set) {
        return subscribeRestartable.invoke(method, testSubscriber, scheduler, set);
    }

    @After
    public void tearDown() throws Exception {
        clearBackgroundObservables();
    }

    private interface ISubscribeRestartable {
        Subscription invoke(DeliveryMethod method, TestSubscriber<Notification<Long>> testSubscriber, TestScheduler scheduler, RestartableSet set);
        boolean single();
    }

    private static class SubscribeRestartable_NoArg_Infinitive implements ISubscribeRestartable {
        @Override
        public Subscription invoke(DeliveryMethod method, TestSubscriber<Notification<Long>> testSubscriber, final TestScheduler scheduler, RestartableSet set) {
            return set
                .restartable(RESTARTABLE_ID, method, new RestartableFactoryNoArg<Long>() {
                    @Override
                    public Observable<Long> call() {
                        return interval(1, 1, TimeUnit.SECONDS, scheduler);
                    }
                })
                .subscribe(testSubscriber);
        }

        @Override
        public boolean single() {
            return false;
        }
    }

    private static class SubscribeRestartable_NoArg implements ISubscribeRestartable {
        @Override
        public Subscription invoke(DeliveryMethod method, TestSubscriber<Notification<Long>> testSubscriber, final TestScheduler scheduler, RestartableSet set) {
            return set
                .restartable(RESTARTABLE_ID, method, new RestartableFactoryNoArg<Long>() {
                    @Override
                    public Observable<Long> call() {
                        return Observable.just(0L).delay(1, TimeUnit.SECONDS, scheduler);
                    }
                })
                .subscribe(testSubscriber);
        }

        @Override
        public boolean single() {
            return true;
        }
    }

    private static class SubscribeRestartable_Infinitive implements ISubscribeRestartable {
        @Override
        public Subscription invoke(DeliveryMethod method, TestSubscriber<Notification<Long>> testSubscriber, final TestScheduler scheduler, RestartableSet set) {
            return set
                .restartable(RESTARTABLE_ID, method, new RestartableFactory<String, Long>() {
                    @Override
                    public Observable<Long> call(final String a) {
                        return interval(1, 1, TimeUnit.SECONDS, scheduler).map(new Func1<Long, Long>() {
                            @Override
                            public Long call(Long aLong) {
                                return aLong + Long.parseLong(a);
                            }
                        });
                    }
                })
                .subscribe(testSubscriber);
        }

        @Override
        public boolean single() {
            return false;
        }
    }

    private static class SubscribeRestartable implements ISubscribeRestartable {
        @Override
        public Subscription invoke(DeliveryMethod method, TestSubscriber<Notification<Long>> testSubscriber, final TestScheduler scheduler, RestartableSet set) {
            return set
                .restartable(RESTARTABLE_ID, method, new RestartableFactory<String, Long>() {
                    @Override
                    public Observable<Long> call(String a) {
                        return Observable.just(Long.parseLong(a)).delay(1, TimeUnit.SECONDS, scheduler);
                    }
                })
                .subscribe(testSubscriber);
        }

        @Override
        public boolean single() {
            return true;
        }
    }
}
