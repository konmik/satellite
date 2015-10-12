package satellite.example.single;

import android.os.Bundle;
import android.widget.TextView;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import satellite.DeliveryMethod;
import satellite.example.BaseLaunchActivity;
import satellite.example.R;
import satellite.RxNotification;

public class SingleConnectionActivity extends BaseLaunchActivity {

    public static final int SINGLE_RESTARTABLE_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_satellite);
        ((TextView)findViewById(R.id.title)).setText("Single result connection");

        findViewById(R.id.launch).setOnClickListener(v -> launch(SINGLE_RESTARTABLE_ID, ExampleSingleObservableFactory.argument(10)));
        findViewById(R.id.drop).setOnClickListener(v -> dismiss(SINGLE_RESTARTABLE_ID));
    }

    @Override
    protected Subscription onConnect() {
        return new CompositeSubscription(super.onConnect(),

            channel(SINGLE_RESTARTABLE_ID, DeliveryMethod.SINGLE, new ExampleSingleObservableFactory())
                .subscribe(RxNotification.split(
                    value -> {
                        log("SINGLE: onNext " + value);
                        onNext(value);
                    },
                    throwable -> log("SINGLE: onError " + throwable))));
    }
}
