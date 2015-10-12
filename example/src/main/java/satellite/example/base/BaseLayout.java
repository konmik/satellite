package satellite.example.base;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

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

public class BaseLayout extends FrameLayout implements Launcher {

    private RestartableSet restartables;
    private Subscription subscription;
    private ValueMap.Builder out;

    public BaseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * This method is being called during the {@link #onAttachedToWindow()} call.
     * The returned {@link Subscription} will be unsubscribed during {@link #onDetachedFromWindow()}.
     *
     * You can combine multiple subscriptions with {@link Subscriptions#from(Subscription...)} method.
     */
    protected Subscription onConnect() {
        return Subscriptions.empty();
    }

    /**
     * Call this method when you're not going to reattach the {@link BaseLayout} anymore,
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
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle)state;
        super.onRestoreInstanceState(bundle.getParcelable("super"));
        ValueMap map = bundle.getParcelable("restartables");
        restartables = new RestartableSet(map, out = map.toBuilder());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("restartables", out.build());
        bundle.putParcelable("super", super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (isInEditMode())
            return;

        if (restartables == null)
            restartables = new RestartableSet(out = new ValueMap.Builder());

        subscription = onConnect();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        subscription.unsubscribe();
        if (getActivity(getContext()).isFinishing())
            dismissRestartables();
    }

    private static Activity getActivity(Context context) {
        while (!(context instanceof Activity) && context instanceof ContextWrapper)
            context = ((ContextWrapper)context).getBaseContext();
        if (!(context instanceof Activity))
            throw new IllegalStateException("Expected an activity context, got " + context.getClass().getSimpleName());
        return (Activity)context;
    }
}
