package com.example.mouseracer.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.Stack;

/**
 * @author bobo
 * @date 2018/9/23
 */
public class MyApplication extends Application {

    private static MyApplication mApplication;

    public Stack<Activity> store;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        store = new Stack<>();
        //修改打印的tag值
        registerActivityLifecycleCallbacks(new SwitchBackgroundCallbacks());

    }

    public static MyApplication getInstance() {
        return mApplication;
    }

    private class SwitchBackgroundCallbacks implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            store.add(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            store.remove(activity);
        }
    }

    /**
     * 获取当前的Activity
     *
     * @return
     */
    public Activity getCurActivity() {
        return store.lastElement();
    }

}
