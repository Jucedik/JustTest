package just.juced.justtest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import just.juced.justtest.R;
import just.juced.justtest.application.MyApplication;
import just.juced.justtest.fragments.StartActivityFragment;

public class StartActivity extends AppCompatActivity {

    public static final String FRAGMENT_TAG_START_ACTIVITY = "FRAGMENT_TAG_START_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setToolbar();
        setContentBlock();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //noinspection ConstantConditions
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show create new rss provider activity
                Intent intent = new Intent(getApplicationContext(), CreateNewProviderActivity.class);
                Bundle b = new Bundle();
                b.putString(Constants.ARG_PARAM_RSS_PROVIDER_ID, null);
                intent.putExtras(b);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setContentBlock() {
        FragmentManager fm = getSupportFragmentManager();
        StartActivityFragment startActivityFragment = (StartActivityFragment) fm.findFragmentByTag(FRAGMENT_TAG_START_ACTIVITY);
        if (startActivityFragment == null) {
            startActivityFragment = new StartActivityFragment();
            fm.beginTransaction().add(R.id.block_content, startActivityFragment, FRAGMENT_TAG_START_ACTIVITY)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
        else {
            fm.beginTransaction().show(startActivityFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (MyApplication.getSingleton().isMyServiceRunning()) {
            menu.getItem(0).setTitle(getString(R.string.stopService));
        }
        else {
            menu.getItem(0).setTitle(getString(R.string.runService));
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_toggleService) {
            if (MyApplication.getSingleton().isMyServiceRunning()) {
                MyApplication.getSingleton().cancelIntentServiceAlarm();
            }
            else {
                MyApplication.getSingleton().fireIntentService();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
