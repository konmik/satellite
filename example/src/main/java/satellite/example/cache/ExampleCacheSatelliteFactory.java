package satellite.example.cache;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;
import satellite.SatelliteFactory;
import satellite.io.InputMap;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleCacheSatelliteFactory implements SatelliteFactory<Integer> {

    public static final String FROM_KEY = "from";

    public static InputMap missionStatement(int from) {
        return new InputMap(FROM_KEY, from);
    }

    @Override
    public Observable<Integer> call(InputMap missionStatement) {
        final int from = missionStatement.get(FROM_KEY);
        return Observable.interval(1, 1, TimeUnit.SECONDS, mainThread())
            .map(new Func1<Long, Integer>() {
                @Override
                public Integer call(Long time) {
                    return (int)(time + from);
                }
            });
    }
}
