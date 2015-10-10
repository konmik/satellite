package satellite;

import rx.Observable;
import rx.functions.Func1;

/**
 * {@code ObservableFactory} defines an interface which can be used to instantiate
 * restartable observables from a given argument.
 *
 * @param <A> an argument type. It must fit {@link android.os.Parcel#writeValue(Object)}
 *            method requirements.
 * @param <T> a type of constructed observable`s onNext value.
 */
public interface ObservableFactory<A, T> extends Func1<A, Observable<T>> {
}
