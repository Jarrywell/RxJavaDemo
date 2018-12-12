package meizu.rxjavademo.test;



import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import meizu.rxjavademo.DLog;

/**
 * https://www.jianshu.com/p/8ee8fe70aacc
 */
public class Test1 {
    private static final String TAG = "TestRxJava";


    public static void test() {
        //test1();

        //test2();

        //test3();
    }

    /**
     * 基本使用
     */
    private static void test1() {

        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                DLog.i(TAG, "onSubscribe(): " + d);

            }

            @Override
            public void onNext(String value) {
                DLog.i(TAG, "onNext(): " + value);
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

        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                DLog.i(TAG, "subscribe()");
                e.onNext("100");
                e.onComplete();
            }
        });

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }


    /**
     * map
     */
    private static void test2() {

        Observer<String> observer = new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                DLog.i(TAG, "onSubscribe(): " + d);
            }

            @Override
            public void onNext(String value) {
                DLog.i(TAG, "onNext(): " + value);
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


        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                DLog.i(TAG, "subscribe()");
                e.onNext(100);
                e.onComplete();
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                DLog.i(TAG, "map.apply()");
                return "numble is " + integer;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

    }

    /**
     * flatMap
     */
    private static void test3() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onNext(4);
                e.onNext(5);
                e.onComplete();
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("this is value: " + integer);
                }
                int delayTime = (int) (1 + Math.random() * 10);
                return Observable.fromIterable(list).delay(delayTime, TimeUnit.MILLISECONDS);
                //return Observable.fromIterable(list);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String content) throws Exception {
                        DLog.i(TAG, "accept() content: " + content);
                    }
                });
    }
}
