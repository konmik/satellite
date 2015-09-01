package satellite.example.single;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import rx.functions.Action1;
import satellite.MissionControlCenter;
import satellite.connections.SingleConnectionFactory;
import satellite.example.BaseLaunchActivity;
import satellite.example.R;
import satellite.io.InputMap;
import satellite.util.RxNotification;

public class SingleConnectionActivity extends BaseLaunchActivity {

    private MissionControlCenter controlCenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_satellite);
        ((TextView)findViewById(R.id.title)).setText("Single result connection");

        findViewById(R.id.launch)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlCenter.launch(ExampleSingleSatelliteFactory.missionStatement(10));
                }
            });
        findViewById(R.id.drop)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlCenter.dismiss();
                }
            });

        controlCenter = savedInstanceState == null ?
            new MissionControlCenter() :
            new MissionControlCenter((InputMap)savedInstanceState.getParcelable("center"));
    }

    @Override
    protected void onCreateConnections() {
        super.onCreateConnections();

        unsubscribeOnDestroy(
            controlCenter.connection(new SingleConnectionFactory<>(new ExampleSingleSatelliteFactory()))
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
            controlCenter.dismiss();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("center", controlCenter.saveInstanceState());
    }
}
