package satellite;

import rx.Observable;
import rx.functions.Func0;

/**
 * {@code ObservableFactoryNoArg} defines an interface which can be used to instantiate
 * observables without arguments.
 *
 * @param <T> a type of constructed observable`s onNext value.
 */
public interface ObservableFactoryNoArg<T> extends Func0<Observable<T>> {
}
