package activity;

import android.os.Bundle;

import br.com.icaro.filme.R;
import fragment.FilmeFragment;
import utils.Constantes;

public class FilmeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filme);
        setUpToolBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        if (savedInstanceState == null) {
            FilmeFragment filmeFrag = new FilmeFragment();
            Bundle bundle = new Bundle(); //Tentar pegar nome que esta no bundle
            bundle.putInt(Constantes.FILME_ID, getIntent().getExtras().getInt(Constantes.FILME_ID));
            filmeFrag.setArguments(bundle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container_filme, filmeFrag, null)
                    .commit();
        }
    }
}