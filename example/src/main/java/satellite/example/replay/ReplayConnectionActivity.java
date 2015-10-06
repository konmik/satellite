package satellite.example.replay;

import android.os.Bundle;
import android.widget.TextView;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import satellite.DeliveryMethod;
import satellite.example.BaseLaunchActivity;
import satellite.example.R;
import satellite.RxNotification;

public class ReplayConnectionActivity extends BaseLaunchActivity {

    public static final int REPLAY_RESTARTABLE_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_satellite);
        ((TextView)findViewById(R.id.title)).setText("Cache result connection");

        findViewById(R.id.launch).setOnClickListener(v -> launch(REPLAY_RESTARTABLE_ID, ExampleReplayRestartableFactory.argument(10)));
        findViewById(R.id.drop).setOnClickListener(v -> dismiss(REPLAY_RESTARTABLE_ID));
    }

    @Override
    protected Subscription onConnect() {
        return new CompositeSubscription(super.onConnect(),

            restartable(REPLAY_RESTARTABLE_ID, DeliveryMethod.REPLAY, new ExampleReplayRestartableFactory())
                .subscribe(RxNotification.split(
                    value -> {
                        log("REPLAY: onNext " + value);
                        onNext(value);
                    },
                    throwable -> log("REPLAY: onError " + throwable))));
    }
}
