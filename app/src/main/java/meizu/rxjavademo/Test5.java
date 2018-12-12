package meizu.rxjavademo;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * des:https://www.jianshu.com/p/464fa025229e
 * author: libingyan
 * Date: 18-12-12 10:56
 */
public class Test5 {

    private static final String TAG = "TestRxJava";

    public static void test() {
        test1();
    }

    /**
     * 测试基本使用 & Disposable的用法（切断事件流）& subscribe()几个重载函数的使用
     */
    private static void test1() {
        Observable observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                DLog.i(TAG, "emitter 1");
                emitter.onNext(1);

                DLog.i(TAG, "emitter 2");
                emitter.onNext(2);

                DLog.i(TAG, "emitter 3");
                emitter.onNext(3);

                /**
                 * 发送了onComplete()事件，后续的4在下游不能接收到了
                 */
                DLog.i(TAG, "emitter complete");
                emitter.onComplete();

                DLog.i(TAG, "emitter 4");
                emitter.onNext(4);
            }
        });

        Observer<Integer> observer = new Observer<Integer>() {

            private Disposable mDisposable;
            private int i;

            @Override
            public void onSubscribe(Disposable d) {
                mDisposable = d;
            }

            @Override
            public void onNext(Integer integer) {
                DLog.i(TAG, "onNext: " + integer);
                i++;
                if (i >= 2) {
                    /**
                     * 在收到两个事件后，切断事件流，3,4后续都不会收到了
                     */
                    DLog.i(TAG, "dispose");
                    mDisposable.dispose();
                    DLog.i(TAG, "isDisposed: " + mDisposable.isDisposed());
                }
            }

            @Override
            public void onError(Throwable e) {
                DLog.i(TAG, "error");
            }

            @Override
            public void onComplete() {
                DLog.i(TAG, "complete");
            }
        };

        Disposable disposable;

        /**
         * 1.普通用法使用Observer
         */
        observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observer);

        /**
         * 2.只关心onNext()事件的情况
         */
        disposable = observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer value) throws Exception {
                    DLog.i(TAG, "Consumer.accept: " + value);
                }
            });

        /**
         * 3.将事件单独分开指定的写法：onNext() onError() onComplete() onSubscribe()事件的情况
         */
        disposable = observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer value) throws Exception {
                    DLog.i(TAG, "Consumer.accept() & onNext(): " + value);
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    DLog.i(TAG, "Consumer.accept() & onError()", throwable);
                }
            }, new Action() {
                @Override
                public void run() throws Exception {
                    DLog.i(TAG, "Consumer.accept() & onComplete()");
                }
            }, new Consumer<Disposable>() {
                @Override
                public void accept(Disposable disposable) throws Exception {
                    DLog.i(TAG, "Consumer.accept() & onSubscribe(): " + disposable);
                }
            });


        /**
         * 可在其地方（线程）随时打断事件的传递
         */
        //disposable.dispose();
    }
}
