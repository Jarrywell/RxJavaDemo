package meizu.rxjavademo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
    }


    /**
     * 简单调用测试
     */
    private void test() {
        //Test1.test();

        //Test2.test();

        Test3.test();

    }
}
