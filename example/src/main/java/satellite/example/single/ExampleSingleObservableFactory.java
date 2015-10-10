package satellite.example.single;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import satellite.ObservableFactory;
import valuemap.ValueMap;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleSingleObservableFactory implements ObservableFactory<ValueMap, Integer> {

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
