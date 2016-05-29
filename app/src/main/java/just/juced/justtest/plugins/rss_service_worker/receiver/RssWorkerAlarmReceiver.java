package just.juced.justtest.plugins.rss_service_worker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import just.juced.justtest.plugins.rss_service_worker.RssServiceWorkerConstants;
import just.juced.justtest.plugins.rss_service_worker.service.RssWorkerIntentService;

public class RssWorkerAlarmReceiver extends BroadcastReceiver {

    public RssWorkerAlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, RssWorkerIntentService.class);
        i.putExtra(RssServiceWorkerConstants.COMMAND, RssServiceWorkerConstants.COMMAND_LOAD_ALL_RSS);
        context.startService(i);
    }
}
