package satellite.example.single;

import android.os.Bundle;
import android.view.View;

import rx.functions.Action1;
import satellite.SessionType;
import satellite.example.BaseLaunchActivity;
import satellite.example.R;
import satellite.util.RxNotification;

public class SingleLaunchActivity extends BaseLaunchActivity {

    private static final int SATELLITE_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        findViewById(R.id.launch)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlCenter().launch(SATELLITE_ID, ExampleSingleSatelliteFactory.missionStatement(10));
                }
            });
        findViewById(R.id.drop)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlCenter().dropSatellite(SATELLITE_ID);
                }
            });

        controlCenter().satelliteFactory(SATELLITE_ID, new ExampleSingleSatelliteFactory());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstOnResume()) {
            add(
                controlCenter().<Integer>connection(SATELLITE_ID, SessionType.SINGLE)
                    .subscribe(RxNotification.split(
                        new Action1<Integer>() {
                            @Override
                            public void call(Integer o) {
                                log("SINGLE: onNext " + o);
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                log("SINGLE: onError " + throwable);
                            }
                        })));
        }
    }
}
