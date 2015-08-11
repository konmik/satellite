package satellite.example.cache;

import android.os.Bundle;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;
import satellite.SatelliteFactory;
import satellite.util.LogTransformer;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleCacheSatelliteFactory implements SatelliteFactory<Integer> {

    public static final String FROM_KEY = "from";

    public static Bundle missionStatement(int from) {
        Bundle statement = new Bundle();
        statement.putInt(FROM_KEY, from);
        return statement;
    }

    @Override
    public Observable<Integer> call(final Bundle missionStatement) {
        return Observable.interval(1, 1, TimeUnit.SECONDS, mainThread())
            .map(new Func1<Long, Integer>() {
                @Override
                public Integer call(Long time) {
                    return (int)(time + missionStatement.getInt(FROM_KEY));
                }
            });
    }
}
