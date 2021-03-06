package listafilmes.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import activity.BaseActivity;
import br.com.icaro.filme.R;
import listafilmes.fragment.FilmesFragment;
import utils.Constantes;

public class FilmesActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_list_main);
		setUpToolBar();
		setupNavDrawer();
		getExtras();
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		AdView adview = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
				.addTestDevice("AC98C820A50B4AD8A2106EDE96FB87D4")  // An example device ID
				.build();
		adview.loadAd(adRequest);

		if (savedInstanceState == null) {
			FilmesFragment filmesFragment = new FilmesFragment();
			filmesFragment.setArguments(getIntent().getExtras());
			getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.container_list_main, filmesFragment)
					.commit();
		}

	}

	private void getExtras() {
		if (getIntent().getAction() == null) {
			getSupportActionBar().setTitle(getString(getIntent()
					.getIntExtra(Constantes.INSTANCE.getNAV_DRAW_ESCOLIDO(), R.string.now_playing)));
		} else {
			getSupportActionBar().setTitle(getString(Integer.parseInt(getIntent()
					.getStringExtra(Constantes.INSTANCE.getNAV_DRAW_ESCOLIDO()))));
		}
	}

}
