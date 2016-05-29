package just.juced.justtest.application;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import just.juced.justtest.helpers.RealmHelper;
import just.juced.justtest.models.RssProvider;
import just.juced.justtest.plugins.rss_service_worker.RssServiceWorkerConstants;
import just.juced.justtest.plugins.rss_service_worker.receiver.RssWorkerAlarmReceiver;
import just.juced.justtest.plugins.rss_service_worker.service.RssWorkerIntentService;

/**
 * Created by juced on 08.11.2015.
 */
public class MyApplication extends Application {

    public static final String LOG_TAG = "JustTest";

    private static MyApplication singleton;

    public MyApplication getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        initializeRealm();

        if (isMyServiceRunning()) {
            fireIntentService();
        }
    }

    public static MyApplication getSingleton() {
        return singleton;
    }

    private void initializeRealm() {
        try {
            RealmConfiguration config = new RealmConfiguration.Builder(getApplicationContext())
                    .name(RealmHelper.DB_NAME)
                    .schemaVersion(RealmHelper. DB_VERSION)
                    .deleteRealmIfMigrationNeeded()
                    .build();

            Realm.setDefaultConfiguration(config);
        }
        catch (Exception e) {
            /*Log.e(LOG_TAG, "Unable to instantiate realm! Finishing application...");
            e.printStackTrace();

            android.os.Process.killProcess(android.os.Process.myPid());*/
        }
    }

    public void fireIntentService() {
        Intent intent = new Intent(getApplicationContext(), RssWorkerAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, RssServiceWorkerConstants.ALARM_RECEIVER_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, RssServiceWorkerConstants.ALARM_INTERVAL, pIntent);

        SharedPreferences spref = getSharedPreferences("TAG", MODE_PRIVATE);
        SharedPreferences.Editor editor = spref.edit();
        editor.putBoolean("alarm_set_flag", true);
        editor.apply();
    }

    public void cancelIntentServiceAlarm() {
        Intent intent = new Intent(getApplicationContext(), RssWorkerAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, RssServiceWorkerConstants.ALARM_RECEIVER_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);

        SharedPreferences spref = getSharedPreferences("TAG", MODE_PRIVATE);
        SharedPreferences.Editor editor = spref.edit();
        editor.putBoolean("alarm_set_flag", false);
        editor.apply();
    }

    public boolean isMyServiceRunning() {
        SharedPreferences spref = getSharedPreferences("TAG", MODE_PRIVATE);
        return spref.getBoolean("alarm_set_flag", true);
    }

    public boolean isServiceWorkerRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (RssWorkerIntentService.CLASS_FULL_NAME.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
