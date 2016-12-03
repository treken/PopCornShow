package applicaton;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.onesignal.OneSignal;

import onsignal.CustomNotificationOpenedHandler;
import onsignal.CustomNotificationReceivedHandler;

/**
 * Created by icaro on 01/08/16.
 */

public class FilmeApplication extends Application {

    private static final String TAG = FilmeApplication.class.getName();
    private static FilmeApplication instance = null;
    private FirebaseAnalytics mFirebaseAnalytics;

    public static FilmeApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "FilmeApplication.onCreate");
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new CustomNotificationOpenedHandler())
                .setNotificationReceivedHandler(new CustomNotificationReceivedHandler())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .init();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}
