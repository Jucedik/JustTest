package just.juced.justtest.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import just.juced.justtest.R;
import just.juced.justtest.activities.Constants;
import just.juced.justtest.activities.RssItemDetailActivity;
import just.juced.justtest.activities.RssItemListActivity;
import just.juced.justtest.adapters.RssCategoriesSpinnerAdapter;
import just.juced.justtest.adapters.RssItemsAdapter;
import just.juced.justtest.application.MyApplication;
import just.juced.justtest.helpers.AnimationHelper;
import just.juced.justtest.helpers.ArrayHelper;
import just.juced.justtest.models.RssFeedItem;
import just.juced.justtest.models.RssProvider;
import just.juced.justtest.plugins.rss_service_worker.RssServiceWorkerConstants;
import just.juced.justtest.plugins.rss_service_worker.service.RssWorkerIntentService;
import just.juced.justtest.views.AutofitRecyclerView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RssItemListFragment extends Fragment {

    public interface FragmentInteractionListener {
        String getRssProviderId();
    }

    private Realm realm;
    private FragmentInteractionListener mListener;
    private String rssProviderId;
    private RssProvider rssProvider;
    private boolean dataLoaded = false;
    private RealmResults<RssFeedItem> rssFeedItems;
    private RssItemsAdapter rssItemsAdapter;
    private boolean firstTimeForLoadFeedsFromRealm = true;
    private RssCategoriesSpinnerAdapter spinnerAdapter;
    private boolean needToShowEmptyFragment = true;

    private boolean mTwoPane = false;
    private View view;
    private AutofitRecyclerView recyclerView;
    private FrameLayout block_details;
    private RelativeLayout block_loader;
    private LinearLayout block_content;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipe_refresh_layout;
    private Spinner spinner;

    private String selectedCategoryName;

    public RssItemListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        realm = Realm.getDefaultInstance();
        rssProviderId = mListener.getRssProviderId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //if (view == null) {
        view = inflater.inflate(R.layout.fragment_rssitem_list, container, false);
        clearViewsPointers();
        getViewElems();
        setToolbar();
        setRefreshSpinner();
        //}

        if (!dataLoaded) {
            loadProviderData();
        }
        else {
            recyclerView.setOnLoadAnimated(true);
            fillData(true);
        }

        return view;
    }

    private void clearViewsPointers() {
        block_loader = null;
        block_content = null;
        recyclerView = null;
        block_details = null;
        toolbar = null;
        swipe_refresh_layout = null;
    }

    private void setRefreshSpinner() {
        swipe_refresh_layout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        swipe_refresh_layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadFeedsFromNetwork(true);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        toolbar.setTitle(((RssItemListActivity) getActivity()).getTitle());
        ((RssItemListActivity) getActivity()).setSupportActionBar(toolbar);
        ((RssItemListActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((RssItemListActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void getViewElems() {
        block_loader = (RelativeLayout) view.findViewById(R.id.block_loader);
        block_content = (LinearLayout) view.findViewById(R.id.block_content);

        recyclerView = (AutofitRecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setOnLoadAnimated(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        block_details = (FrameLayout) view.findViewById(R.id.block_details);

        mTwoPane = block_details != null;

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        swipe_refresh_layout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);

        if (mTwoPane && needToShowEmptyFragment) {
            setEmptyContent();
        }
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

    private void loadProviderData() {
        dataLoaded = true;
        AnimationHelper.toggleViews(getContext(), false, block_loader, block_content);

        rssProviderId = mListener.getRssProviderId();
        if (rssProviderId == null || rssProviderId.isEmpty()) {
            // finish activity
            rssProvider = new RssProvider();
            getActivity().finish();
        }
        else {
            RealmQuery<RssProvider> query = realm.where(RssProvider.class);
            query.equalTo("id", rssProviderId);
            RealmResults<RssProvider> results = query.findAllAsync();
            RealmChangeListener<RealmResults<RssProvider>> realmChangeListener = new RealmChangeListener<RealmResults<RssProvider>>() {
                @Override
                public void onChange(RealmResults<RssProvider> results) {
                    if (results == null || results.isEmpty()) {
                        getActivity().finish();
                    }
                    else {
                        rssProvider = results.first();
                    }

                    loadFeedsFromRealm(null);
                }
            };
            results.addChangeListener(realmChangeListener);
        }
    }

    private void loadFeedsFromRealm(String categoryName) {
        RealmQuery<RssFeedItem> query = realm.where(RssFeedItem.class);
        query.equalTo("providerId", rssProvider.getId());

        if (categoryName != null) {
            query.equalTo("categoryName", categoryName);
        }

        rssFeedItems = query.findAllSortedAsync("pubDate", Sort.DESCENDING);
        RealmChangeListener<RealmResults<RssFeedItem>> realmChangeListener = new RealmChangeListener<RealmResults<RssFeedItem>>() {
            @Override
            public void onChange(RealmResults<RssFeedItem> results) {
                swipe_refresh_layout.setRefreshing(false);

                try {
                    if (firstTimeForLoadFeedsFromRealm && results.isEmpty()) {
                        firstTimeForLoadFeedsFromRealm = false;

                        // load feeds from server
                        loadFeedsFromNetwork(false);
                    }
                    else {
                        // fill data or update adapter
                        if (rssItemsAdapter == null) {
                            fillData(false);
                        }
                        else {
                            rssItemsAdapter.setRssFeedItems(results);
                            setCategoriesSelectorSpinner(false);
                            rssItemsAdapter.notifyDataSetChanged();
                        }
                    }
                }
                catch (Exception e) {
                    String sss = "123";
                }
            }
        };
        rssFeedItems.addChangeListener(realmChangeListener);
    }

    private void loadFeedsFromNetwork(boolean forceChangeData) {
        // intent service already running, return
        if (MyApplication.getSingleton().isServiceWorkerRunning()) {
            return;
        }

        Intent i = new Intent(getContext(), RssWorkerIntentService.class);
        i.putExtra(RssServiceWorkerConstants.COMMAND, RssServiceWorkerConstants.COMMAND_LOAD_SINGLE_RSS);
        i.putExtra(RssServiceWorkerConstants.ARG_RSS_PROVIDER_ID, rssProviderId);

        if (forceChangeData) {
            i.putExtra(RssServiceWorkerConstants.ARG_FORCE, RssServiceWorkerConstants.ARG_TRUE);
        }
        else {
            i.putExtra(RssServiceWorkerConstants.ARG_FORCE, RssServiceWorkerConstants.ARG_FALSE);
        }

        getContext().startService(i);
    }

    protected void setCategoriesSelectorSpinner(boolean forceToolbarSpinner) {
        /*if (selectedCategoryName != null) {
            return;
        }*/

        List<String> items = new ArrayList<>();
        for (int i = 0; i < rssFeedItems.size(); i++) {
            String categName = rssFeedItems.get(i).getCategoryName();
            if (categName != null && !categName.isEmpty() && !items.contains(categName)) {
                items.add(categName);
            }
        }
        Collections.sort(items);
        items.add(0, MyApplication.getSingleton().getResources().getString(R.string.allCategs));

        if (forceToolbarSpinner || spinnerAdapter == null || !ArrayHelper.equalLists(spinnerAdapter.getmItems(), items)) {
            View spinnerContainer = LayoutInflater.from(getContext()).inflate(R.layout.toolbar_spinner, toolbar, false);
            ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            toolbar.addView(spinnerContainer, lp);

            spinnerAdapter = new RssCategoriesSpinnerAdapter();
            spinnerAdapter.addItems(items);

            spinner = (Spinner) spinnerContainer.findViewById(R.id.toolbar_spinner);
            spinner.setAdapter(spinnerAdapter);

            if (selectedCategoryName != null && !selectedCategoryName.isEmpty()) {
                spinner.setSelection(spinnerAdapter.getmItems().indexOf(selectedCategoryName));
            }

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!spinnerAdapter.getItem(position).equals(selectedCategoryName)) {
                        if (spinnerAdapter.getItem(position).equals(MyApplication.getSingleton().getResources().getString(R.string.allCategs))) {
                            selectedCategoryName = null;
                        }
                        else {
                            selectedCategoryName = spinnerAdapter.getItem(position);
                        }

                        // apply filter
                        recyclerView.setOnLoadAnimated(false);

                        if (rssFeedItems != null) {
                            rssFeedItems.removeChangeListeners();
                        }

                        if (position == 0) {
                            loadFeedsFromRealm(null);
                        }
                        else {
                            loadFeedsFromRealm(spinnerAdapter.getItem(position));
                        }

                        if (mTwoPane) {
                            needToShowEmptyFragment = true;
                            setEmptyContent();
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    private void fillData(boolean forceToolbarSpinner) {
        AnimationHelper.toggleViews(getContext(), true, block_loader, block_content);

        // set category selector
        setCategoriesSelectorSpinner(forceToolbarSpinner);

        // set rss feeds
        rssItemsAdapter = new RssItemsAdapter(rssFeedItems, new RssItemsAdapter.RssProvidersAdapterInteractionListener() {
            @Override
            public void onRssFeedClicked(String itemId) {
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(Constants.ARG_PARAM_RSS_FEED_ID, itemId);
                    RssItemDetailFragment fragment = new RssItemDetailFragment();
                    fragment.setArguments(arguments);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.block_details, fragment)
                            .commit();
                }
                else {
                    Intent intent = new Intent(getContext(), RssItemDetailActivity.class);
                    intent.putExtra(Constants.ARG_PARAM_RSS_FEED_ID, itemId);
                    getContext().startActivity(intent);
                }
            }
        });
        recyclerView.setAdapter(rssItemsAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (rssFeedItems != null) {
            rssFeedItems.removeChangeListeners();
        }

        if (realm != null) {
            realm.close();
        }
    }

    private void setEmptyContent() {
        needToShowEmptyFragment = false;
        EmptyContentFragment fragment = new EmptyContentFragment();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.block_details, fragment)
                .commit();
    }

}
