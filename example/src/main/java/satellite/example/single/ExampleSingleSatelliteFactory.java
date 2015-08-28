package satellite.example.single;

import android.os.Bundle;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import satellite.SatelliteFactory;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public class ExampleSingleSatelliteFactory implements SatelliteFactory<Integer> {

    public static final String FROM_KEY = "from";

    public static Bundle missionStatement(int from) {
        Bundle statement = new Bundle();
        statement.putInt(FROM_KEY, from);
        return statement;
    }

    @Override
    public Observable<Integer> call(final Bundle missionStatement) {
        return Observable.just(missionStatement.getInt(FROM_KEY))
            .delay(1, TimeUnit.SECONDS, mainThread())
            .first();
    }
}
