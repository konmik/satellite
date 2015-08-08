package satellite.util;

import android.util.Log;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

public class LogTransformer<T> implements Observable.Transformer<T, T> {

    private final String tag;

    public LogTransformer(String tag) {
        this.tag = tag;
    }

    @Override
    public Observable<T> call(Observable<T> observable) {
        return observable
            .doOnNext(new Action1<T>() {
                @Override
                public void call(T t) {
                    Log.v(tag, "onNext: " + t);
                }
            })
            .doOnError(new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    Log.v(tag, "onError: " + throwable);
                }
            })
            .doOnCompleted(new Action0() {
                @Override
                public void call() {
                    Log.v(tag, "onComplete");
                }
            })
            .doOnSubscribe(new Action0() {
                @Override
                public void call() {
                    Log.v(tag, "onSubscribe");
                }
            })
            .doOnUnsubscribe(new Action0() {
                @Override
                public void call() {
                    Log.v(tag, "onUnsubscribe");
                }
            });
    }
}
