package com.example.HealthyUpdate;

import android.os.Handler;

public class HealthyUpdateThread {


        public static void runInThread(Runnable task) {
            new Thread(task).start();
        }


        public  static Handler mhandler = new Handler();
        public static void runInUIThread(Runnable task) {
            mhandler.post(task);
        }



}
