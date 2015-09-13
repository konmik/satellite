package satellite.example.replay;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import satellite.SatelliteFactory;
import satellite.io.StateMap;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleReplaySatelliteFactory implements SatelliteFactory<StateMap, Integer> {

    public static StateMap missionStatement(int from) {
        return StateMap.sequence("from", from);
    }

    @Override
    public Observable<Integer> call(StateMap missionStatement) {
        return Observable.interval(0, 1, TimeUnit.SECONDS, mainThread())
            .map(tick -> (int)(long)(tick) + (int)missionStatement.get("from"));
    }
}
