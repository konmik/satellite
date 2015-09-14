package satellite.example.cache;

import android.os.Bundle;
import android.widget.TextView;

import satellite.RestartableConnectionSet;
import satellite.example.BaseLaunchActivity;
import satellite.example.R;
import satellite.util.RxNotification;
import satellite.util.SubjectFactory;

public class CacheConnectionActivity extends BaseLaunchActivity {

    public static final int CONNECTION_ID = 1;

    private RestartableConnectionSet restartableConnectionSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_satellite);
        ((TextView)findViewById(R.id.title)).setText("Cache result connection");

        findViewById(R.id.launch).setOnClickListener(v -> restartableConnectionSet.launch(CONNECTION_ID, ExampleCacheRestartableFactory.argument(10)));
        findViewById(R.id.drop).setOnClickListener(v -> restartableConnectionSet.dismiss(CONNECTION_ID));

        restartableConnectionSet = savedInstanceState == null ? new RestartableConnectionSet() : new RestartableConnectionSet(savedInstanceState.getParcelable("base"));
    }

    @Override
    protected void onCreateConnections() {
        super.onCreateConnections();

        unsubscribeOnDestroy(
            restartableConnectionSet.connection(CONNECTION_ID, SubjectFactory.behaviorSubject(), new ExampleCacheRestartableFactory())
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
            restartableConnectionSet.dismiss();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("base", restartableConnectionSet.instanceState());
    }
}
