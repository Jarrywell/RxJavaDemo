package meizu.rxjavademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "TestRxJava";

    private Button mBtnTest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnTest = findViewById(R.id.id_btn_test);
        mBtnTest.setOnClickListener(this);

        Test1.test();
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnTest) {
            Test1.test();
        }
    }
}
