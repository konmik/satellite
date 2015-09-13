package satellite.example.cache;

import android.os.Bundle;
import android.widget.TextView;

import satellite.RestartableConnectionGroup;
import satellite.example.BaseLaunchActivity;
import satellite.example.R;
import satellite.util.RxNotification;
import satellite.util.SubjectFactory;

public class CacheConnectionActivity extends BaseLaunchActivity {

    public static final int SATELLITE_ID = 1;

    private RestartableConnectionGroup restartableConnectionGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_satellite);
        ((TextView)findViewById(R.id.title)).setText("Cache result connection");

        findViewById(R.id.launch).setOnClickListener(v -> restartableConnectionGroup.launch(SATELLITE_ID, ExampleCacheRestartableFactory.missionStatement(10)));
        findViewById(R.id.drop).setOnClickListener(v -> restartableConnectionGroup.dismiss(SATELLITE_ID));

        restartableConnectionGroup = savedInstanceState == null ? new RestartableConnectionGroup() : new RestartableConnectionGroup(savedInstanceState.getParcelable("base"));
    }

    @Override
    protected void onCreateConnections() {
        super.onCreateConnections();

        unsubscribeOnDestroy(
            restartableConnectionGroup.connection(SATELLITE_ID, SubjectFactory.behaviorSubject(), new ExampleCacheRestartableFactory())
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
            restartableConnectionGroup.dismissAll();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("base", restartableConnectionGroup.instanceState());
    }
}
