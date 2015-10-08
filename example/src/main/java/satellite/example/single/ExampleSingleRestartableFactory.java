package satellite.example.single;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import satellite.RestartableFactory;
import valuemap.ValueMap;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleSingleRestartableFactory implements RestartableFactory<ValueMap, Integer> {

    public static ValueMap argument(int from) {
        return ValueMap.map("from", from);
    }

    @Override
    public Observable<Integer> call(ValueMap arg) {
        return Observable.just((int)arg.get("from"))
            .delay(1, TimeUnit.SECONDS, mainThread())
            .first();
    }
}
