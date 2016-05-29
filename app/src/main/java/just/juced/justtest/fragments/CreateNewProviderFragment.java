package just.juced.justtest.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.crazyhitty.chdev.ks.rssmanager.OnFeedLoadListener;
import com.crazyhitty.chdev.ks.rssmanager.OnRssLoadListener;
import com.crazyhitty.chdev.ks.rssmanager.RssItem;
import com.crazyhitty.chdev.ks.rssmanager.RssReader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import just.juced.justtest.R;
import just.juced.justtest.application.MyApplication;
import just.juced.justtest.helpers.AnimationHelper;
import just.juced.justtest.helpers.CustomAsyncTask;
import just.juced.justtest.helpers.InputHelper;
import just.juced.justtest.models.RssProvider;

public class CreateNewProviderFragment extends Fragment implements OnRssLoadListener {

    public static final String SAMPLE_RSS = "https://lenta.ru/rss/news";

    private Realm realm;

    public interface FragmentInteractionListener {
        String getRssProviderId();
    }

    private FragmentInteractionListener mListener;
    private String rssProviderId;
    private RssProvider rssProvider;
    private boolean dataLoaded = false;

    private View view;
    private RelativeLayout block_loader;
    private ScrollView block_content;
    private TextInputLayout input_rssUrl;
    private Button btn_next;

    private LinearLayout block_providerInfo;
    private ImageView img_photo;
    private TextView text_name;
    private TextView text_description;

    private FloatingActionButton fab;

    public CreateNewProviderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        realm = Realm.getDefaultInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_create_new, container, false);
            getViewElems();
            setActions();
            fab.hide();
        }

        if (!dataLoaded) {
            prepareData();
        }

        return view;
    }

    private void getViewElems() {
        block_loader = (RelativeLayout) view.findViewById(R.id.block_loader);
        block_content = (ScrollView) view.findViewById(R.id.block_content);
        input_rssUrl = (TextInputLayout) view.findViewById(R.id.input_rssUrl);
        btn_next = (Button) view.findViewById(R.id.btn_next);

        // provider info views
        block_providerInfo = (LinearLayout) view.findViewById(R.id.block_providerInfo);
        img_photo = (ImageView) view.findViewById(R.id.img_photo);
        text_name = (TextView) view.findViewById(R.id.text_name);
        text_description = (TextView) view.findViewById(R.id.text_description);
        block_providerInfo.setVisibility(View.GONE);

        // get fab
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
    }

    @SuppressWarnings("ConstantConditions")
    private void setActions() {
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workWithRssUrl();
            }
        });

        input_rssUrl.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (rssProvider != null && rssProvider.getUrl() != null && !rssProvider.getUrl().equals(s.toString())) {
                    btn_next.setVisibility(View.VISIBLE);
                    fab.hide();
                    block_providerInfo.setVisibility(View.GONE);
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
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

    private void prepareData() {
        dataLoaded = true;
        AnimationHelper.toggleViews(getContext(), false, block_loader, block_content);

        rssProviderId = mListener.getRssProviderId();
        if (rssProviderId == null || rssProviderId.isEmpty()) {
            // create new
            rssProvider = new RssProvider();
            fillData();
        }
        else {
            // get exist
            RealmQuery<RssProvider> query = realm.where(RssProvider.class);
            query.equalTo("id", rssProviderId);
            RealmResults<RssProvider> results = query.findAllAsync();
            RealmChangeListener<RealmResults<RssProvider>> realmChangeListener = new RealmChangeListener<RealmResults<RssProvider>>() {
                @Override
                public void onChange(RealmResults<RssProvider> results) {
                    if (results == null || results.isEmpty()) {
                        rssProvider = new RssProvider();
                    }
                    else {
                        rssProvider = results.first();
                    }

                    fillData();
                }
            };
            results.addChangeListener(realmChangeListener);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void fillData() {
        AnimationHelper.toggleViews(getContext(), true, block_loader, block_content);
        input_rssUrl.getEditText().setText(/*SAMPLE_RSS*/ rssProvider.getUrl());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (realm != null) {
            realm.close();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void workWithRssUrl() {
        InputHelper.hideKeyboard(getActivity(), input_rssUrl.getEditText());
        fab.hide();
        AnimationHelper.toggleViews(getContext(), false, block_loader, block_content);

        String[] urlArr = {input_rssUrl.getEditText().getText().toString()};

        try {
            new RssReader(getContext())
                    .showDialog(false)
                    .urls(urlArr)
                    .parseProviderInfo(CreateNewProviderFragment.this);
        }
        catch (Exception e) {
            onFailure(e.getMessage());
        }
    }

    @Override
    public void onSuccess(List<RssItem> rssItems) {
        // nothing to do
    }

    @Override
    public void onFailure(String message) {
        AnimationHelper.toggleViews(getContext(), true, block_loader, block_content);
        btn_next.setVisibility(View.VISIBLE);
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onSuccessProviderInfo(String title, String description, String imageUrl) {
        AnimationHelper.toggleViews(getContext(), true, block_loader, block_content);
        btn_next.setVisibility(View.INVISIBLE);
        fab.show();

        rssProvider.setUrl(input_rssUrl.getEditText().getText().toString());
        rssProvider.setName(title);
        rssProvider.setDescription(description);
        rssProvider.setImageUrl(imageUrl);

        fillProviderInfoViews();
    }

    private void fillProviderInfoViews() {
        block_providerInfo.setVisibility(View.VISIBLE);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(MyApplication.getSingleton().getApplicationContext()).build();
        ImageLoader.getInstance().init(config);
        ImageLoader imageLoader = ImageLoader.getInstance();

        // set photo
        if (rssProvider.getImageUrl() != null && !rssProvider.getImageUrl().isEmpty()) {
            ImageSize targetSize = new ImageSize(img_photo.getWidth(), img_photo.getHeight());
            imageLoader.loadImage(rssProvider.getImageUrl(), targetSize, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    img_photo.setImageBitmap(loadedImage);
                    AnimationHelper.animateIn(img_photo);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    img_photo.setVisibility(View.GONE);

                    Log.e("IMAGE_LOADER", "Failed  to load image on url: " + imageUri);
                }
            });
        }
        else {
            img_photo.setVisibility(View.GONE);
        }

        // set name (title)
        if (rssProvider.getName() != null && !rssProvider.getName().isEmpty()) {
            text_name.setText(rssProvider.getName());
            text_name.setVisibility(View.VISIBLE);
        }
        else {
            text_name.setVisibility(View.GONE);
        }

        // set description
        if (rssProvider.getDescription() != null && !rssProvider.getDescription().isEmpty()) {
            text_description.setText(rssProvider.getDescription());
            text_description.setVisibility(View.VISIBLE);
        }
        else {
            text_description.setVisibility(View.GONE);
        }
    }

    private void saveData() {
        fab.hide();
        AnimationHelper.toggleViews(getContext(), false, block_loader, block_content);

        // write new rss provider to realm
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                bgRealm.copyToRealm(rssProvider);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), R.string.rssProviderSuccessfullySaved, Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                AnimationHelper.toggleViews(getContext(), true, block_loader, block_content);
                Snackbar.make(view, error.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
