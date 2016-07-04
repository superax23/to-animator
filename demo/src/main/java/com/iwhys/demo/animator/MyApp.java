package com.iwhys.demo.animator;

import android.app.Application;

public class MyApp extends Application {

    /**
     * get the instance of the application.
     * @return instance
     */
    public static MyApp getInstance(){
        if (myApp == null){
            throw new IllegalArgumentException("");
        }
        return myApp;
    }

    // the app's instance
    private static MyApp myApp;

    @Override
    public void onCreate() {
        super.onCreate();
        if (myApp != null){
            return;
        }
        myApp = this;
    }

}
