package meizu.rxjavademo.internal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import meizu.rxjavademo.DLog;
import meizu.rxjavademo.R;
import meizu.rxjavademo.Subscribes;

public class RetryWhenActivity extends AppCompatActivity {

    private static final String TAG = DLog.TAG;

    private Subscribes mSubscribes;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_retrywhen);

        mSubscribes = new Subscribes();

        findViewById(R.id.id_btn_retry_when).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryWhen();
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


    /**
     * retryWhen和repeatWhen最大的不同就是：retryWhen是收到onError后触发是否要重订阅的询问，
     * 而repeatWhen是通过onComplete触发。
     */
    private void retryWhen() {
        final int errorCount = 4;
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            private int retryIndex = 0;
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {

                /**
                 * 耗时操作
                 */
                doWork();

                /**
                 * 模拟请求的结果，前四次都返回失败，并将失败信息递交给retryWhen。
                 */
                if (retryIndex < errorCount) {
                    DLog.i(TAG, "retry index: " + retryIndex);
                    e.onError(new RetryException(retryIndex, "retry"));
                    retryIndex++;
                } else { //模拟请求成功的情况
                    e.onNext(100);
                    e.onComplete();
                }
            }
        }).retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
                return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Throwable throwable) throws Exception {
                        if (throwable instanceof RetryException) {
                            final int index = ((RetryException)throwable).getRetryIndex();
                            DLog.i(TAG, "retryWhen() of apply() index: " + index);
                            long waitTime = index * 2000;
                            return Observable.timer(waitTime, TimeUnit.MILLISECONDS);
                        } else {
                            return Observable.error(throwable);
                        }
                    }
                });
            }
        });
        mSubscribes.subscribe(observable);
    }

    private void doWork() {
        long workTime = (long) (Math.random() * 500) + 500;
        try {
            DLog.i(TAG, "doWork start()");
            Thread.sleep(workTime);
            DLog.i(TAG, "doWork finished()");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private class RetryException extends Throwable {
        private int retryIndex = 0;

        public RetryException(int retryIndex, String message) {
            super(message);
            this.retryIndex = retryIndex;
        }

        public int getRetryIndex() {
            return retryIndex;
        }
    }
}
