package meizu.rxjavademo.internal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import meizu.rxjavademo.DLog;
import meizu.rxjavademo.R;
import meizu.rxjavademo.Subscribes;


/**
 * 缓存处理
 */
public class CacheActivity extends AppCompatActivity {
    private static final String TAG = DLog.TAG;

    private Subscribes mSubscribes;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_cache);

        mSubscribes = new Subscribes();
        findViewById(R.id.id_btn_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contact();
            }
        });

        findViewById(R.id.id_btn_concatEager).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                concatEager();
            }
        });

        findViewById(R.id.id_btn_merge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                merge();
                //mergeEx();
            }
        });

        findViewById(R.id.id_btn_publish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish();
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
     * 它会连接多个Observable，并且必须要等到前一个Observable的所有数据项都发送完之后，
     * 才会开始下一个Observable数据的发送。
     *
     * 白白浪费了前面读取缓存的这段时间，能不能同时发起读取缓存和网络的请求，
     * 而不是等到读取缓存完毕之后，才去请求网络呢？
     *
     * 此处耗时5000 + 1000 = 6000;
     */
    private void contact() {
        Observable<List<ResultEntity>> observable =
                Observable.concat(getCacheEntitiys(1000).subscribeOn(Schedulers.io()),
                getNetworkEntitiys(5000).subscribeOn(Schedulers.io()));

        mSubscribes.subscribe(observable, getObserver());
    }

    /**
     * 它和concat最大的不同就是多个Observable可以同时开始发射数据，如果后一个Observable发射完成后，
     * 前一个Observable还有发射完数据，那么它会将后一个Observable的数据先缓存起来，
     * 等到前一个Observable发射完毕后，才将缓存的数据发射出去。
     *
     * 如果读取缓存的时间要大于网络请求的时间，那么就会导致出现“网络请求的结果”等待“读取缓存”这一过程完成
     * 后才能传递给下游，白白浪费了一段时间。
     *
     * 此处耗时max(5000, 1000) = 5000;
     */
    private void concatEager() {
        List<Observable<List<ResultEntity>>> observables = new ArrayList<>();
        observables.add(getCacheEntitiys(5000).subscribeOn(Schedulers.io()));
        observables.add(getNetworkEntitiys(1000).subscribeOn(Schedulers.io()));

        mSubscribes.subscribe(Observable.concatEager(observables), getObserver());

    }


    /**
     * 它和concatEager一样，会让多个Observable同时开始发射数据，但是它不需要Observable之间的互相等待，
     * 而是直接发送给下游。
     *
     * 在读取缓存的时间小于请求网络的时间时，这个操作符能够很好的工作，但是反之，就会出现我们先展示了网络的数据，
     * 然后又被刷新成旧的缓存数据。
     *
     * 此处耗时max(5000, 1000) = 5000;
     */
    private void merge() {
        Observable<List<ResultEntity>> observable =
                Observable.merge(getCacheEntitiys(5000).subscribeOn(Schedulers.io()),
                getNetworkEntitiys(1000).subscribeOn(Schedulers.io()));

        mSubscribes.subscribe(observable, getObserver());
    }



    /**
     * 在读取缓存的时间大于请求网络时间的时候，仅仅只会展示网络的数据.
     * 读取缓存和请求网络是同时发起的，很好地解决了前面几种实现方式的缺陷
     *
     * publish操作符，它接收一个Function函数，该函数返回一个Observable，该Observable是对原Observable，
     * 也就是上面网络源的Observable转换之后的结果，该Observable可以被takeUntil和merge操作符所共享，
     * 从而实现只订阅一次的效果。
     *
     * 官方说明：
     * publish() -> 这个函数用原始Observable发射的数据作为参数，产生一个新的数据作为ConnectableObservable给发射，
     * 替换原位置的数据项。实质是在签名的基础上添加一个Map操作。
     *
     */
    private void publish() {
        final Observable<List<ResultEntity>> cacheObservable =
                getCacheEntitiys(5000).subscribeOn(Schedulers.io());

        final Observable<List<ResultEntity>> networkObservable =
                getNetworkEntitiys(1000).subscribeOn(Schedulers.io());

        Observable<List<ResultEntity>> result = networkObservable.publish(
                new Function<Observable<List<ResultEntity>>, ObservableSource<List<ResultEntity>>>() {
            @Override
            public ObservableSource<List<ResultEntity>> apply(Observable<List<ResultEntity>> network)
                    throws Exception {
                /**
                 * takeUntil()传入了另一个otherObservable，它表示sourceObservable在otherObservable发射数据之后，
                 * 就不允许再发射数据了，这就刚好满足了我们前面说的“只要网络源发送了数据，那么缓存源就不应再发射数据”
                 *
                 * merge()解释同上
                 */
                return Observable.merge(network, cacheObservable.takeUntil(network));
            }
        });
        mSubscribes.subscribe(result, getObserver());
    }

    /**
     * 同下publish(),但是最终会发起两次网络请求
     * merge + takeUntil
     */
    private void mergeEx() {

        final Observable<List<ResultEntity>> cacheObservable =
                getCacheEntitiys(1000).subscribeOn(Schedulers.io());

        final Observable<List<ResultEntity>> networkObservable =
                getNetworkEntitiys(5000).subscribeOn(Schedulers.io());

        Observable<List<ResultEntity>> observable =
                Observable.merge(networkObservable, cacheObservable.takeUntil(networkObservable));

        mSubscribes.subscribe(observable, getObserver());
    }

    private DisposableObserver<List<ResultEntity>> getObserver() {
        return new DisposableObserver<List<ResultEntity>>() {
            @Override
            public void onNext(List<ResultEntity> value) {
                for (ResultEntity entity : value) {
                    DLog.i(TAG, "onNext(): " + entity.getId() + ", " + entity.getTitle() + ", " + entity.getContent());
                }
            }

            @Override
            public void onError(Throwable e) {
                DLog.i(TAG, "onError", e);
            }

            @Override
            public void onComplete() {
                DLog.i(TAG,"onComplete()");
            }
        };
    }


    public Observable<List<ResultEntity>> getCacheEntitiys(final long simulateTime) {
        return getResultEntitys(simulateTime, "缓存");
    }


    public Observable<List<ResultEntity>> getNetworkEntitiys(final long simulateTime) {
        return getResultEntitys(simulateTime, "网络");
    }


    private Observable<List<ResultEntity>> getResultEntitys(final long simulateTime, final String title) {
        return Observable.create(new ObservableOnSubscribe<List<ResultEntity>>() {
            @Override
            public void subscribe(ObservableEmitter<List<ResultEntity>> e) throws Exception {
                DLog.i(TAG, "start load " + title);
                try {
                    Thread.sleep(simulateTime);
                } catch (InterruptedException throwable) {
                    if (!e.isDisposed()) {
                        e.onError(throwable);
                    }
                }
                List<ResultEntity> result = new ArrayList<>(10);
                for (int i = 0 ; i < 10; i++) {
                    ResultEntity entity = new ResultEntity();
                    entity.setId(i);
                    entity.setTitle(title);
                    entity.setContent("content " + i);
                    result.add(entity);
                }
                DLog.i(TAG, "end load " + title);
                e.onNext(result);
                e.onComplete();
                /*if (!title.equals("网络")) {
                    e.onNext(result);
                    e.onComplete();
                } else {
                    e.onError(new Throwable());
                }*/
            }
        });
    }

    private class ResultEntity {
        private int id;
        private String title;
        private String content;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
