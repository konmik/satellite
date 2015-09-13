package satellite.example.replay;

import android.os.Bundle;
import android.widget.TextView;

import satellite.MissionControlCenter;
import satellite.example.BaseLaunchActivity;
import satellite.example.R;
import satellite.state.StateMap;
import satellite.util.RxNotification;
import satellite.util.SubjectFactory;

public class ReplayConnectionActivity extends BaseLaunchActivity {

    private MissionControlCenter<StateMap, Integer> controlCenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_satellite);
        ((TextView)findViewById(R.id.title)).setText("Cache result connection");

        findViewById(R.id.launch)
            .setOnClickListener(v -> controlCenter.launch(ExampleReplaySatelliteFactory.missionStatement(10)));
        findViewById(R.id.drop)
            .setOnClickListener(v -> controlCenter.dismiss());

        controlCenter = savedInstanceState == null ?
            new MissionControlCenter<>() :
            new MissionControlCenter<>(savedInstanceState.getParcelable("center"));
    }

    @Override
    protected void onCreateConnections() {
        super.onCreateConnections();

        unsubscribeOnDestroy(
            controlCenter.connection(SubjectFactory.replaySubject(), new ExampleReplaySatelliteFactory())
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
            controlCenter.dismiss();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("center", controlCenter.instanceState());
    }
}
