package just.juced.justtest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import just.juced.justtest.R;
import just.juced.justtest.fragments.CreateNewProviderFragment;
import just.juced.justtest.fragments.RssItemDetailFragment;
import just.juced.justtest.fragments.RssItemDetailsLargeFragment;

public class RssItemDetailActivity extends AppCompatActivity implements RssItemDetailsLargeFragment.FragmentInteractionListener {

    // interface implements ------------------------------------------------------------------------
    @Override
    public String getRssFeedId() {
        return this.rssFeedId;
    }
    // ---------------------------------------------------------------------------------------------

    public static final String FRAGMENT_TAG_DETAILS_LARGE = "FRAGMENT_TAG_DETAILS_LARGE";

    private String rssFeedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssitem_detail);
        getIntentData(savedInstanceState);
        setContentBlock();
    }

    private void setContentBlock() {
        FragmentManager fm = getSupportFragmentManager();
        RssItemDetailsLargeFragment rssItemDetailsLargeFragment = (RssItemDetailsLargeFragment) fm.findFragmentByTag(FRAGMENT_TAG_DETAILS_LARGE);
        if (rssItemDetailsLargeFragment == null) {
            rssItemDetailsLargeFragment = new RssItemDetailsLargeFragment();
            fm.beginTransaction().add(R.id.block_content, rssItemDetailsLargeFragment, FRAGMENT_TAG_DETAILS_LARGE)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
        else {
            fm.beginTransaction().show(rssItemDetailsLargeFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }
    }

    private void getIntentData(Bundle savedInstanceState) {
        try {
            if (savedInstanceState == null) {
                rssFeedId = getIntent().getExtras().getString(Constants.ARG_PARAM_RSS_FEED_ID, null);
            }
            else {
                rssFeedId = savedInstanceState.getString(Constants.ARG_PARAM_RSS_FEED_ID, null);
            }
        }
        catch (Exception ignored) {}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.ARG_PARAM_RSS_FEED_ID, rssFeedId);
    }
}
