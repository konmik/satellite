package satellite.example.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import satellite.DeliveryMethod;
import satellite.Launcher;
import satellite.ObservableFactory;
import satellite.ObservableFactoryNoArg;
import satellite.RestartableSet;
import valuemap.ValueMap;

public class BaseFragment extends Fragment implements Launcher {

    private RestartableSet restartables;
    private Subscription subscription;
    private boolean connect = true;
    private ValueMap.Builder out;

    /**
     * This method is being called during the first {@link #onResume()} call.
     * The returned {@link Subscription} will be unsubscribed during {@link #onDestroyView()}.
     *
     * You can combine multiple subscriptions with {@link Subscriptions#from(Subscription...)} method.
     */
    protected Subscription onConnect() {
        return Subscriptions.empty();
    }

    /**
     * Call this method when you're not going to reattach the {@link BaseFragment} anymore,
     * to dismiss all of its restartables.
     */
    public void dismissRestartables() {
        if (restartables != null)
            restartables.dismiss();
    }

    @Override
    public <T> Observable<Notification<T>> channel(int id, DeliveryMethod method, ObservableFactoryNoArg<T> restartableFactory) {
        return restartables.channel(id, method, restartableFactory);
    }

    @Override
    public <A, T> Observable<Notification<T>> channel(int id, DeliveryMethod method, ObservableFactory<A, T> factory) {
        return restartables.channel(id, method, factory);
    }

    @Override
    public void launch(int id) {
        restartables.launch(id);
    }

    @Override
    public void launch(int id, Object arg) {
        restartables.launch(id, arg);
    }

    @Override
    public void dismiss(int id) {
        restartables.dismiss(id);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (bundle == null)
            restartables = new RestartableSet(out = new ValueMap.Builder());
        else {
            ValueMap map = bundle.getParcelable("restartables");
            restartables = new RestartableSet(map, out = map.toBuilder());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelable("restartables", out.build());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        connect = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
        connect = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (connect) {
            subscription = onConnect();
            connect = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (subscription != null)
            subscription.unsubscribe();

        if (getActivity().isFinishing())
            dismissRestartables();
    }
}
