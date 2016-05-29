package just.juced.justtest.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

import io.realm.RealmResults;
import just.juced.justtest.R;
import just.juced.justtest.application.MyApplication;
import just.juced.justtest.helpers.AnimationHelper;
import just.juced.justtest.models.RssFeedItem;

/**
 * Created by juced on 28.05.2016.
 */
public class RssItemsAdapter extends RecyclerView.Adapter<RssItemsAdapter.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            interactionListener.onRssFeedClicked(itemId);
        }

        String itemId;

        LinearLayout block_clickableContent;
        ImageView img_photo;
        TextView text_title;
        TextView text_category;
        TextView text_date;

        public ViewHolder(View view) {
            super(view);

            block_clickableContent = (LinearLayout) view.findViewById(R.id.block_clickableContent);
            img_photo = (ImageView) view.findViewById(R.id.img_photo);
            text_title = (TextView) view.findViewById(R.id.text_title);
            text_category = (TextView) view.findViewById(R.id.text_category);
            text_date = (TextView) view.findViewById(R.id.text_date);

            img_photo.setImageDrawable(null);

            block_clickableContent.setOnClickListener(this);
        }
    }

    public interface RssProvidersAdapterInteractionListener {
        void onRssFeedClicked(String itemId);
    }

    private RssProvidersAdapterInteractionListener interactionListener;
    private RealmResults<RssFeedItem> rssFeedItems;

    private DisplayImageOptions options;
    private ImageLoader imageLoader;

    public RssItemsAdapter(RealmResults<RssFeedItem> rssFeedItems, RssProvidersAdapterInteractionListener interactionListener) {
        this.rssFeedItems = rssFeedItems;
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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rss_feed, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        RssFeedItem item = rssFeedItems.get(position);
        holder.itemId = item.getGuid();

        // set title
        holder.text_title.setText(item.getTitle());

        // set category name
        holder.text_category.setText(item.getCategoryName());

        // set date
        holder.text_date.setText(item.getPubDate());

        // set photo
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            ImageSize targetSize = new ImageSize(holder.img_photo.getWidth(), holder.img_photo.getHeight());
            imageLoader.loadImage(item.getImageUrl(), targetSize, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    holder.img_photo.setImageBitmap(loadedImage);
                    holder.img_photo.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    holder.img_photo.setVisibility(View.GONE);
                }
            });
        }
        else {
            holder.img_photo.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return rssFeedItems.size();
    }

    public void setRssFeedItems(RealmResults<RssFeedItem> rssFeedItems) {
        this.rssFeedItems = rssFeedItems;
    }
}
