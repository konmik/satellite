package satellite;

import android.os.Parcelable;

import rx.Observable;
import rx.functions.Func1;

public interface SatelliteFactory<A extends Parcelable, T> extends Func1<A, Observable<T>> {
}
