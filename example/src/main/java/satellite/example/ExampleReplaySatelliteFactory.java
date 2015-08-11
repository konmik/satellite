package satellite.example;

import android.os.Bundle;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;
import satellite.SatelliteFactory;
import satellite.util.LogTransformer;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleReplaySatelliteFactory implements SatelliteFactory<Integer> {

    public static Bundle missionStatement(int from) {
        Bundle statement = new Bundle();
        statement.putInt("from", from);
        return statement;
    }

    @Override
    public Observable<Integer> call(final Bundle missionStatement) {
        return Observable.interval(0, 1, TimeUnit.SECONDS, mainThread())
            .map(new Func1<Long, Integer>() {
                @Override
                public Integer call(Long aLong) {
                    return (int)(long)(aLong + missionStatement.getInt("from"));
                }
            })
            .compose(new LogTransformer<Integer>(getClass().getSimpleName() + " -->"));
    }
}
