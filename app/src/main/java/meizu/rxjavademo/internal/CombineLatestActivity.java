package meizu.rxjavademo.internal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.PublishSubject;
import meizu.rxjavademo.DLog;
import meizu.rxjavademo.R;
import meizu.rxjavademo.Subscribes;

/**
 * combineLatest
 */
public class CombineLatestActivity extends AppCompatActivity {
    private static final String TAG = DLog.TAG;

    private Subscribes mSubscribes;
    private PublishSubject<String> mNameSubject;
    private PublishSubject<String> mPasswordSubject;

    private Button mBtnLogin;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_combinelatest);

        mSubscribes = new Subscribes();
        mNameSubject = PublishSubject.create();
        mPasswordSubject = PublishSubject.create();

        EditText userName = findViewById(R.id.id_edt_name);
        userName.addTextChangedListener(new MyTextWatcher(mNameSubject));

        EditText password = findViewById(R.id.id_edt_password);
        password.addTextChangedListener(new MyTextWatcher(mPasswordSubject));

        mBtnLogin = findViewById(R.id.id_btn_login);


        Observable<Boolean> observable = Observable.combineLatest(mNameSubject, mPasswordSubject, new BiFunction<String, String, Boolean>() {
            @Override
            public Boolean apply(String name, String password) throws Exception {
                final int nameLength = name.length();
                final int passwordLength = password.length();
                DLog.i(TAG, "name length: " + nameLength + ", password length: " + passwordLength);
                return nameLength >= 3 && passwordLength >= 8;
            }
        });

        mSubscribes.subscribe(observable, new DisposableObserver<Boolean>() {
            @Override
            public void onNext(Boolean value) {
                mBtnLogin.setText(value ? "登录" : "用户名或密码无效");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        /**
         * combineLatest()需要每个observable都至少发射过一次数据才会触发，
         * 因此可以在最开始的时候手动发射空数据
         */
        mSubscribes.subscribe(Observable.timer(100, TimeUnit.MILLISECONDS),
                new DisposableObserver<Long>() {
            @Override
            public void onNext(Long value) {
                DLog.i(TAG, "send empty content");
                mNameSubject.onNext("");
                mPasswordSubject.onNext("");
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

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


    private class MyTextWatcher implements TextWatcher {
        private final PublishSubject<String> publishSubject;

        public MyTextWatcher(PublishSubject<String> publishSubject) {
            this.publishSubject = publishSubject;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            publishSubject.onNext(s.toString());
        }
    }
}
