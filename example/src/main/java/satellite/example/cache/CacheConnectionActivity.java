package satellite.example.cache;

import android.os.Bundle;
import android.widget.TextView;

import satellite.EarthBase;
import satellite.example.BaseLaunchActivity;
import satellite.example.R;
import satellite.util.RxNotification;
import satellite.util.SubjectFactory;

public class CacheConnectionActivity extends BaseLaunchActivity {

    public static final int SATELLITE_ID = 1;

    private EarthBase earthBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_satellite);
        ((TextView)findViewById(R.id.title)).setText("Cache result connection");

        findViewById(R.id.launch).setOnClickListener(v -> earthBase.launch(SATELLITE_ID, ExampleCacheSatelliteFactory.missionStatement(10)));
        findViewById(R.id.drop).setOnClickListener(v -> earthBase.dismiss(SATELLITE_ID));

        earthBase = savedInstanceState == null ? new EarthBase() : new EarthBase(savedInstanceState.getParcelable("base"));
    }

    @Override
    protected void onCreateConnections() {
        super.onCreateConnections();

        unsubscribeOnDestroy(
            earthBase.connection(SATELLITE_ID, SubjectFactory.behaviorSubject(), new ExampleCacheSatelliteFactory())
                .subscribe(RxNotification.split(
                    value -> {
                        log("SINGLE: onNext " + value);
                        onNext(value);
                    },
                    throwable -> log("SINGLE: onError " + throwable))));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing())
            earthBase.dismissAll();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("base", earthBase.instanceState());
    }
}
