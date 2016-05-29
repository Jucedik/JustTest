package just.juced.justtest.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import just.juced.justtest.R;
import just.juced.justtest.fragments.RssItemListFragment;

import java.util.List;

public class RssItemListActivity extends AppCompatActivity implements RssItemListFragment.FragmentInteractionListener {

    // interface implements ------------------------------------------------------------------------
    @Override
    public String getRssProviderId() {
        return this.rssProviderId;
    }
    // ---------------------------------------------------------------------------------------------

    public static final String FRAGMENT_TAG_RSS_ITEM_LIST = "FRAGMENT_TAG_RSS_ITEM_LIST";


    private String rssProviderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssitem_list);
        getIntentData(savedInstanceState);
        setContentBlock();
    }

    private void setContentBlock() {
        FragmentManager fm = getSupportFragmentManager();
        RssItemListFragment rssItemListFragment = (RssItemListFragment) fm.findFragmentByTag(FRAGMENT_TAG_RSS_ITEM_LIST);
        if (rssItemListFragment == null) {
            rssItemListFragment = new RssItemListFragment();
            fm.beginTransaction().add(R.id.block_content, rssItemListFragment, FRAGMENT_TAG_RSS_ITEM_LIST)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
        else {
            fm.beginTransaction().show(rssItemListFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }
    }

    private void getIntentData(Bundle savedInstanceState) {
        try {
            if (savedInstanceState == null) {
                rssProviderId = getIntent().getExtras().getString(Constants.ARG_PARAM_RSS_PROVIDER_ID, null);
            }
            else {
                rssProviderId = savedInstanceState.getString(Constants.ARG_PARAM_RSS_PROVIDER_ID, null);
            }
        }
        catch (Exception ignored) {}
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.ARG_PARAM_RSS_PROVIDER_ID, rssProviderId);
    }

}
