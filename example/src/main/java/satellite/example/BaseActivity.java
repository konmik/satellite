package satellite.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import rx.Subscription;
import rx.internal.util.SubscriptionList;
import satellite.MissionControlCenter;

public class BaseActivity extends AppCompatActivity {

    private static final String CONTROL_CENTER = "control_center";

    private MissionControlCenter controlCenter;
    private boolean isFirstOnCreate = true;
    private boolean isFirstOnResume = true;
    private boolean isDestroyed = false;
    private SubscriptionList subscriptions = new SubscriptionList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            controlCenter = savedInstanceState.getParcelable(CONTROL_CENTER);
        else
            controlCenter = new MissionControlCenter();
        isFirstOnCreate = savedInstanceState == null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CONTROL_CENTER, controlCenter);
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        super.onDestroy();
        subscriptions.unsubscribe();
        if (isFinishing())
            controlCenter.dismiss();
    }

    public boolean isFirstOnCreate() {
        return isFirstOnCreate;
    }

    public boolean isFirstOnResume() {
        return isFirstOnResume;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public MissionControlCenter controlCenter() {
        return controlCenter;
    }

    public void add(Subscription subscription) {
        subscriptions.add(subscription);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstOnResume)
            controlCenter.restoreSatellites();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFirstOnResume = false;
    }
}
