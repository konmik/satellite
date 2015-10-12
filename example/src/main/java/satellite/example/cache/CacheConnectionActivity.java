package satellite.example.cache;

import android.os.Bundle;
import android.widget.TextView;

import rx.Subscription;
import rx.subscriptions.Subscriptions;
import satellite.DeliveryMethod;
import satellite.RxNotification;
import satellite.example.BaseLaunchActivity;
import satellite.example.R;

public class CacheConnectionActivity extends BaseLaunchActivity {

    public static final int CHANNEL_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_satellite);
        ((TextView)findViewById(R.id.title)).setText("Cache result connection");

        findViewById(R.id.launch).setOnClickListener(v -> launch(CHANNEL_ID, ExampleCacheObservableFactory.argument(10)));
        findViewById(R.id.drop).setOnClickListener(v -> dismiss(CHANNEL_ID));
    }

    @Override
    protected Subscription onConnect() {
        return Subscriptions.from(super.onConnect(),

            channel(CHANNEL_ID, DeliveryMethod.LATEST, new ExampleCacheObservableFactory())
                .subscribe(RxNotification.split(
                    value -> {
                        log("CACHE: onNext " + value);
                        onNext(value);
                    },
                    throwable -> log("CACHE: onError " + throwable))));
    }
}
