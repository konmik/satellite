package satellite.example;

import android.os.Bundle;

import java.util.concurrent.TimeUnit;

import satellite.SatelliteFactory;
import rx.Observable;
import rx.functions.Func1;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleSatelliteFactory implements SatelliteFactory<Integer> {
    @Override
    public Observable<Integer> call(Bundle missionStatement) {
        return Observable.interval(missionStatement.getInt("from"), 1, TimeUnit.SECONDS, mainThread()).map(new Func1<Long, Integer>() {
            @Override
            public Integer call(Long aLong) {
                return (int)(long)aLong;
            }
        });
    }
}
