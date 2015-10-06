package satellite.example.replay;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import satellite.RestartableFactory;
import statemap.StateMap;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleReplayRestartableFactory implements RestartableFactory<StateMap, Integer> {

    public static StateMap argument(int from) {
        return StateMap.sequence("from", from);
    }

    @Override
    public Observable<Integer> call(StateMap arg) {
        return Observable.interval(0, 1, TimeUnit.SECONDS, mainThread())
            .map(tick -> (int)(long)(tick) + (int)arg.get("from"));
    }
}
