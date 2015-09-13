package satellite;

import rx.Observable;
import rx.functions.Func0;

/**
 * {@code RestartableFactoryNoArg} defines an interface which can be used to instantiate
 * restartable observables without arguments.
 *
 * @param <T> a type of constructed observable`s onNext value.
 */
public interface RestartableFactoryNoArg<T> extends Func0<Observable<T>> {
}
