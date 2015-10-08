package satellite.example.cache;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import satellite.RestartableFactory;
import valuemap.ValueMap;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleCacheRestartableFactory implements RestartableFactory<ValueMap, Integer> {

    public static ValueMap argument(int from) {
        return ValueMap.map("from", from);
    }

    @Override
    public Observable<Integer> call(ValueMap arg) {
        return Observable.interval(1, 1, TimeUnit.SECONDS, mainThread())
            .map(time -> (int)(time + (int)arg.get("from")));
    }
}
