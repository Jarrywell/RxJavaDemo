package meizu.rxjavademo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import meizu.rxjavademo.internal.CacheActivity;
import meizu.rxjavademo.internal.CombineLatestActivity;
import meizu.rxjavademo.internal.IntervalActivity;
import meizu.rxjavademo.internal.RetryWhenActivity;
import meizu.rxjavademo.internal.TokenActivity;
import meizu.rxjavademo.test.Test3;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TestRxJava";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.id_btn_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });

        findViewById(R.id.id_btn_interval).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), IntervalActivity.class));
            }
        });

        findViewById(R.id.id_btn_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RetryWhenActivity.class));
            }
        });

        findViewById(R.id.id_btn_combinelatest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CombineLatestActivity.class));
            }
        });

        findViewById(R.id.id_btn_cache).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CacheActivity.class));
            }
        });

        findViewById(R.id.id_btn_token).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), TokenActivity.class));
            }
        });

    }




    private void test() {
        //Test1.test();

        //Test2.test();

        //Test3.test();

        //Test4.test();

        Test5.test();
    }
}
