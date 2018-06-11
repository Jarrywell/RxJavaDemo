package meizu.rxjavademo;


import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * https://www.jianshu.com/p/2ddd9bb8b1d7
 */
public class Test2 {
    private static final String TAG = "TestRxJava";

    public static void test() {
        //test1();

        //test2();

        //test3();

        test4();
    }

    /**
     * just简单示例
     */
    private static void test1() {
        Observable.just("Cricket", "Football")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                DLog.i(TAG, "onSubscribe() Disposable: " + d);
            }

            @Override
            public void onNext(String value) {
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
        });
    }

    /**
     * map
     */
    private static void test2() {
        Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> e) throws Exception {
                List<String> result = new ArrayList<>();
                result.add("101");
                result.add("102");
                result.add("103");
                result.add("104");
                result.add("105");
                DLog.i(TAG, "subscribe(), size: " + result.size());
                e.onNext(result);
                e.onComplete();
            }
        }).map(new Function<List<String>, List<Integer>>() {
            @Override
            public List<Integer> apply(List<String> strings) throws Exception {
                List<Integer> result = null;
                if (strings != null) {
                    result = new ArrayList<>(strings.size());
                    for (String content : strings) {
                        result.add(Integer.valueOf(content));
                    }
                }
                return result;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Integer>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        DLog.i(TAG, "onSubscribe() Disposable: " + d);
                    }

                    @Override
                    public void onNext(List<Integer> value) {
                        for (int v : value) {
                            DLog.i(TAG, "onNext() value: " + v);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        DLog.i(TAG, "onComplete()");
                    }
                });
    }


    /**
     * zip
     */
    private static void test3() {
        Observable<List<String>> observableOfString = Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> e) throws Exception {
                List<String> result = new ArrayList<>();
                result.add("li1");
                result.add("li2");
                result.add("li3");
                result.add("li4");
                result.add("li5");
                DLog.i(TAG, "string subscribe(), size: " + result.size());
                e.onNext(result);
                e.onComplete();
            }
        });

        Observable<List<Integer>> observableOfInteger = Observable.create(new ObservableOnSubscribe<List<Integer>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Integer>> e) throws Exception {
                List<Integer> result = new ArrayList<>();
                result.add(20);
                result.add(31);
                result.add(42);
                result.add(53);
                result.add(55);

                DLog.i(TAG, "int subscribe(), size: " + result.size());
                e.onNext(result);
                e.onComplete();
            }
        });

        Observable.zip(observableOfString, observableOfInteger,
                new BiFunction<List<String>, List<Integer>, List<TestStruct1>>() {
            @Override
            public List<TestStruct1> apply(List<String> strings, List<Integer> integers) throws Exception {
                List<TestStruct1> result = new ArrayList<>();
                final int size = Math.min(strings.size(), integers.size());
                for (int i = 0; i < size; i++) {
                    result.add(new TestStruct1(strings.get(i), integers.get(i)));
                }
                return result;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<TestStruct1>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<TestStruct1> value) {
                for (TestStruct1 v : value) {
                    DLog.i(TAG, "onNext() name: " + v.name + ", age: " + v.age);
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

    private static class TestStruct1 {

        private String name;
        private int age;

        public TestStruct1(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }


    /**
     * CompositeDisposable管理订阅，用于防止内存泄漏
     * http://www.jcodecraeer.com/plus/view.php?aid=7957
     */
    private static void test4() {
        //CompositeDisposable disposables = new CompositeDisposable();

    }
}
