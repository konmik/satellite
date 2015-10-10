package satellite.example.replay;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import satellite.ObservableFactory;
import valuemap.ValueMap;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleReplayObservableFactory implements ObservableFactory<ValueMap, Integer> {

    public static ValueMap argument(int from) {
        return ValueMap.map("from", from);
    }

    @Override
    public Observable<Integer> call(ValueMap arg) {
        return Observable.interval(0, 1, TimeUnit.SECONDS, mainThread())
            .map(tick -> (int)(long)(tick) + (int)arg.get("from"));
    }
}
