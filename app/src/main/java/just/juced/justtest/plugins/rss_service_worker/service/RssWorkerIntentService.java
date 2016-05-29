package just.juced.justtest.plugins.rss_service_worker.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.crazyhitty.chdev.ks.rssmanager.OnRssLoadListener;
import com.crazyhitty.chdev.ks.rssmanager.RssItem;
import com.crazyhitty.chdev.ks.rssmanager.RssReader;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import just.juced.justtest.fragments.CreateNewProviderFragment;
import just.juced.justtest.models.RssFeedItem;
import just.juced.justtest.models.RssProvider;
import just.juced.justtest.plugins.rss_service_worker.RssServiceWorkerConstants;

public class RssWorkerIntentService extends IntentService {

    public static final String CLASS_FULL_NAME = "just.juced.justtest.plugins.rss_service_worker.service.RssWorkerIntentService";

    public RssWorkerIntentService() {
        super("RssWorkerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("RssWorkerService", "onHandleIntent running...");

        try {
            String command = intent.getExtras().getString(RssServiceWorkerConstants.COMMAND, "");
            if (command.equals(RssServiceWorkerConstants.COMMAND_LOAD_ALL_RSS)) {
                computeLoadAllRssCommand(intent.getExtras().getString(RssServiceWorkerConstants.ARG_FORCE, "").equals(RssServiceWorkerConstants.ARG_TRUE));
            }
            if (command.equals(RssServiceWorkerConstants.COMMAND_LOAD_SINGLE_RSS)) {
                computeLoadSingleRssCommand(null, intent.getExtras().getString(RssServiceWorkerConstants.ARG_FORCE, "").equals(RssServiceWorkerConstants.ARG_TRUE), intent.getExtras().getString(RssServiceWorkerConstants.ARG_RSS_PROVIDER_ID, ""), null);
            }
        }
        catch (Exception ignored) {}
    }

    private void computeLoadSingleRssCommand(Realm realm, boolean forceChangeData, String providerId, String providerUrl) {
        if (realm == null) {
            realm = Realm.getDefaultInstance();
        }

        if (providerUrl == null) {
            RssProvider provider = realm.where(RssProvider.class).equalTo("id", providerId).findFirst();
            providerUrl = provider.getUrl();
        }

        List<RssItem> rssItems = new RssReader(getApplicationContext()).showDialog(false).urls(new String[]{providerUrl}).parseSync();
        workWithLoadedRssItems(realm, providerId, rssItems, forceChangeData);
    }

    private void computeLoadAllRssCommand(boolean forceChangeData) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<RssProvider> rssProviders = realm.where(RssProvider.class).findAll();
        for (int i = 0; i < rssProviders.size(); i++) {
            RssProvider provider = rssProviders.get(i);
            String providerId = provider.getId();

            try {
                computeLoadSingleRssCommand(realm, forceChangeData, providerId, provider.getUrl());
            }
            catch (Exception e) {
                Log.e("RssWorkerIntentService", "Unable to lod rss feeds from url: " + provider.getUrl());
                e.printStackTrace();
            }

        }

        realm.close();
    }

    private void workWithLoadedRssItems(Realm realm, String providerId, List<RssItem> downloadedItems, boolean forceChangeData) {
        //RssProvider rssProvider = realm.where(RssProvider.class).equalTo("id", providerId).findFirst();
        boolean hasChanges = false;
        RealmResults<RssFeedItem> rssFeedItems = realm.where(RssFeedItem.class).equalTo("providerId", providerId).findAll();

        if (downloadedItems.size() != rssFeedItems.size()) {
            hasChanges = true;
        }

        if (!hasChanges && !downloadedItems.isEmpty() && !rssFeedItems.isEmpty()) {
            RssItem firstDownloadedItem = downloadedItems.get(0);
            RssFeedItem firstExistsItem = rssFeedItems.get(0);

            if (!firstDownloadedItem.getLink().equals(firstExistsItem.getGuid())) {
                hasChanges = true;
            }
        }

        //hasChanges = true;

        if (hasChanges) {
            realm.beginTransaction();

            // remove old items
            rssFeedItems.deleteAllFromRealm();

            // add new items
            for (int i = 0; i < downloadedItems.size(); i++) {
                RssItem rssItem = downloadedItems.get(i);

                RssFeedItem rssFeedItem = new RssFeedItem(
                        rssItem.getLink(),
                        providerId,
                        rssItem.getTitle(),
                        rssItem.getLink(),
                        rssItem.getDescription(),
                        rssItem.getPubDate(),
                        rssItem.getImageUrl(),
                        rssItem.getCategory()
                );

                realm.copyToRealm(rssFeedItem);
            }

            realm.commitTransaction();
        }

        if (!hasChanges && forceChangeData) {
            realm.beginTransaction();
            rssFeedItems.get(0).setTitle(rssFeedItems.get(0).getTitle());
            realm.commitTransaction();
        }
    }

}
