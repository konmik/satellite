package satellite.example;

import android.app.Activity;
import android.os.Bundle;

import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import satellite.RestartableConnectionGroup;
import satellite.Launcher;
import satellite.RestartableConnection;
import satellite.RestartableFactory;
import satellite.util.SubjectFactory;

/**
 * This is an example activity that eliminates code duplications when dealing with
 * {@link RestartableConnection}.
 */
public class BaseActivity extends Activity implements Launcher {

    private RestartableConnectionGroup restartableConnectionGroup;
    private Subscription subscription;
    private boolean connect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restartableConnectionGroup = savedInstanceState == null ? new RestartableConnectionGroup() : new RestartableConnectionGroup(savedInstanceState.getParcelable("restartableConnectionGroup"));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("restartableConnectionGroup", restartableConnectionGroup.instanceState());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (connect) {
            subscription = onConnect();
            connect = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
        if (isFinishing())
            restartableConnectionGroup.dismissAll();
    }

    /**
     * This method is being called during the first {@link #onResume()} call.
     *
     * You can combine multiple subscriptions with {@link Subscriptions#from(Subscription...)}.
     */
    protected Subscription onConnect() {
        return Subscriptions.empty();
    }

    public <A, T> Observable<Notification<T>> connection(int id, RestartableFactory<A, T> restartableFactory) {
        return restartableConnectionGroup.connection(id, SubjectFactory.behaviorSubject(), restartableFactory);
    }

    @Override
    public <A, T> Observable<Notification<T>> connection(
        int id, SubjectFactory<Notification<T>> subjectFactory, RestartableFactory<A, T> restartableFactory) {

        return restartableConnectionGroup.connection(id, subjectFactory, restartableFactory);
    }

    @Override
    public <A> void launch(int id, A missionStatement) {
        restartableConnectionGroup.launch(id, missionStatement);
    }

    @Override
    public void dismiss(int id) {
        restartableConnectionGroup.dismiss(id);
    }
}
