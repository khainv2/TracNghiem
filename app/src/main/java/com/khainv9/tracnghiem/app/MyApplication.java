package com.khainv9.tracnghiem.app;

import android.app.Application;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseManager.init(this);
    }
}
