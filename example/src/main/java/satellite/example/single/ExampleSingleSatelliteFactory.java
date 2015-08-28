package satellite.example.single;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import satellite.SatelliteFactory;
import satellite.io.InputMap;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleSingleSatelliteFactory implements SatelliteFactory<Integer> {

    public static final String FROM_KEY = "from";

    public static InputMap missionStatement(int from) {
        return new InputMap(FROM_KEY, from);
    }

    @Override
    public Observable<Integer> call(InputMap missionStatement) {
        return Observable.just((int)missionStatement.get(FROM_KEY))
            .delay(1, TimeUnit.SECONDS, mainThread())
            .first();
    }
}
