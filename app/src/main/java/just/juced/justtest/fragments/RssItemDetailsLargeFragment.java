package just.juced.justtest.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import io.realm.RealmQuery;
import just.juced.justtest.R;
import just.juced.justtest.activities.RssItemDetailActivity;
import just.juced.justtest.activities.RssItemListActivity;
import just.juced.justtest.application.MyApplication;
import just.juced.justtest.helpers.AnimationHelper;
import just.juced.justtest.models.RssFeedItem;
import just.juced.justtest.models.RssProvider;
import just.juced.justtest.views.AutofitRecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RssItemDetailsLargeFragment extends Fragment {

    public interface FragmentInteractionListener {
        String getRssFeedId();
    }

    private View view;
    private RelativeLayout block_loader;
    //private LinearLayout block_content;
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private ImageView img_photo;
    private TextView text_description;
    private TextView text_date;
    private TextView text_name;

    private boolean dataLoaded = false;
    private Realm realm;
    private FragmentInteractionListener mListener;
    private String rssFeedId;
    private RssFeedItem rssFeedItem;

    private DisplayImageOptions options;
    private ImageLoader imageLoader;

    public RssItemDetailsLargeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        realm = Realm.getDefaultInstance();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_rss_item_details_large, container, false);
            getViewElems();
            //setToolbar();
            setActions();
        }

        if (!dataLoaded) {
            loadData();
        }

        return view;
    }

    private void setActions() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(rssFeedItem.getLink()));
                    getActivity().startActivity(intent);
                }
                catch (Exception e) {}
            }
        });
    }

    private void getViewElems() {
        block_loader = (RelativeLayout) view.findViewById(R.id.block_loader);
        //block_content = (LinearLayout) view.findViewById(R.id.block_content);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        img_photo = (ImageView) view.findViewById(R.id.img_photo);
        text_description = (TextView) view.findViewById(R.id.text_description);
        text_date = (TextView) view.findViewById(R.id.text_date);
        text_name = (TextView) view.findViewById(R.id.text_name);
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        //toolbar.setTitle(((RssItemDetailActivity) getActivity()).getTitle());
        ((RssItemDetailActivity) getActivity()).setSupportActionBar(toolbar);
        ((RssItemDetailActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((RssItemDetailActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (FragmentInteractionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (rssFeedItem != null) {
            rssFeedItem.removeChangeListeners();
        }

        if (realm != null) {
            realm.close();
        }
    }

    private void loadData() {
        dataLoaded = true;
        block_loader.setVisibility(View.VISIBLE);
        fab.setVisibility(View.GONE);

        rssFeedId = mListener.getRssFeedId();
        RealmQuery<RssFeedItem> query = realm.where(RssFeedItem.class);
        query.equalTo("guid", rssFeedId);
        rssFeedItem = query.findFirstAsync();
        rssFeedItem.addChangeListener(new RealmChangeListener<RealmModel>() {
            @Override
            public void onChange(RealmModel element) {
                fillData();
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void fillData() {
        AnimationHelper.animateOut(block_loader);
        fab.show();

        // set category
        toolbar.setTitle(rssFeedItem.getCategoryName() == null ? "" : rssFeedItem.getCategoryName());
        setToolbar();

        // set title
        text_name.setText(rssFeedItem.getTitle());

        // set photo
        if (rssFeedItem.getImageUrl() != null && !rssFeedItem.getImageUrl().isEmpty()) {
            ImageSize targetSize = new ImageSize(img_photo.getWidth(), img_photo.getHeight());
            imageLoader.loadImage(rssFeedItem.getImageUrl(), targetSize, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    img_photo.setImageBitmap(loadedImage);
                    AnimationHelper.animateIn(img_photo);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    img_photo.setVisibility(View.GONE);
                }
            });
        }
        else {
            img_photo.setVisibility(View.GONE);
        }

        // set description
        text_description.setText(Html.fromHtml(rssFeedItem.getDescription()));

        // set date
        text_date.setText(rssFeedItem.getPubDate());
    }

}
