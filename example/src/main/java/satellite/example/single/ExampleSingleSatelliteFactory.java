package satellite.example.single;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import satellite.SatelliteFactory;
import satellite.io.InputMap;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleSingleSatelliteFactory implements SatelliteFactory<InputMap, Integer> {

    public static InputMap missionStatement(int from) {
        return InputMap.sequence("from", from);
    }

    @Override
    public Observable<Integer> call(InputMap missionStatement) {
        return Observable.just((int)missionStatement.get("from"))
            .delay(1, TimeUnit.SECONDS, mainThread())
            .first();
    }
}
