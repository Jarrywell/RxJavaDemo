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
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import meizu.rxjavademo.DLog;

/**
 * des:
 * author: libingyan
 * Date: 18-6-11 09:45
 */
public class Test3 {
    private static final String TAG = "TestRxJava";


    public static void test() {
        test1();
        //test2();
        //test3();
        //test4();
        //test5();
    }


    /**
     * 线程切换: https://www.jianshu.com/p/8818b98c44e2
     * 06-11 09:58:04.204 I/TestRxJava( 9452): [threadId:348]:subscribe() -->[RxNewThreadScheduler-1]
     * 06-11 09:58:04.237 I/TestRxJava( 9452): [threadId:1]:After observeOn(mainThread), current thread is: main -->[main]
     * 06-11 09:58:04.238 I/TestRxJava( 9452): [threadId:350]:After observeOn(io), current thread is : RxCachedThreadScheduler-2 -->[RxCachedThreadScheduler-2]
     * 06-11 09:58:04.238 I/TestRxJava( 9452): [threadId:350]:last obverser accept: 100 -->[RxCachedThreadScheduler-2]
     */
    private static void test1() {
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                DLog.i(TAG, "subscribe()");
                e.onNext("100");
                e.onComplete();
            }
        });

        observable.subscribeOn(Schedulers.newThread()) //第一次上游线程
            .subscribeOn(Schedulers.io()) //第二次上游线程
            .observeOn(AndroidSchedulers.mainThread()) //第一次下游线程
            .doOnNext(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                DLog.i(TAG, "After observeOn(mainThread), current thread is: " + Thread.currentThread().getName());
            }
        }).observeOn(Schedulers.io())  //第二次下游线程
            .doOnNext(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                DLog.i(TAG, "After observeOn(io), current thread is : " + Thread.currentThread().getName());
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                DLog.i(TAG, "last obverser accept: " + s);
            }
        });
    }


    /**
     * https://www.jianshu.com/p/128e662906af
     * flatMap & contactMap
     */
    private static void test2() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).concatMap(new Function<Integer, ObservableSource<String>>() { //注意：flatMap不能保证顺序一致
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                final List<String> result = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    result.add("this is value: " + integer);
                }

                return Observable.fromIterable(result).delay(10, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                DLog.i(TAG, "concatMap value: " + s);
            }
        });
    }


    /**
     * https://www.jianshu.com/p/bb58571cdb64
     * zip
     *
     *
     * 06-11 10:28:02.234 I/TestRxJava(11185): [threadId:1]:emit 1 -->[main]
     * 06-11 10:28:02.235 I/TestRxJava(11185): [threadId:1]:emit 2 -->[main]
     * 06-11 10:28:02.235 I/TestRxJava(11185): [threadId:1]:emit 3 -->[main]
     * 06-11 10:28:02.235 I/TestRxJava(11185): [threadId:1]:emit 4 -->[main]
     * 06-11 10:28:02.235 I/TestRxJava(11185): [threadId:1]:emit onComplete1 -->[main]
     * 06-11 10:28:02.235 I/TestRxJava(11185): [threadId:1]:emit A -->[main]
     * 06-11 10:28:02.235 I/TestRxJava(11185): [threadId:1]:onNext: 1A -->[main]
     * 06-11 10:28:02.235 I/TestRxJava(11185): [threadId:1]:emit B -->[main]
     * 06-11 10:28:02.235 I/TestRxJava(11185): [threadId:1]:onNext: 2B -->[main]
     * 06-11 10:28:02.235 I/TestRxJava(11185): [threadId:1]:emit C -->[main]
     * 06-11 10:28:02.235 I/TestRxJava(11185): [threadId:1]:onNext: 3C -->[main]
     * 06-11 10:28:02.235 I/TestRxJava(11185): [threadId:1]:emit onComplete2 -->[main]
     */
    private static void test3() {
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                DLog.i(TAG, "emit 1");
                e.onNext(1);
                DLog.i(TAG, "emit 2");
                e.onNext(2);
                DLog.i(TAG, "emit 3");
                e.onNext(3);
                DLog.i(TAG, "emit 4");
                e.onNext(4);

                DLog.i(TAG, "emit onComplete1");
                e.onComplete();

            }
        });

        Observable observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                DLog.i(TAG, "emit A");
                e.onNext("A");

                DLog.i(TAG, "emit B");
                e.onNext("B");

                DLog.i(TAG, "emit C");
                e.onNext("C");

                DLog.i(TAG, "emit onComplete2");
                e.onComplete();
            }
        });

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).subscribe(new Observer() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object value) {
                DLog.i(TAG, "onNext: " + value);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }


    /**
     * 006-11 20:45:54.122 I/TestRxJava(12927): [threadId:907]:emit 1 -->[RxCachedThreadScheduler-1]
     * 06-11 20:45:54.123 I/TestRxJava(12927): [threadId:908]:emit A -->[RxCachedThreadScheduler-2]
     * 06-11 20:45:54.161 I/TestRxJava(12927): [threadId:1]:onNext: 1A -->[main]
     * 06-11 20:45:55.123 I/TestRxJava(12927): [threadId:907]:emit 2 -->[RxCachedThreadScheduler-1]
     * 06-11 20:45:55.124 I/TestRxJava(12927): [threadId:908]:emit B -->[RxCachedThreadScheduler-2]
     * 06-11 20:45:55.124 I/TestRxJava(12927): [threadId:1]:onNext: 2B -->[main]
     * 06-11 20:45:56.124 I/TestRxJava(12927): [threadId:907]:emit 3 -->[RxCachedThreadScheduler-1]
     * 06-11 20:45:56.124 I/TestRxJava(12927): [threadId:908]:emit C -->[RxCachedThreadScheduler-2]
     * 06-11 20:45:56.125 I/TestRxJava(12927): [threadId:1]:onNext: 3C -->[main]
     * 06-11 20:45:57.125 I/TestRxJava(12927): [threadId:907]:emit 4 -->[RxCachedThreadScheduler-1]
     * 06-11 20:45:57.126 I/TestRxJava(12927): [threadId:907]:emit onComplete1 -->[RxCachedThreadScheduler-1]
     * 06-11 20:45:57.131 I/TestRxJava(12927): [threadId:908]:emit D -->[RxCachedThreadScheduler-2]
     * 06-11 20:45:57.134 I/TestRxJava(12927): [threadId:1]:onNext: 4D -->[main]
     * 06-11 20:45:57.150 I/TestRxJava(12927): [threadId:908]:emit onComplete2 -->[RxCachedThreadScheduler-2]
     */
    private static void test4() {
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                DLog.i(TAG, "emit 1");
                e.onNext(1);
                Thread.sleep(1000);

                DLog.i(TAG, "emit 2");
                e.onNext(2);
                Thread.sleep(1000);

                DLog.i(TAG, "emit 3");
                e.onNext(3);
                Thread.sleep(1000);

                DLog.i(TAG, "emit 4");
                e.onNext(4);


                DLog.i(TAG, "emit onComplete1");
                e.onComplete();

            }
        }).subscribeOn(Schedulers.io());

        Observable observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                DLog.i(TAG, "emit A");
                e.onNext("A");
                Thread.sleep(1000);

                DLog.i(TAG, "emit B");
                e.onNext("B");
                Thread.sleep(1000);

                DLog.i(TAG, "emit C");
                e.onNext("C");
                Thread.sleep(1000);

                DLog.i(TAG, "emit D");
                e.onNext("D");

                DLog.i(TAG, "emit onComplete2");
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io());

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object value) {
                try {
                    DLog.i(TAG, "onNext: " + value);
                } catch (Exception e) {
                    DLog.i(TAG, "onNext() error", e);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }



    private static void test5() {
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                for (int i = 0; ; i++) {   //无限循环发事件
                    emitter.onNext(i);
                }
            }
        }).subscribeOn(Schedulers.io());

        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                emitter.onNext("A");
            }
        }).subscribeOn(Schedulers.io());

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                DLog.i(TAG, s);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                DLog.i(TAG, "", throwable);
            }
        });

    }

}
