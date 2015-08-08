package satellite.example;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import rx.Notification;
import rx.functions.Action1;
import rx.internal.util.SubscriptionList;
import satellite.util.LogTransformer;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static final int SATELLITE_ID = 1;

    private SubscriptionList connections = new SubscriptionList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controlCenter().satelliteFactory(SATELLITE_ID, new ExampleSatelliteFactory());

        connections.add(controlCenter().<Integer>connection(SATELLITE_ID)
            .compose(new LogTransformer<Notification<Integer>>("Earth " + TAG + " <--"))
            .subscribe(split(
                new Action1<Integer>() {
                    @Override
                    public void call(Integer o) {
                        Log.d(TAG, "onNext " + o);
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG, "onError " + throwable);
                    }
                })));

        findViewById(R.id.start)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlCenter().launch(SATELLITE_ID, new Bundle());
                }
            });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connections.unsubscribe();
    }
}
