package meizu.rxjavademo;

import android.util.Log;

public class DLog {
    public static void i(String tag, String msg) {
        Log.i(tag, "[threadId:" + Thread.currentThread().getId() + "]:" + msg);
    }

    public static void i(String tag, String msg, Throwable tr) {
        Log.i(tag, "[threadId:" + Thread.currentThread().getId() + "]:" + msg, tr);
    }
}