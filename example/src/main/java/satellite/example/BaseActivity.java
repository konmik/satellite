package satellite.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import rx.Notification;
import rx.functions.Action1;
import satellite.MissionControlCenter;

public class BaseActivity extends AppCompatActivity {

    private static final String CONTROL_CENTER = "control_center";

    private MissionControlCenter controlCenter;
    private boolean isFirstOnResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            controlCenter = savedInstanceState.getParcelable(CONTROL_CENTER);
        else
            controlCenter = new MissionControlCenter();
        isFirstOnResume = savedInstanceState != null;
    }

    public boolean isFirstOnResume() {
        return isFirstOnResume;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFirstOnResume = false;
    }

    public MissionControlCenter controlCenter() {
        return controlCenter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        controlCenter.restoreSatellites();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing())
            controlCenter.dismiss();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CONTROL_CENTER, controlCenter);
    }

    public <T> Action1<Notification<T>> split(final Action1<T> onNext, final Action1<Throwable> onError) {
        return new Action1<Notification<T>>() {
            @Override
            public void call(Notification<T> notification) {
                if (notification.isOnNext())
                    onNext.call(notification.getValue());
                else if (notification.isOnError())
                    onError.call(notification.getThrowable());
            }
        };
    }
}
