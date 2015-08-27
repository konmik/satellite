package satellite.example.cache;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import rx.functions.Action1;
import satellite.MissionControlCenter;
import satellite.example.BaseLaunchActivity;
import satellite.example.R;
import satellite.util.RxNotification;

public class CacheConnectionActivity extends BaseLaunchActivity<Integer> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_satellite);
        ((TextView)findViewById(R.id.title)).setText("Cache result connection");

        findViewById(R.id.launch)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlCenter().launch(ExampleCacheSatelliteFactory.missionStatement(10));
                }
            });
        findViewById(R.id.drop)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controlCenter().dismiss();
                }
            });
    }

    @Override
    protected MissionControlCenter.SessionType getSessionType() {
        return MissionControlCenter.SessionType.CACHE;
    }

    @Override
    protected void onCreateConnections() {
        super.onCreateConnections();

        add(
            controlCenter().connection(new ExampleCacheSatelliteFactory())
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
}
