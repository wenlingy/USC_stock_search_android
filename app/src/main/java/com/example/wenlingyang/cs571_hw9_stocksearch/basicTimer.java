package com.example.wenlingyang.cs571_hw9_stocksearch;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wenlingyang on 11/24/17.
 */

public class basicTimer {
    // setTimeout, setInterval
    public interface TaskHandle {
        void invalidate();
    }

    public static TaskHandle setTimeout(final Runnable r, long delay) {
        final Handler h = new Handler();
        h.postDelayed(r, delay);
        return new TaskHandle() {
            @Override
            public void invalidate() {
                h.removeCallbacks(r);
            }
        };
    }

    public static TaskHandle setInterval(final Runnable r, long interval) {
        final Timer t = new Timer();
        final Handler h = new Handler();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                h.post(r);
            }
        }, interval, interval);
        return new TaskHandle() {
            @Override
            public void invalidate() {
                t.cancel();
                t.purge();
            }
        };
    }
}
