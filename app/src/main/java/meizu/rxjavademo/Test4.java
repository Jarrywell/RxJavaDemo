package meizu.rxjavademo;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import android.util.Log;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * des:Flowable
 * author: libingyan
 * Date: 18-6-11 20:38
 */
public class Test4 {

    private static final String TAG = "TestRxJava";
    
    public static void test() {
        //test1();
        test2();
    }

    private static void test1() {
        Flowable<Integer> upstream = Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                DLog.i(TAG, "emit 1");
                emitter.onNext(1);

                DLog.i(TAG, "emit 2");
                emitter.onNext(2);

                DLog.i(TAG, "emit 3");
                emitter.onNext(3);

                DLog.i(TAG, "emit complete");
                emitter.onComplete();
            }
        }, BackpressureStrategy.ERROR);

        Subscriber<Integer> downstream = new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                DLog.i(TAG, "onSubscribe() s = " + s);

                /**
                 * 响应式拉去事件
                 */
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer integer) {
                DLog.i(TAG, "onNext() integer = " + integer);
            }

            @Override
            public void onError(Throwable t) {
                DLog.i(TAG, "onError() Throwable", t);
            }

            @Override
            public void onComplete() {
                DLog.i(TAG, "onComplete()");
            }
        };

        upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(downstream);
    }

    private static void test2() {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> emitter) throws Exception {
                for (int i = 0; i < 200; i++) {
                    emitter.onNext(i);
                    DLog.i(TAG, "emit : " + i);
                }

            }
        }, BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<Integer>() {
                @Override
                public void onSubscribe(Subscription s) {

                }

                @Override
                public void onNext(Integer integer) {
                    DLog.i(TAG, "onNext() : " + integer);
                }

                @Override
                public void onError(Throwable t) {
                    DLog.i(TAG, "onError()", t);
                }

                @Override
                public void onComplete() {

                }
            });
    }
}
