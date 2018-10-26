package fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast

import com.crashlytics.android.Crashlytics

import java.util.ArrayList

import activity.SettingsActivity
import adapter.MovieMainAdapter
import adapter.TvShowMainAdapter
import br.com.icaro.filme.R
import domain.FilmeService
import info.movito.themoviedbapi.TmdbMovies
import info.movito.themoviedbapi.TmdbTV
import info.movito.themoviedbapi.TvResultsPage
import info.movito.themoviedbapi.model.core.MovieResultsPage
import listafilmes.activity.FilmesActivity
import listaserie.activity.TvShowsActivity
import utils.Constantes
import utils.UtilsApp

import br.com.icaro.filme.R.string.filmes_main
import kotlinx.android.synthetic.main.filmes_main.*
import kotlinx.android.synthetic.main.tvshow_main.*
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.util.Arrays.asList
import utils.UtilsApp.getLocale
import utils.UtilsApp.getTimezone
import utils.getIdiomaEscolhido


/**
 * Created by icaro on 23/08/16.
 */
class MainFragment : Fragment() {

    private var buttonFilme: List<String>? = null
    private var buttonTvshow: List<String>? = null
    private var tipo: Int = 0
    private var popularTvshow: TvResultsPage? = null
    private var toDay: TvResultsPage? = null
    private var popularMovie: MovieResultsPage? = null
    private var cinema: MovieResultsPage? = null

    private lateinit var tvshow_popular_main: RecyclerView
    private lateinit var recycle_tvshowtoday_main: RecyclerView
    private lateinit var recycle_movie_popular_main: RecyclerView
    private lateinit var recycle_movieontheair_main: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buttonFilme = ArrayList(asList(getString(R.string.now_playing),
                getString(R.string.upcoming), getString(R.string.populares), getString(R.string.top_rated)))
        buttonTvshow = ArrayList(asList(getString(R.string.air_date),
                getString(R.string.today), getString(R.string.populares), getString(R.string.top_rated)))
        if (arguments != null) {
            tipo = arguments!!.getInt(Constantes.ABA)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (tipo == R.string.tvshow_main) {
            setScrollTvshowButton()
        }
        if (tipo == R.string.filmes_main) {
            setScrollFilmeButton()
        }

    }

    private fun setScrollFilmeButton() {
        for (i in 0..3) {
            val view = activity!!.layoutInflater.inflate(R.layout.main_botton, view as ViewGroup?, false)
            val linearLayout = getView()!!.findViewById<View>(R.id.scroll_filme_button_main) as LinearLayout
            val layoutScroll = view.findViewById<View>(R.id.layout_main_button)

            val button = view.findViewById<View>(R.id.button_main) as Button
            button.text = buttonFilme!![i]

            button.setOnClickListener { view1 ->
                when (i) {

                    0 -> {

                        val intent = Intent(activity, FilmesActivity::class.java)
                        intent.putExtra(Constantes.ABA, R.string.now_playing)
                        intent.putExtra(Constantes.NAV_DRAW_ESCOLIDO, R.string.now_playing)
                        startActivity(intent)
                    }

                    1 -> {

                        val intent = Intent(activity, FilmesActivity::class.java)
                        intent.putExtra(Constantes.ABA, R.string.upcoming)
                        intent.putExtra(Constantes.NAV_DRAW_ESCOLIDO, R.string.upcoming)
                        startActivity(intent)
                    }


                    2 -> {

                        val intent = Intent(activity, FilmesActivity::class.java)
                        intent.putExtra(Constantes.ABA, R.string.populares)
                        intent.putExtra(Constantes.NAV_DRAW_ESCOLIDO, R.string.populares)
                        startActivity(intent)
                    }

                    3 -> {

                        val intent = Intent(activity, FilmesActivity::class.java)
                        intent.putExtra(Constantes.ABA, R.string.top_rated)
                        intent.putExtra(Constantes.NAV_DRAW_ESCOLIDO, R.string.top_rated)
                        startActivity(intent)
                    }
                }
            }

            linearLayout.addView(layoutScroll)
        }

    }

