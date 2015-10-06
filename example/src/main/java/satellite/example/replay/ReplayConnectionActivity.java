package satellite.example.replay;

import android.os.Bundle;
import android.widget.TextView;

import satellite.Restartable;
import satellite.example.BaseLaunchActivity;
import satellite.example.R;
import satellite.state.StateMap;
import satellite.util.RxNotification;
import satellite.util.SubjectFactory;

public class ReplayConnectionActivity extends BaseLaunchActivity {

    private Restartable connection;
    private StateMap.Builder out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_satellite);
        ((TextView)findViewById(R.id.title)).setText("Cache result connection");

        findViewById(R.id.launch)
            .setOnClickListener(v -> connection.launch(ExampleReplayRestartableFactory.argument(10)));
        findViewById(R.id.drop)
            .setOnClickListener(v -> connection.dismiss());

        if (savedInstanceState == null)
            this.connection = new Restartable(out = new StateMap.Builder());
        else {
            StateMap map = savedInstanceState.getParcelable("connection");
            this.connection = new Restartable(map, out = map.toBuilder());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("connection", out.build());
    }

    @Override
    protected void onCreateConnections() {
        super.onCreateConnections();

        unsubscribeOnDestroy(
            connection.channel(SubjectFactory.replaySubject(), new ExampleReplayRestartableFactory())
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
            connection.dismiss();
    }
}
