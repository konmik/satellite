package satellite.example;

import android.os.Bundle;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Notification;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import satellite.SessionType;
import satellite.util.LogTransformer;
import satellite.util.RxNotification;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static final int SATELLITE_ID = 1;

    private Subscription stationSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.launch)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlCenter().launch(SATELLITE_ID, ExampleSingleSatelliteFactory.missionStatement(10));
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
                    stationSubscription.unsubscribe();
                    Observable.just(1)
                        .delay(5, TimeUnit.SECONDS, mainThread())
                        .subscribe(new Action1<Integer>() {
                            @Override
                            public void call(Integer integer) {
                                if (!isDestroyed())
                                    setupSingle();
                            }
                        });
                }
            });

        controlCenter().satelliteFactory(SATELLITE_ID, new ExampleSingleSatelliteFactory());

        add(Observable.interval(1, 1, TimeUnit.SECONDS, mainThread())
            .subscribe(new Action1<Long>() {
                @Override
                public void call(Long ignored) {
                    StringBuilder builder = new StringBuilder();
                    controlCenter().printSpaceStation(new StringBuilderPrinter(builder));
                    TextView report = (TextView)findViewById(R.id.stationReport);
                    report.setText(builder.toString());
                }
            }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstOnResume())
            setupSingle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stationSubscription.unsubscribe();
    }

    private void setupSingle() {
        stationSubscription = controlCenter().<Integer>connection(SATELLITE_ID, SessionType.SINGLE)
            .compose(new LogTransformer<Notification<Integer>>("Earth " + TAG + " <--"))
            .subscribe(RxNotification.split(
                new Action1<Integer>() {
                    @Override
                    public void call(Integer o) {
                        log("SINGLE: onNext " + o);
                    }
                },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        log("SINGLE: onError " + throwable);
                    }
                }));
    }

    private void log(String message) {
        TextView textView = (TextView)findViewById(R.id.textView);
        textView.setText(textView.getText() + "\n" + message);
        final ScrollView scrollView = (ScrollView)findViewById(R.id.scrollView);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}
