package satellite.example.replay;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;
import satellite.SatelliteFactory;
import satellite.io.InputMap;
import satellite.util.LogTransformer;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleReplaySatelliteFactory implements SatelliteFactory<Integer> {

    public static InputMap missionStatement(int from) {
        return new InputMap("from", from);
    }

    @Override
    public Observable<Integer> call(InputMap missionStatement) {
        final int from = missionStatement.get("from");
        return Observable.interval(0, 1, TimeUnit.SECONDS, mainThread())
            .map(new Func1<Long, Integer>() {
                @Override
                public Integer call(Long aLong) {
                    return (int)(long)(aLong + from);
                }
            })
            .compose(new LogTransformer<Integer>(getClass().getSimpleName() + " -->"));
    }
}
