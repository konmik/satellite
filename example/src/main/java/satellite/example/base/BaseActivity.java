package satellite.example.base;

import android.app.Activity;
import android.os.Bundle;

import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import satellite.DeliveryMethod;
import satellite.Launcher;
import satellite.ObservableFactory;
import satellite.ObservableFactoryNoArg;
import satellite.Restartable;
import satellite.RestartableSet;
import valuemap.ValueMap;

/**
 * This is an example activity that eliminates code duplication when dealing with
 * {@link Restartable} and {@link RestartableSet}.
 */
public class BaseActivity extends Activity implements Launcher {

    private RestartableSet restartables;
    private Subscription subscription;
    private boolean connect = true;
    private ValueMap.Builder out;

    /**
     * This method is being called during the first {@link #onResume()} call.
     * The returned {@link Subscription} will be unsubscribed during {@link #onDestroy()}.
     *
     * You can combine multiple subscriptions with {@link Subscriptions#from(Subscription...)} method.
     */
    protected Subscription onConnect() {
        return Subscriptions.empty();
    }

    @Override
    public <T> Observable<Notification<T>> channel(int id, DeliveryMethod type, ObservableFactoryNoArg<T> observableFactoryNoArg) {
        return restartables.channel(id, type, observableFactoryNoArg);
    }

    @Override
    public <A, T> Observable<Notification<T>> channel(int id, DeliveryMethod type, ObservableFactory<A, T> observableFactory) {
        return restartables.channel(id, type, observableFactory);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
            restartables = new RestartableSet(out = new ValueMap.Builder());
        else {
            ValueMap map = savedInstanceState.getParcelable("restartables");
            restartables = new RestartableSet(map, out = map.toBuilder());
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
}
