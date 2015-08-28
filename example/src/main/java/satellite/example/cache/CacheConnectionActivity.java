package satellite.example.cache;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import rx.functions.Action1;
import satellite.EarthBase;
import satellite.connections.CacheConnectionFactory;
import satellite.example.BaseLaunchActivity;
import satellite.example.R;
import satellite.util.RxNotification;

public class CacheConnectionActivity extends BaseLaunchActivity {

    public static final String EARTH_BASE_KEY = "base";
    public static final int SATELLITE_ID = 1;

    private EarthBase earthBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_satellite);
        ((TextView)findViewById(R.id.title)).setText("Cache result connection");

        findViewById(R.id.launch)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    earthBase.launch(SATELLITE_ID, ExampleCacheSatelliteFactory.missionStatement(10));
                }
            });
        findViewById(R.id.drop)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    earthBase.dismiss(SATELLITE_ID);
                }
            });

        earthBase = new EarthBase(savedInstanceState == null ? null : savedInstanceState.getBundle(EARTH_BASE_KEY), SATELLITE_ID);
    }

    @Override
    protected void onCreateConnections() {
        super.onCreateConnections();

        unsubscribeOnDestroy(
            earthBase.connection(SATELLITE_ID, new CacheConnectionFactory<>(new ExampleCacheSatelliteFactory()))
                .subscribe(RxNotification.split(
                    new Action1<Integer>() {
                        @Override
                        public void call(Integer value) {
                            log("SINGLE: onNext " + value);
                            onNext(value);
                        }
                    },
                    new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            log("SINGLE: onError " + throwable);
                        }
                    })));
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
        outState.putBundle("base", earthBase.saveInstanceState());
    }
}
