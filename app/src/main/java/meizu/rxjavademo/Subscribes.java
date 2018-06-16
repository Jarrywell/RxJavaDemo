package meizu.rxjavademo;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class Subscribes {

    private static final String TAG = DLog.TAG;
    private CompositeDisposable mCompositeDisposable;


    public Subscribes() {
        mCompositeDisposable = new CompositeDisposable();
    }


    public <T> DisposableObserver<T> subscribe(Observable<T> observable) {
        DisposableObserver<T> observer = createDisposableObserver();
        return subscribe(observable, observer);
    }

    public <T> DisposableObserver<T> subscribe(Observable<T> observable, DisposableObserver<T> observer) {
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
        mCompositeDisposable.add(observer);

        return observer;
    }


    public void release() {
        if (mCompositeDisposable != null) {
            DLog.i(TAG, "release() CompositeDisposable size: " + mCompositeDisposable.size());
            mCompositeDisposable.clear();
            mCompositeDisposable = null;
        }
    }

    private  <T> DisposableObserver<T> createDisposableObserver() {
        return new DisposableObserver<T>() {
            @Override
            public void onNext(T value) {
                DLog.i(TAG, "onNext() value: " + value);
            }

            @Override
            public void onError(Throwable e) {
                DLog.i(TAG, "onError()", e);
            }

            @Override
            public void onComplete() {
                DLog.i(TAG, "onComplete()");
            }
        };
    }
}
