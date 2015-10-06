package satellite.example.cache;

import android.os.Bundle;
import android.widget.TextView;

import satellite.RestartableSet;
import satellite.example.BaseLaunchActivity;
import satellite.example.R;
import satellite.state.StateMap;
import satellite.util.RxNotification;
import satellite.util.SubjectFactory;

public class CacheConnectionActivity extends BaseLaunchActivity {

    public static final int CONNECTION_ID = 1;

    private RestartableSet connections;
    private StateMap.Builder out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_satellite);
        ((TextView)findViewById(R.id.title)).setText("Cache result connection");

        findViewById(R.id.launch).setOnClickListener(v -> connections.launch(CONNECTION_ID, ExampleCacheRestartableFactory.argument(10)));
        findViewById(R.id.drop).setOnClickListener(v -> connections.dismiss(CONNECTION_ID));

        if (savedInstanceState == null)
            this.connections = new RestartableSet(out = new StateMap.Builder());
        else {
            StateMap map = savedInstanceState.getParcelable("connections");
            this.connections = new RestartableSet(map, out = map.toBuilder());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("connections", out.build());
    }

    @Override
    protected void onCreateConnections() {
        super.onCreateConnections();

        unsubscribeOnDestroy(
            connections.restartable(CONNECTION_ID, SubjectFactory.behaviorSubject(), new ExampleCacheRestartableFactory())
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
            connections.dismiss();
    }
}
