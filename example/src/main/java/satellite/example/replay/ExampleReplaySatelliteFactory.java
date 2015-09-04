package satellite.example.replay;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import satellite.SatelliteFactory;
import satellite.io.InputMap;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleReplaySatelliteFactory implements SatelliteFactory<InputMap, Integer> {

    public static InputMap missionStatement(int from) {
        return new InputMap("from", from);
    }

    @Override
    public Observable<Integer> call(InputMap missionStatement) {
        return Observable.interval(0, 1, TimeUnit.SECONDS, mainThread())
            .map(tick -> (int)(long)(tick) + (int)missionStatement.get("from"));
    }
}
