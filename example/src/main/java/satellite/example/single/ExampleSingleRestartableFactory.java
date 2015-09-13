package satellite.example.single;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import satellite.RestartableFactory;
import satellite.state.StateMap;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleSingleRestartableFactory implements RestartableFactory<StateMap, Integer> {

    public static StateMap argument(int from) {
        return StateMap.sequence("from", from);
    }

    @Override
    public Observable<Integer> call(StateMap arg) {
        return Observable.just((int)arg.get("from"))
            .delay(1, TimeUnit.SECONDS, mainThread())
            .first();
    }
}
