package satellite.example;

import android.app.Activity;
import android.os.Bundle;

import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import satellite.EarthBase;
import satellite.Launcher;
import satellite.SatelliteFactory;
import satellite.util.SubjectFactory;

/**
 * This is an example activity that eliminates code duplications when dealing with
 * {@link satellite.MissionControlCenter}.
 */
public class BaseActivity extends Activity implements Launcher {

    private EarthBase earthBase;
    private Subscription subscription;
    private boolean connect = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        earthBase = savedInstanceState == null ? new EarthBase() : new EarthBase(savedInstanceState.getParcelable("earthBase"));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("earthBase", earthBase.instanceState());
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
            earthBase.dismissAll();
    }

    /**
     * This method is being called during the first {@link #onResume()} call.
     *
     * You can combine multiple subscriptions with {@link Subscriptions#from(Subscription...)}.
     */
    protected Subscription onConnect() {
        return Subscriptions.empty();
    }

    public <A, T> Observable<Notification<T>> connection(int id, SatelliteFactory<A, T> satelliteFactory) {
        return earthBase.connection(id, SubjectFactory.behaviorSubject(), satelliteFactory);
    }

    @Override
    public <A, T> Observable<Notification<T>> connection(
        int id, SubjectFactory<Notification<T>> subjectFactory, SatelliteFactory<A, T> satelliteFactory) {

        return earthBase.connection(id, subjectFactory, satelliteFactory);
    }

    @Override
    public <A> void launch(int id, A missionStatement) {
        earthBase.launch(id, missionStatement);
    }

    @Override
    public void dismiss(int id) {
        earthBase.dismiss(id);
    }
}
