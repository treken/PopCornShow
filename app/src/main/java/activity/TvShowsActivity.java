package activity;

/**
 * Created by icaro on 14/09/16.
 */

import android.app.SearchManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;

import br.com.icaro.filme.R;
import fragment.TvShowsFragment;
import utils.Constantes;


public class TvShowsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_main); // ???
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setUpToolBar();
        setupNavDrawer();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent()
                .getStringExtra(Constantes.INSTANCE.getNAV_DRAW_ESCOLIDO()));

//        AdView adview = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
//                .addTestDevice("AC98C820A50B4AD8A2106EDE96FB87D4")  // An example device ID
//                .build();
//        adview.loadAd(adRequest);

        if (savedInstanceState == null) {
            TvShowsFragment tvShowsFragment = new TvShowsFragment();
            tvShowsFragment.setArguments(getIntent().getExtras());
            setCheckable(getIntent().getIntExtra(Constantes.INSTANCE.getABA(), 0));
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container_list_main, tvShowsFragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getResources().getString(R.string.procurar));
        searchView.setEnabled(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home: {
                finish();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

}

