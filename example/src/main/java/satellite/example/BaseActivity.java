package satellite.example;

import android.app.Activity;
import android.os.Bundle;

import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import satellite.DeliveryMethod;
import satellite.Launcher;
import satellite.Restartable;
import satellite.RestartableFactory;
import satellite.RestartableFactoryNoArg;
import satellite.RestartableSet;
import satellite.state.StateMap;

/**
 * This is an example activity that eliminates code duplication when dealing with
 * {@link Restartable} and {@link RestartableSet}.
 */
public class BaseActivity extends Activity implements Launcher {

    private RestartableSet restartables;
    private Subscription subscription;
    private boolean connect = true;
    private StateMap.Builder out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
            this.restartables = new RestartableSet(out = new StateMap.Builder());
        else {
            StateMap map = savedInstanceState.getParcelable("restartables");
            this.restartables = new RestartableSet(map, out = map.toBuilder());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("restartables", out.build());
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
            restartables.dismiss();
    }

    /**
     * This method is being called during the first {@link #onResume()} call.
     * The returned {@link Subscription} will be unsubscribed during {@link #onDestroy()}.
     *
     * You can combine multiple subscriptions with {@link Subscriptions#from(Subscription...)}.
     */
    protected Subscription onConnect() {
        return Subscriptions.empty();
    }

    public <T> Observable<Notification<T>> restartable(int id, RestartableFactoryNoArg<T> restartableFactory) {
        return restartables.restartable(id, DeliveryMethod.LATEST, restartableFactory);
    }

    public <A, T> Observable<Notification<T>> restartable(int id, RestartableFactory<A, T> restartableFactory) {
        return restartables.restartable(id, DeliveryMethod.LATEST, restartableFactory);
    }

    @Override
    public <T> Observable<Notification<T>> restartable(int id, DeliveryMethod type, RestartableFactoryNoArg<T> restartableFactory) {
        return restartables.restartable(id, type, restartableFactory);
    }

    @Override
    public <A, T> Observable<Notification<T>> restartable(int id, DeliveryMethod type, RestartableFactory<A, T> restartableFactory) {
        return restartables.restartable(id, type, restartableFactory);
    }

    @Override
    public void launch(int id, Object arg) {
        restartables.launch(id, arg);
    }

    @Override
    public void launch(int id) {
        restartables.launch(id);
    }

    @Override
    public void dismiss(int id) {
        restartables.dismiss(id);
    }
}
