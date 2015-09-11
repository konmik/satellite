package satellite;

import rx.Observable;
import rx.functions.Func1;

public interface SatelliteFactory<A, T> extends Func1<A, Observable<T>> {
}
