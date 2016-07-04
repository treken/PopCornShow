package activity;

import android.os.Bundle;
import android.widget.Toast;

import br.com.icaro.filme.R;
import fragment.ListFilmesFragment;
import utils.Constantes;

/**
 * Created by icaro on 02/07/16.
 */
public class FilmesActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpToolBar();
        setupNavDrawer();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toast.makeText(this, "Lat/Lng: %s/%s - Usar em generos", Toast.LENGTH_SHORT).show();
        getSupportActionBar().setTitle(getString(getIntent().getIntExtra(Constantes.NAV_DRAW_ESCOLIDO, 0)));
        if (savedInstanceState == null) {
            ListFilmesFragment listFilmesFragment = new ListFilmesFragment();
            listFilmesFragment.setArguments(getIntent().getExtras());
            setCheckable(getIntent().getIntExtra(Constantes.ABA,0));
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, listFilmesFragment)
                    .commit();
        }

    }


}
