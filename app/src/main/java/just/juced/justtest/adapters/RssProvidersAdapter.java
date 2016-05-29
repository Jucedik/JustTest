package just.juced.justtest.adapters;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmResults;
import just.juced.justtest.R;
import just.juced.justtest.application.MyApplication;
import just.juced.justtest.helpers.AnimationHelper;
import just.juced.justtest.models.RssFeedItem;
import just.juced.justtest.models.RssProvider;

/**
 * Created by juced on 27.05.2016.
 */
public class RssProvidersAdapter extends RecyclerView.Adapter<RssProvidersAdapter.ViewHolder> {

    public static final int TYPE_LOADER = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_EMPTY = 2;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public IMyViewHolderClicks mListener;
        public interface IMyViewHolderClicks {
            void removeProvider(String providerId);
            void showRssFeed(String providerId);
        }

        @Override
        public void onClick(View v) {
            String tag = v.getTag().toString();
            if (tag.equals(MyApplication.getSingleton().getApplicationContext().getString(R.string.tag_clickableItem))) {
                mListener.showRssFeed(rssProviderId);
            }
            else if (tag.equals(MyApplication.getSingleton().getApplicationContext().getString(R.string.tag_remove))) {
                mListener.removeProvider(rssProviderId);
            }
        }

        String rssProviderId;

        LinearLayout block_clickableContent;
        ImageView img_photo;
        TextView text_name;
        TextView text_description;
        ImageButton btn_delete;

        public ViewHolder(View v, int viewType, IMyViewHolderClicks listener) {
            super(v);
            this.mListener = listener;

            if (viewType == TYPE_ITEM) {
                block_clickableContent = (LinearLayout) v.findViewById(R.id.block_clickableContent);
                img_photo = (ImageView) v.findViewById(R.id.img_photo);
                text_name = (TextView) v.findViewById(R.id.text_name);
                text_description = (TextView) v.findViewById(R.id.text_description);
                btn_delete = (ImageButton) v.findViewById(R.id.btn_delete);

                block_clickableContent.setOnClickListener(this);
                btn_delete.setOnClickListener(this);
            }
        }
    }

    public interface RssProvidersAdapterInteractionListener {
        void onRssProviderClicked(String providerId);
        void onRssProviderRemoved(String providerId);
    }

    private RssProvidersAdapterInteractionListener interactionListener;
    private RealmResults<RssProvider> rssProviders;
    private boolean dataLoaded = false;
    private Realm realm;

    private DisplayImageOptions options;
    private ImageLoader imageLoader;

    public RssProvidersAdapter(RealmResults<RssProvider> rssProviders, Realm realm, RssProvidersAdapterInteractionListener interactionListener) {
        this.rssProviders = rssProviders;
        this.realm = realm;
        this.interactionListener = interactionListener;

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(MyApplication.getSingleton().getApplicationContext()).build();
        ImageLoader.getInstance().init(config);
        imageLoader = ImageLoader.getInstance();
    }

    public int getItemCount() {
        if (!dataLoaded) {
            return 1;
        }
        else {
            if (rssProviders.isEmpty()) {
                return 1;
            }
            else {
                return rssProviders.size();
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh = null;
        ViewHolder.IMyViewHolderClicks listener = new ViewHolder.IMyViewHolderClicks() {
            @Override
            public void removeProvider(final String providerId) {
                // remove rss provider
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm bgRealm) {
                        RssProvider rssProvider = bgRealm.where(RssProvider.class).equalTo("id", providerId).findFirst();
                        if (rssProvider != null) {
                            interactionListener.onRssProviderRemoved(providerId);
                            rssProvider.deleteFromRealm();
                        }
                    }
                });

                // remove rss feeds from this provider
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm bgRealm) {
                        try {
                            bgRealm.where(RssFeedItem.class).equalTo("providerId", providerId).findAll().deleteAllFromRealm();
                        }
                        catch (Exception ignored) {}
                    }
                });
            }

            @Override
            public void showRssFeed(String providerId) {
                interactionListener.onRssProviderClicked(providerId);
            }
        };

        if (viewType == TYPE_LOADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loader, parent, false);
            vh = new ViewHolder(v, viewType, null);
        }
        else if (viewType == TYPE_EMPTY) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nothing_to_show, parent, false);
            vh = new ViewHolder(v, viewType, null);
        }
        else if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rss_provider, parent, false);
            vh = new ViewHolder(v, viewType, listener);
        }

        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        if (!dataLoaded) {
            return TYPE_LOADER;
        }
        else {
            if (rssProviders.isEmpty()) {
                return TYPE_EMPTY;
            }
            else {
                return TYPE_ITEM;
            }
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ITEM) {
            RssProvider rssProvider = rssProviders.get(position);

            // set provider id
            holder.rssProviderId = rssProvider.getId();

            // set photo
            if (rssProvider.getImageUrl() != null && !rssProvider.getImageUrl().isEmpty()) {
                ImageSize targetSize = new ImageSize(holder.img_photo.getWidth(), holder.img_photo.getHeight());
                imageLoader.loadImage(rssProvider.getImageUrl(), targetSize, options, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        holder.img_photo.setImageBitmap(loadedImage);
                        AnimationHelper.animateIn(holder.img_photo);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        holder.img_photo.setImageResource(R.drawable.img_empty);
                        Log.e("IMAGE_LOADER", "Failed  to load image on url: " + imageUri);
                    }
                });
            }
            else {
                holder.img_photo.setImageResource(R.drawable.img_empty);
            }

            // set name (title)
            holder.text_name.setText(rssProvider.getName());

            // set description
            holder.text_description.setText(rssProvider.getDescription());
        }
    }

    public boolean isDataLoaded() {
        return dataLoaded;
    }

    public void setDataLoaded(boolean dataLoaded) {
        this.dataLoaded = dataLoaded;
    }
}
