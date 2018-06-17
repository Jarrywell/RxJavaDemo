package meizu.rxjavademo.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import meizu.rxjavademo.DLog;
import meizu.rxjavademo.R;
import meizu.rxjavademo.Subscribes;


/**
 * 测试需要校验token过期的情况
 */
public class TokenActivity extends AppCompatActivity {
    private static final String TAG = DLog.TAG;

    private static final int CODE_TOKEN_FAILED = 100;
    private SharedPreferences mPreferences;
    private Subscribes mSubscribes;
    private TokenLoader mTokenLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_token);
        mPreferences = getSharedPreferences("sp-token", Context.MODE_PRIVATE);

        mSubscribes = new Subscribes();
        mTokenLoader = new TokenLoader();
        findViewById(R.id.id_btn_network1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request1();
            }
        });

        findViewById(R.id.id_btn_network2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request2();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSubscribes != null) {
            mSubscribes.release();
        }
    }


    private Observable<String> requestUserInfo(final int index, final long token) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                DLog.i(TAG, "start request userInfo of token: " + token);
                if (token > 0 && System.currentTimeMillis() - token < 2000) {
                    e.onNext("this is " + index + " userinfo!!!");
                    e.onComplete();
                } else {
                    e.onError(new HttpException(CODE_TOKEN_FAILED, "failed of token!!"));
                }
            }
        });
    }


    private void request1() {
        request(1);
    }

    private void request2() {
        request(2);
    }

    private void request(final int index) {
        /**
         * defer() 当观察者订阅时，才创建Observable，并且针对每个观察者创建都是一个新的Observable
         */
        Observable<String> observable = Observable.defer(new Callable<ObservableSource<Long>>() {
            @Override
            public ObservableSource<Long> call() throws Exception {
                final long token = getToken();
                DLog.i(TAG, "get chached token: " + token);
                return Observable.just(token);
            }
        }).flatMap(new Function<Long, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Long token) throws Exception {

                return requestUserInfo(index, token);
            }
        }).retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
            private int retryCount = 0;
            private static final int RETRY_MAX_COUNT = 4;

            @Override
            public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
                return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Throwable throwable) throws Exception {
                        DLog.i(TAG, index + " - throwable： " + throwable + ", retry count: " + retryCount);
                        if (throwable instanceof HttpException) {
                            final int code = ((HttpException) throwable).getcode();
                            if (retryCount > RETRY_MAX_COUNT) { //大于最大重试次数，则直接抛异常抛给下游
                                return Observable.error(throwable);
                            } else if (code == CODE_TOKEN_FAILED) { //小于最大重试次数

                                retryCount++;

                                /**
                                 *
                                 */
                                return mTokenLoader.getTokenObservableLocked();

                            } else {

                                return Observable.error(throwable);
                            }
                        } else {
                            return Observable.error(throwable);
                        }
                    }
                });
            }
        });

        mSubscribes.subscribe(observable);
    }



    private void saveToken(long time) {
        mPreferences.edit().putLong("token", time).apply();
    }

    private long getToken() {
        return mPreferences.getLong("token", 0);
    }


    private class TokenLoader {

        private AtomicBoolean mRefreshing = new AtomicBoolean(false);
        private PublishSubject<Long> mTokenSubject;
        private Observable<Long> mTokenObservable;

        public TokenLoader() {
            mTokenSubject = PublishSubject.create();

            mTokenObservable = Observable.create(new ObservableOnSubscribe<Long>() {
                @Override
                public void subscribe(ObservableEmitter<Long> e) throws Exception {
                    DLog.i(TAG, "start request token!!!");

                    //模拟网络请求耗时
                    Thread.sleep(1000);

                    final long token = System.currentTimeMillis();

                    /**
                     * 模拟返回错误的token
                     */
                    //final long token = 0;

                    e.onNext(token);
                }
            }).doOnNext(new Consumer<Long>() {
                @Override
                public void accept(Long token) throws Exception {

                    DLog.i(TAG, "save token to file!!!");
                    saveToken(token);

                    //更新开关
                    mRefreshing.set(false);
                }
            }).doOnError(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {

                    //更新开关
                    mRefreshing.set(false);
                }
            }).subscribeOn(Schedulers.io());
        }

        /**
         * 同步读取token
         * @return
         */
        public Observable<Long> getTokenObservableLocked() {
            if (mRefreshing.compareAndSet(false, true)) {
                DLog.i(TAG, "current has not token request, then start request token!!!!");

                requestToken();

            } else {
                DLog.i(TAG, "has token request, then wait!!!");
            }

            return mTokenSubject;
        }


        /**
         * 发起token请求
         */
        private void requestToken() {
            mTokenObservable.subscribe(mTokenSubject);
        }
    }

    private class HttpException extends Throwable {
        private int code;

        public HttpException(int code, String message) {
            super(message);
            this.code = code;
        }

        public int getcode() {
            return code;
        }
    }
}
