package satellite;

import rx.Observable;
import rx.functions.Func1;
import satellite.io.InputMap;

public interface SatelliteFactory<T> extends Func1<InputMap, Observable<T>> {
}
