package satellite.example;

import android.os.Bundle;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import satellite.SatelliteFactory;
import satellite.util.LogTransformer;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleSingleSatelliteFactory implements SatelliteFactory<Integer> {

    public static Bundle missionStatement(int from) {
        Bundle statement = new Bundle();
        statement.putInt("from", from);
        return statement;
    }

    @Override
    public Observable<Integer> call(final Bundle missionStatement) {
        return Observable.just(missionStatement.getInt("from"))
            .delay(1, TimeUnit.SECONDS, mainThread())
            .compose(new LogTransformer<Integer>(getClass().getSimpleName() + " -->"));
    }
}
