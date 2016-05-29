package just.juced.justtest.activities;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import just.juced.justtest.R;
import just.juced.justtest.fragments.CreateNewProviderFragment;
import just.juced.justtest.fragments.StartActivityFragment;
import just.juced.justtest.models.RssProvider;

public class CreateNewProviderActivity extends AppCompatActivity implements CreateNewProviderFragment.FragmentInteractionListener {

    // interface implements ------------------------------------------------------------------------
    @Override
    public String getRssProviderId() {
        return this.rssProviderId;
    }
    // ---------------------------------------------------------------------------------------------

    public static final String FRAGMENT_TAG_CREATE_NEW_PROVIDER = "FRAGMENT_TAG_CREATE_NEW_PROVIDER";

    private String rssProviderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_provider);
        getIntentData(savedInstanceState);
        setToolbar();
        setContentBlock();
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

    private void setContentBlock() {
        FragmentManager fm = getSupportFragmentManager();
        CreateNewProviderFragment createNewProviderFragment = (CreateNewProviderFragment) fm.findFragmentByTag(FRAGMENT_TAG_CREATE_NEW_PROVIDER);
        if (createNewProviderFragment == null) {
            createNewProviderFragment = new CreateNewProviderFragment();
            fm.beginTransaction().add(R.id.block_content, createNewProviderFragment, FRAGMENT_TAG_CREATE_NEW_PROVIDER)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
        else {
            fm.beginTransaction().show(createNewProviderFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onBackPressed() {
        // TODO: 27.05.2016 ask user for save unsaved changes

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // TODO: 27.05.2016 ask user for save unsaved changes

            finish();
        }

        return false;
    }
}
