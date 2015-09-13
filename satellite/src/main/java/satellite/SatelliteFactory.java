package satellite;

import rx.Observable;
import rx.functions.Func1;

/**
 * {@code SatelliteFactory} defines an interface which can be used to instantiate
 * satellites from a given argument.
 *
 * @param <A> an argument type. It must fit {@link android.os.Parcel#writeValue(Object)}
 *            method requirements.
 * @param <T> a type of satellite's onNext value.
 */
public interface SatelliteFactory<A, T> extends Func1<A, Observable<T>> {
}
