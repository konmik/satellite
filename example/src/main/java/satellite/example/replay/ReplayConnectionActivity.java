package satellite.example.replay;

import android.os.Bundle;
import android.widget.TextView;

import satellite.RestartableConnection;
import satellite.example.BaseLaunchActivity;
import satellite.example.R;
import satellite.util.RxNotification;
import satellite.util.SubjectFactory;

public class ReplayConnectionActivity extends BaseLaunchActivity {

    private RestartableConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_satellite);
        ((TextView)findViewById(R.id.title)).setText("Cache result connection");

        findViewById(R.id.launch)
            .setOnClickListener(v -> connection.launch(ExampleReplayRestartableFactory.argument(10)));
        findViewById(R.id.drop)
            .setOnClickListener(v -> connection.dismiss());

        connection = savedInstanceState == null ?
            new RestartableConnection() :
            new RestartableConnection(savedInstanceState.getParcelable("connection"));
    }

    @Override
    protected void onCreateConnections() {
        super.onCreateConnections();

        unsubscribeOnDestroy(
            connection.connection(SubjectFactory.replaySubject(), new ExampleReplayRestartableFactory())
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("connection", connection.instanceState());
    }
}
