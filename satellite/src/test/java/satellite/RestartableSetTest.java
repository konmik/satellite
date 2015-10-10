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

    private final ISubscribeRestartable subscribeRestartable;
    private final boolean noArg;
    private final DeliveryMethod method;

    public RestartableSetTest(int i) {
        this.subscribeRestartable =
            i % 10 == 0 ? new SubscribeRestartable_NoArg_Infinitive() :
                i % 10 == 1 ? new SubscribeRestartable_NoArg() :
                    i % 10 == 2 ? new SubscribeRestartable_Infinitive() :
                        new SubscribeRestartable();
        this.noArg = i % 10 <= 1;
        this.method =
            i / 10 == 0 ? DeliveryMethod.SINGLE :
                i / 10 == 1 ? DeliveryMethod.LATEST :
                    i / 10 == 2 ? DeliveryMethod.REPLAY :
                        DeliveryMethod.PUBLISH;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "SubscribeRestartable = {0}")
    public static Collection<Object[]> data() {
        ArrayList<Object[]> variants = new ArrayList<>();
        for (int restartable = 0; restartable < 4; restartable++)
            for (int method = 0; method < 4; method++)
                variants.add(new Object[]{restartable + method * 10});
        return variants;
    }

    @Test
    public void test_basic_usage() throws Exception {
        ValueMap.Builder builder = ValueMap.builder();
        RestartableSet set = restartableSet(method, builder, subscriber1, scheduler);

        launch(set);
        subscriber1.assertNoValues();
        assertEquals(1, ReconnectableMap.INSTANCE.keys().size());

        advanceOneEmission();
        verifyReceived(subscriber1, 0L);
        verifyNoLeakedObservables(1);
    }

    @Test
    public void test_dismiss() throws Exception {
        ValueMap.Builder builder = ValueMap.builder();
        RestartableSet set = restartableSet(method, builder, subscriber1, scheduler);
        launch(set);

        set.dismiss();

        advanceOneEmission();
        subscriber1.assertNoValues();
        verifyNoLeakedObservables(0);
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

        advanceOneEmission();
        verifyReceived(subscriber2, 0L);
        verifyNoLeakedObservables(1);
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

        advanceOneEmission();
        verifyReceived(subscriber2, 0L);
        verifyNoLeakedObservables(1);
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

    private void verifyReceived(TestSubscriber<Notification<Long>> subscriber2, Long... values) {
        subscriber2.assertReceivedOnNext(valuesToNotifications(values));
    }

    private void verifyNoLeakedObservables(int receivedAmount) {
        if (subscribeRestartable.capacity() <= receivedAmount) {
            assertEquals(0, ReconnectableMap.INSTANCE.keys().size());
        }
    }

    private <T> ArrayList<Notification<T>> valuesToNotifications(T... values) {
        ArrayList<Notification<T>> list = new ArrayList<>();
        for (T v : values)
            list.add(Notification.createOnNext(v));
        return list;
    }

    private void advanceOneEmission() {
        scheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS);
    }

    private RestartableSet restartableSet(DeliveryMethod method, ValueMap.Builder builder, TestSubscriber<Notification<Long>> testSubscriber, final TestScheduler scheduler) {
        RestartableSet set = new RestartableSet(builder);
        subscribeRestartable(method, testSubscriber, scheduler, set);
        return set;
    }

    private RestartableSet restartableSet(DeliveryMethod method, ValueMap state, ValueMap.Builder builder, TestSubscriber<Notification<Long>> testSubscriber, final TestScheduler scheduler) {
        RestartableSet set = new RestartableSet(state, builder);
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
        int capacity();
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
        public int capacity() {
            return Integer.MAX_VALUE;
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
        public int capacity() {
            return 1;
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
        public int capacity() {
            return Integer.MAX_VALUE;
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
        public int capacity() {
            return 1;
        }
    }
}
