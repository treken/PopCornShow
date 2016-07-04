package activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import br.com.icaro.filme.R;
import fragment.ListFilmesFragment;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpToolBar();
        setupNavDrawer();

        if (savedInstanceState == null) {
            ListFilmesFragment listFilmesFragment = new ListFilmesFragment();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.container, listFilmesFragment, null);
            fragmentTransaction.commit();
        }

    }
}
