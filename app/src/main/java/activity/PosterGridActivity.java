package activity;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;

import java.util.List;

import adapter.PosterGridAdapter;
import br.com.icaro.filme.R;
import info.movito.themoviedbapi.model.Artwork;
import info.movito.themoviedbapi.model.ArtworkType;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.tv.TvSeries;
import utils.Constantes;

/**
 * Created by icaro on 28/07/16.
 */

public class PosterGridActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.poster_grid);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycleView_poster_grid);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new GridLayoutManager(getBaseContext(), 2));

//        AdView adview = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
//                .addTestDevice("AC98C820A50B4AD8A2106EDE96FB87D4")  // An example device ID
//                .build();
//        adview.loadAd(adRequest);

        if (getIntent().getExtras() != null) {
            if (getIntent().getSerializableExtra(Constantes.INSTANCE.getSERIE()) != null) {
                TvSeries series = (TvSeries) getIntent().getSerializableExtra(Constantes.INSTANCE.getSERIE());
              //  Log.d("PosterGridActivity", "SERIE " + series.getName());
                List<Artwork> artworks = series.getImages().getPosters();
                recyclerView.setAdapter(new PosterGridAdapter(PosterGridActivity.this, artworks, series.getName()));
                return;
            }
            if (getIntent().getSerializableExtra(Constantes.INSTANCE.getFILME()) != null) {
                MovieDb movieDb = (MovieDb) getIntent().getSerializableExtra(Constantes.INSTANCE.getFILME());
              //  Log.d("PosterGridActivity", "FILME" + movieDb.getTitle());
                List<Artwork> artworks = movieDb.getImages(ArtworkType.POSTER);
                recyclerView.setAdapter(new PosterGridAdapter(PosterGridActivity.this, artworks, movieDb.getTitle()));
                return;
            }
        }
    }
}
