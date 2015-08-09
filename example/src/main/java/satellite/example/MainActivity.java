package satellite.example;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Notification;
import rx.Observable;
import rx.functions.Action1;
import rx.internal.util.SubscriptionList;
import satellite.SessionType;
import satellite.util.LogTransformer;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static final int SATELLITE_ID = 1;

    private SubscriptionList connections = new SubscriptionList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();

        findViewById(R.id.launch)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlCenter().launch(SATELLITE_ID, ExampleSatelliteFactory.missionStatement(10));
                }
            });
        findViewById(R.id.drop)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlCenter().dropSatellite(SATELLITE_ID);
                }
            });
        findViewById(R.id.hide)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connections.unsubscribe();
                    connections = new SubscriptionList();
                    Observable.just(1)
                        .delay(5, TimeUnit.SECONDS)
                        .observeOn(mainThread())
                        .subscribe(new Action1<Integer>() {
                            @Override
                            public void call(Integer integer) {
                                setup();
                            }
                        });
                }
            });
    }

    private void setup() {
        final TextView log = (TextView)findViewById(R.id.log);

        controlCenter().satelliteFactory(SATELLITE_ID, new ExampleSatelliteFactory());

        connections.add(controlCenter().<Integer>connection(SATELLITE_ID, SessionType.FIRST)
            .compose(new LogTransformer<Notification<Integer>>("Earth " + TAG + " <--"))
            .subscribe(split(
                new Action1<Integer>() {
                    @Override
                    public void call(Integer o) {
                        log.setText(log.getText() + "\n" + "onNext " + o);
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        log.setText(log.getText() + "\n" + "onError " + throwable);
                    }
                })));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        connections.unsubscribe();
    }
}
