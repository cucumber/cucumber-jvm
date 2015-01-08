package cucumber.example.android.cukeulator.test;

import android.app.Activity;
import android.util.Log;
import android.view.View;

public final class Utils {
    private static final MultiLock lock = new MultiLock();

    public static class MultiLock {
        private int mLocks;

        public synchronized void acquire() throws InterruptedException {
            if (mLocks++ >= 0) {
                wait();
            }
        }

        public synchronized void release() {
            if (--mLocks <= 0) {
                notifyAll();
            }
        }
    }

    private Utils() {
    }

    public static void clickOnView(Activity activity, int id) {
        View view = activity.findViewById(id);
        if (view != null) clickOnView(activity, view);
    }

    public static void clickOnView(Activity activity, final View view) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.callOnClick();
                lock.release();
            }
        });
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            Log.e("cucumber-android", e.toString());
            Thread.currentThread().interrupt();
        }
    }
}
