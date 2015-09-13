package satellite.example.single;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import satellite.SatelliteFactory;
import satellite.io.StateMap;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleSingleSatelliteFactory implements SatelliteFactory<StateMap, Integer> {

    public static StateMap missionStatement(int from) {
        return StateMap.sequence("from", from);
    }

    @Override
    public Observable<Integer> call(StateMap missionStatement) {
        return Observable.just((int)missionStatement.get("from"))
            .delay(1, TimeUnit.SECONDS, mainThread())
            .first();
    }
}
