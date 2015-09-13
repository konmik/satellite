package satellite.example.cache;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import satellite.RestartableFactory;
import satellite.state.StateMap;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleCacheRestartableFactory implements RestartableFactory<StateMap, Integer> {

    public static StateMap argument(int from) {
        return StateMap.sequence("from", from);
    }

    @Override
    public Observable<Integer> call(StateMap arg) {
        return Observable.interval(1, 1, TimeUnit.SECONDS, mainThread())
            .map(time -> (int)(time + (int)arg.get("from")));
    }
}
