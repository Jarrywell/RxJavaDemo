package meizu.rxjavademo.internal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import meizu.rxjavademo.DLog;
import meizu.rxjavademo.R;
import meizu.rxjavademo.Subscribes;


/**
 * 测试intervalRange() & repeatWhen()
 */
public class IntervalActivity extends AppCompatActivity {
    private static final String TAG = DLog.TAG;

    private Subscribes mSubscribes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_interval);

        mSubscribes = new Subscribes();
        findViewById(R.id.id_btn_interval_range).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intervalRange();
            }
        });

        findViewById(R.id.id_btn_repeat_when).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeatWhen();
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

    private void intervalRange() {
        DLog.i(TAG, "start intervalRange()");
        mSubscribes.subscribe(Observable.intervalRange(0, 10, 0, 3, TimeUnit.SECONDS)
                .take(5) //取前5个
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        doWork();
                    }
                }));
    }

    private void repeatWhen() {
        mSubscribes.subscribe(Observable.just(0L)
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        DLog.i(TAG, "doOnComplete()");
                        doWork();
                    }
                }).repeatWhen(new Function<Observable<Object>, ObservableSource<Long>>() {
                    private long repeatCount = 0;

                    @Override
                    public ObservableSource<Long> apply(Observable<Object> objectObservable) throws Exception {
                        return objectObservable.flatMap(new Function<Object, ObservableSource<Long>>() {

                            @Override
                            public ObservableSource<Long> apply(Object o) throws Exception {
                                if (++repeatCount < 5) {
                                    DLog.i(TAG, "repeatWhen of function apply() continue!!");
                                    return Observable.timer(3000 + repeatCount * 1000, TimeUnit.MILLISECONDS);
                                } else {
                                    DLog.i(TAG, "repeatWhen of function apply() complete!!");
                                    return Observable.empty(); //发送onComplete消息，无法触发下游的onComplete回调。
                                    //return Observable.error(new Throwable("Polling work finished"));
                                }
                            }
                        });
                    }
                }));
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
}
