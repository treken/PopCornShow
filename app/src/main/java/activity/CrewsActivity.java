package activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import adapter.CrewsAdapter;
import adapter.ElencoAdapter;
import br.com.icaro.filme.R;
import domian.FilmeService;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbTvSeasons;
import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.Multi;
import utils.Constantes;
import utils.UtilsFilme;



public class CrewsActivity extends BaseActivity {

    RecyclerView recyclerView;
    LinearLayout linear_crews_layout;
    int id;
    Multi.MediaType mediaType;
    ProgressBar progressBar;
    //Credits creditsTvShow;
    MovieDb movies;
    int season = -100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crews);
        setUpToolBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        id = getIntent().getIntExtra(Constantes.ID, 0);
        mediaType = (Multi.MediaType) getIntent().getSerializableExtra(Constantes.MEDIATYPE);
        season = getIntent().getIntExtra(Constantes.TVSEASONS, -100);
        Log.d("CrewsActivity", " " + id);
        String title = getIntent().getStringExtra(Constantes.NOME);
        getSupportActionBar().setTitle(title);

        recyclerView = (RecyclerView) findViewById(R.id.crews_recyckeview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        linear_crews_layout = (LinearLayout) findViewById(R.id.linear_crews_layout);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        AdView adview = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("AC98C820A50B4AD8A2106EDE96FB87D4")  // An example device ID
                .build();
        adview.loadAd(adRequest);


        if (UtilsFilme.isNetWorkAvailable(getBaseContext())) {
            new TMDVAsync().execute();
        } else {
            snack();
        }

    }

    protected void snack() {
        Snackbar.make(linear_crews_layout, R.string.no_internet, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (UtilsFilme.isNetWorkAvailable(getBaseContext())) {
                            new TMDVAsync().execute();
                        } else {
                            snack();
                        }
                    }
                }).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return true;
    }


    private class TMDVAsync extends AsyncTask<Void, Void, Void> {
        Credits creditsTvShow;
        @Override
        protected Void doInBackground(Void... voids) {
            if (Multi.MediaType.TV_SERIES.equals(mediaType)) {
                creditsTvShow = FilmeService.getTmdbTvShow().getCredits(id, "en");
                Log.d("CrewsActivity", "IF " + creditsTvShow.getCrew().size());
            }
            if (Multi.MediaType.TV_SERIES.equals(mediaType) && season != -100){
                creditsTvShow = FilmeService.getTmdbTvSeasons()
                        .getSeason(id, season,"en", TmdbTvSeasons.SeasonMethod.credits)
                        .getCredits();
                Log.d("CrewsActivity", "-100 " + creditsTvShow.getCrew().size());
            }

            if (Multi.MediaType.MOVIE.equals(mediaType)) {
                TmdbMovies tmdbMovies = FilmeService.getTmdbMovies();
                movies = tmdbMovies.getMovie(id, "en", TmdbMovies.MovieMethod.credits);
                Log.d("CrewsActivity", "" + movies.getCredits().getCast().size());
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid){
            Log.d("CrewsActivity", "onPostExecute");
            progressBar.setVisibility(View.GONE);
            if (Multi.MediaType.MOVIE.equals(mediaType)) {
                recyclerView.setAdapter(new CrewsAdapter(CrewsActivity.this, movies.getCredits().getCrew()));
            }
            if (Multi.MediaType.TV_SERIES.equals(mediaType)) {
                Log.d("CrewsActivity", "IF " + creditsTvShow.getCrew().size());
                recyclerView.setAdapter(new CrewsAdapter(CrewsActivity.this, creditsTvShow.getCrew()));
            }
        }
    }
}
