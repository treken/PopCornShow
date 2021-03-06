package activity

import adapter.PosterGridAdapter
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.Window
import br.com.icaro.filme.R
import com.google.android.gms.ads.AdRequest
import domain.PostersItem
import kotlinx.android.synthetic.main.poster_grid.*
import utils.Constantes

/**
 * Created by icaro on 28/07/16.
 */

class PosterGridActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
        }
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.poster_grid)
        recycleView_poster_grid.apply {
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            layoutManager = GridLayoutManager(baseContext, 2)
        }

        val adRequest = AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("AC98C820A50B4AD8A2106EDE96FB87D4")  // An example device ID
                .build()
        adView.loadAd(adRequest)


        if (intent.hasExtra(Constantes.POSTER)) {
            val posters = intent.getSerializableExtra(Constantes.POSTER) as List<PostersItem>
            val titulo = intent.getStringExtra(Constantes.NOME)
            recycleView_poster_grid.adapter = PosterGridAdapter(this@PosterGridActivity, posters, titulo)
        }

    }
}
