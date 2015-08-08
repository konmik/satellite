package satellite;

import android.os.Bundle;

import rx.Observable;
import rx.functions.Func1;

public interface SatelliteFactory<T> extends Func1<Bundle, Observable<T>> {
    Observable<T> call(Bundle bundle);
}
