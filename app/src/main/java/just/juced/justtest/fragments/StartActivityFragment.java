package just.juced.justtest.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import just.juced.justtest.R;
import just.juced.justtest.activities.Constants;
import just.juced.justtest.activities.CreateNewProviderActivity;
import just.juced.justtest.activities.RssItemListActivity;
import just.juced.justtest.adapters.RssProvidersAdapter;
import just.juced.justtest.application.MyApplication;
import just.juced.justtest.helpers.CustomAsyncTask;
import just.juced.justtest.models.RssProvider;
import just.juced.justtest.views.AutofitRecyclerView;

public class StartActivityFragment extends Fragment {

    private Realm realm;

    private View view;
    private AutofitRecyclerView recyclerView;
    private RealmResults<RssProvider> rssProviders;
    private RssProvidersAdapter rssProvidersAdapter;
    private boolean dataLoaded = false;

    public StartActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (rssProviders != null) {
            rssProviders.removeChangeListeners();
        }

        if (realm != null) {
            realm.close();
        }
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
            view = inflater.inflate(R.layout.fragment_start_activity, container, false);
            getViewElems();
        }

        if (!dataLoaded) {
            loadData();
        }

        return view;
    }

    private void getViewElems() {
        recyclerView = (AutofitRecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setOnLoadAnimated(true);
        recyclerView.setHasFixedSize(true);
        final GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void loadData() {
        dataLoaded = true;

        rssProviders = realm.where(RssProvider.class).findAllAsync();
        RealmChangeListener<RealmResults<RssProvider>> realmChangeListener = new RealmChangeListener<RealmResults<RssProvider>>() {
            @Override
            public void onChange(RealmResults<RssProvider> results) {
                rssProvidersAdapter.setDataLoaded(true);
                rssProvidersAdapter.notifyDataSetChanged();
            }
        };
        rssProviders.addChangeListener(realmChangeListener);

        fillData();
    }

    private void fillData() {
        rssProvidersAdapter = new RssProvidersAdapter(rssProviders, realm, new RssProvidersAdapter.RssProvidersAdapterInteractionListener() {
            @Override
            public void onRssProviderClicked(String providerId) {
                // show rss feeds activity
                Intent intent = new Intent(getContext(), RssItemListActivity.class);
                Bundle b = new Bundle();
                b.putString(Constants.ARG_PARAM_RSS_PROVIDER_ID, providerId);
                intent.putExtras(b);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onRssProviderRemoved(String providerId) {
                // show Snackbar
                Snackbar.make(view, R.string.rssProviderSuccessfullyRemoved, Snackbar.LENGTH_LONG).show();
            }
        });
        recyclerView.setAdapter(rssProvidersAdapter);
    }
}
