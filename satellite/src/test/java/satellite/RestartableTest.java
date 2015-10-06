package satellite;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;

import info.android15.satellite.BuildConfig;
import rx.Notification;
import rx.Subscription;
import rx.observers.TestObserver;
import satellite.state.StateMap;

import static java.util.Arrays.asList;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RestartableTest extends BaseRestartableTest {
    @After
    public void tearDown() throws Exception {
        resetStation();
    }

    @Test
    public void testRestartableImmediate() throws Exception {
        testConnectionImmediateStrategy(new RestartableLauncher());
    }

    @Test
    public void testConnectionDelayed() throws Exception {
        testConnectionDelayedStrategy(new RestartableLauncher());
    }

    @Test
    public void testDismiss() throws Exception {
        testDismissStrategy(new RestartableLauncher());
    }

    @Test
    public void testSaveRestoreRestart() throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();

        StateMap.Builder out = new StateMap.Builder();
        Restartable restartable = new Restartable(out);
        Subscription subscription = restartable.channel(ChannelType.LATEST, INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver);
        restartable.launch("1");

        testObserver.assertReceivedOnNext(asList(Notification.createOnNext(1)));

        resetStation();

        TestObserver<Notification<Integer>> testObserver2 = new TestObserver<>();
        Restartable connection2 = new Restartable(out.build(), out);
        Subscription subscription2 = connection2.channel(ChannelType.LATEST, INFINITE_RESTARTABLE_FACTORY).subscribe(testObserver2);

        testObserver2.assertReceivedOnNext(asList(Notification.createOnNext(1)));
    }

    @Test
    public void testSaveRestoreCompleted() throws Exception {
        TestObserver<Notification<Integer>> testObserver = new TestObserver<>();

        StateMap.Builder out = new StateMap.Builder();
        Restartable restartable = new Restartable(out);
        Subscription subscription = restartable.channel(ChannelType.LATEST, RESTARTABLE_FACTORY).subscribe(testObserver);
        restartable.launch("1");

        testObserver.assertReceivedOnNext(asList(Notification.createOnNext(1), Notification.<Integer>createOnCompleted()));

        resetStation();

        TestObserver<Notification<Integer>> testObserver2 = new TestObserver<>();
        Restartable connection2 = new Restartable(out.build(), out);
        Subscription subscription2 = connection2.channel(ChannelType.LATEST, RESTARTABLE_FACTORY).subscribe(testObserver2);

        testObserver2.assertReceivedOnNext(Collections.<Notification<Integer>>emptyList());
    }
}