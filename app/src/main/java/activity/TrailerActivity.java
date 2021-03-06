package activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.firebase.analytics.FirebaseAnalytics;

import br.com.icaro.filme.R;
import utils.Config;
import utils.Constantes;


/**
 * Created by icaro on 12/07/16.
 */

public class TrailerActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private String youtube_key;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_layout);
        YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        TextView sinopse = (TextView) findViewById(R.id.trailer_sinopse);
        youtube_key = getIntent().getStringExtra(Constantes.INSTANCE.getYOU_TUBE_KEY());
        youTubeView.initialize(Config.YOUTUBE_API_KEY, this);
        sinopse.setText(getIntent().getStringExtra(Constantes.INSTANCE.getSINOPSE()));

//        AdView adview = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder()
//                .build();
//        adview.loadAd(adRequest);

    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                        YouTubePlayer player, boolean wasRestored) {
        try {
            if (!wasRestored) {
                player.cueVideo(youtube_key);
                player.setFullscreen(true);
                player.addFullscreenControlFlag(1);
                player.play();

                FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Event.SELECT_CONTENT, "Play_youTube");
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, youtube_key);
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            }
        } catch ( Exception e){
            Crashlytics.logException(e);
            if (!isFinishing())
            Toast.makeText(this, R.string.ops, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Crashlytics.logException(new Exception("Erro em \"onInitializationFailure\" dentro de " + this.getClass()));
    }
}
