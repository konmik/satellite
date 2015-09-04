package satellite.example.single;

import android.os.Bundle;
import android.widget.TextView;

import satellite.MissionControlCenter;
import satellite.example.BaseLaunchActivity;
import satellite.example.R;
import satellite.io.InputMap;
import satellite.util.RxNotification;
import satellite.util.SubjectFactory;

public class SingleConnectionActivity extends BaseLaunchActivity {

    private MissionControlCenter<InputMap, Integer> controlCenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_satellite);
        ((TextView)findViewById(R.id.title)).setText("Single result connection");

        findViewById(R.id.launch).setOnClickListener(v -> controlCenter.launch(ExampleSingleSatelliteFactory.missionStatement(10)));
        findViewById(R.id.drop).setOnClickListener(v -> controlCenter.dismiss());

        controlCenter = savedInstanceState == null ? new MissionControlCenter<>() : new MissionControlCenter<>(savedInstanceState.getParcelable("center"));
    }

    @Override
    protected void onCreateConnections() {
        super.onCreateConnections();

        unsubscribeOnDestroy(
            controlCenter.connection(SubjectFactory.behaviorSubject(), new ExampleSingleSatelliteFactory())
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