    private fun setScrollTvshowButton() {
        for (i in 0..3) {
            val view = activity!!.layoutInflater.inflate(R.layout.main_botton, view as ViewGroup?, false)
            val linearLayout = getView()!!.findViewById<View>(R.id.scroll_tvshow_button_main) as LinearLayout
            val layoutScroll = view.findViewById<View>(R.id.layout_main_button)

            val button = view.findViewById<View>(R.id.button_main) as Button
            button.text = buttonTvshow!![i]

            button.setOnClickListener {
                when (i) {

                    0 -> {

                        val intent = Intent(activity, TvShowsActivity::class.java)
                        intent.putExtra(Constantes.NAV_DRAW_ESCOLIDO, R.string.air_date)
                        startActivity(intent)
                    }
                    1 -> {

                        val intent = Intent(activity, TvShowsActivity::class.java)
                        intent.putExtra(Constantes.NAV_DRAW_ESCOLIDO, R.string.today)
                        startActivity(intent)
                    }
                    2 -> {

                        val intent = Intent(activity, TvShowsActivity::class.java)
                        intent.putExtra(Constantes.NAV_DRAW_ESCOLIDO, R.string.populares)
                        startActivity(intent)
                    }

                    3 -> {

                        val intent = Intent(activity, TvShowsActivity::class.java)
                        intent.putExtra(Constantes.NAV_DRAW_ESCOLIDO, R.string.top_rated)
                        startActivity(intent)
                    }
                }
            }

            linearLayout.addView(layoutScroll)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        when (tipo) {

            filmes_main -> {
                return getViewMovie(inflater, container)
            }
            R.string.tvshow_main -> {
                return getViewTvshow(inflater, container)
            }
        }
        return null
    }

    private fun getViewMovie(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.filmes_main, container, false)
        recycle_movie_popular_main = view.findViewById<RecyclerView>(R.id.recycle_movie_popular_main).apply {
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }

        recycle_movieontheair_main = view.findViewById<RecyclerView>(R.id.recycle_movieontheair_main).apply {
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }

        if (UtilsApp.isNetWorkAvailable(context!!)) {
            try {
                GlobalScope.launch(Dispatchers.Main) {
                    val popular = async(Dispatchers.Default) { FilmeService.getTmdbMovies()
                            .getPopularMovies(getIdiomaEscolhido(context), 1) }
                    setScrollMoviePopular(popular.await())
                    val upComing = async(Dispatchers.Default) { FilmeService.getTmdbMovies()
                            .getUpcoming(getIdiomaEscolhido(context), 1) }
                    setScrollMovieOntheAir(upComing.await())
                }
            } catch (ex: java.lang.Exception) {
                Toast.makeText(context, getString(R.string.ops), Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, R.string.no_internet, Toast.LENGTH_LONG).show()
        }

        return view
    }

    private fun getViewTvshow(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.tvshow_main, container, false)
        tvshow_popular_main = view.findViewById<RecyclerView>(R.id.tvshow_popular_main).apply {
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }

        recycle_tvshowtoday_main = view.findViewById<RecyclerView>(R.id.recycle_tvshowtoday_main).apply {
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }

        if (UtilsApp.isNetWorkAvailable(context!!)) {
            try {
                GlobalScope.launch(Dispatchers.Main) {
                    val popular = async(Dispatchers.Default) { FilmeService.getTmdbTvShow()
                            .getPopular(getIdiomaEscolhido(context), 1) }
                    setScrollTvShowPopulares(popular.await())
                    val airTv = async(Dispatchers.Default) { FilmeService.getTmdbTvShow()
                            .getAiringToday(getIdiomaEscolhido(context), 1, getTimezone()) }
                    setScrollTvShowToDay(airTv.await())
                }
            } catch (ex: java.lang.Exception) {
                Toast.makeText(context, getString(R.string.ops), Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, R.string.no_internet, Toast.LENGTH_LONG).show()
        }

        return view
    }

    private fun setScrollTvShowToDay(toDay: TvResultsPage) {
        recycle_tvshowtoday_main.adapter = TvShowMainAdapter(activity, toDay)
    }

    private fun setScrollMoviePopular(popular: MovieResultsPage) {
        recycle_movieontheair_main.adapter = MovieMainAdapter(activity, popular)
    }

    private fun setScrollMovieOntheAir(airDay: MovieResultsPage) {
        recycle_movie_popular_main.adapter = MovieMainAdapter(activity, airDay)
    }

    private fun setScrollTvShowPopulares(popularTvShow: TvResultsPage) {
        tvshow_popular_main.adapter = TvShowMainAdapter(activity, popularTvShow)
    }


    private inner class MainAsync : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg voids: Void): Void? {

            if (isDetached) {
                return null
            }

            var idioma_padrao = false
            try {

                val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
                idioma_padrao = sharedPref.getBoolean(SettingsActivity.PREF_IDIOMA_PADRAO, true)
            } catch (e: Exception) {
                Crashlytics.logException(e)
            }

            if (idioma_padrao) {
                try {
                    if (UtilsApp.isNetWorkAvailable(activity!!)) {
                        val tmdbTv = FilmeService.getTmdbTvShow()
                        val tmdbMovies = FilmeService.getTmdbMovies()
                        popularTvshow = tmdbTv.getPopular(getLocale(), 1)
                        toDay = tmdbTv.getAiringToday(getLocale(), 1, getTimezone())
                        popularMovie = tmdbMovies.getPopularMovies(getLocale(), 1)
                        cinema = tmdbMovies.getUpcoming(getLocale(), 1)
                    }
                } catch (e: Exception) {
                    Crashlytics.logException(e)
                    if (activity != null)
                        activity!!.runOnUiThread { Toast.makeText(activity, R.string.ops, Toast.LENGTH_SHORT).show() }
                }

            } else {
                try { // É preciso? o tmdb não retorna 'en' se não houver o idioma?
                    if (UtilsApp.isNetWorkAvailable(activity!!)) {
                        val tmdbTv = FilmeService.getTmdbTvShow()
                        val tmdbMovies = FilmeService.getTmdbMovies()
                        popularTvshow = tmdbTv.getPopular("en", 1)
                        toDay = tmdbTv.getAiringToday("en", 1, getTimezone())
                        popularMovie = tmdbMovies.getPopularMovies("en", 1)
                        cinema = tmdbMovies.getUpcoming("en", 1)
                    }
                } catch (e: Exception) {
                    Crashlytics.logException(e)
                    if (activity != null)
                        activity!!.runOnUiThread { Toast.makeText(activity, R.string.ops, Toast.LENGTH_SHORT).show() }

                }

            }
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            try {
                if (UtilsApp.isNetWorkAvailable(activity!!) && isAdded) {
                    if (tipo == R.string.tvshow_main) {
                        //setScrollTvShowPopulares()
                        // setScrollTvShowToDay()
                    }
                    if (tipo == R.string.filmes_main) {
                        //setScrollMoviePopular()
                        //setScrollMovieOntheAir()
                    }
                }
            } catch (e: Exception) {
                Crashlytics.logException(e)
                if (activity != null)
                    Toast.makeText(activity, R.string.ops, Toast.LENGTH_SHORT).show()
            }

        }

    }

    companion object {

        fun newInstance(informacoes: Int): Fragment {
            val fragment = MainFragment()
            val bundle = Bundle()
            bundle.putInt(Constantes.ABA, informacoes)
            fragment.arguments = bundle

            return fragment
        }
    }
}
