package listafilmes.fragment


import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import br.com.icaro.filme.R
import com.google.firebase.analytics.FirebaseAnalytics
import domain.API
import fragment.FragmentBase
import kotlinx.android.synthetic.main.fragment_list_filme.*
import listafilmes.adapter.ListaFilmesAdapter
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import utils.Constantes
import utils.InfiniteScrollListener
import utils.UtilsApp
import utils.getIdiomaEscolhido


/**
 * A simple [Fragment] subclass.
 */
class FilmesFragment : FragmentBase() {

    private var abaEscolhida: Int = 0
    private var pagina = 1
    private var totalPagina: Int? = 0
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            if (activity.intent.action == null) {
                this.abaEscolhida = arguments.getInt(Constantes.NAV_DRAW_ESCOLIDO)
            } else {
                this.abaEscolhida = Integer.parseInt(arguments.getString(Constantes.NAV_DRAW_ESCOLIDO))
            }
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_list_filme, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recycle_listas.apply {
            val gridLayout = GridLayoutManager(activity, 2)
            layoutManager = gridLayout
            itemAnimator = DefaultItemAnimator()
            addOnScrollListener(InfiniteScrollListener({ getListaFilmes() }, gridLayout))
            setHasFixedSize(true)
            adapter = ListaFilmesAdapter(activity)
        }

        if (!UtilsApp.isNetWorkAvailable(context)) {
            txt_listas?.visibility = View.VISIBLE
            txt_listas?.text = "SEM INTERNET"
            snack()

        } else {
            getListaFilmes()
        }
    }


    fun getListaFilmes() {

        val inscricao = API(context).BuscaDeFilmes(getTipo(), pagina = pagina, local = getIdiomaEscolhido(context))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    (recycle_listas.adapter as ListaFilmesAdapter).addFilmes(it.results, it?.totalResults!!)
                    pagina = it?.page!!
                    totalPagina = it?.totalPages!!
                    ++pagina
                }, { erro ->
                    Toast.makeText(context, getString(R.string.ops), Toast.LENGTH_LONG).show()
                    Log.d(javaClass.simpleName, "Erro " + erro.message)
                })

        subscriptions.add(inscricao)

    }

    protected fun snack() {
        Snackbar.make(frame_list_filme!!, R.string.no_internet, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry) {
                    if (UtilsApp.isNetWorkAvailable(context)) {
                        txt_listas!!.visibility = View.INVISIBLE
                        //   val tmdvAsync = TMDVAsync()
                        //   tmdvAsync.execute()
                    } else {
                        snack()
                    }
                }.show()
    }

//    private fun onClickListener(): FilmesAdapter.FilmeOnClickListener {
//        return FilmesAdapter.FilmeOnClickListener { view, position ->
//            //  Log.d("onClickMovieListener", "" + position);
//            // Log.d("onClickMovieListener", "" + movies.get(position).getTitle());
//            val intent = Intent(activity, FilmeActivity::class.java)
//            intent.putExtra(Constantes.COLOR_TOP, UtilsApp.loadPalette(view))
//            intent.putExtra(Constantes.FILME_ID, movies!![position].id)
//            intent.putExtra(Constantes.NOME_FILME, movies!![position].title)
//            context.startActivity(intent)
//        }
//    }

    fun getTipo(): String? {

        when (abaEscolhida) {

            R.string.now_playing -> return API.TIPOBUSCA.FILME.agora

            R.string.upcoming -> {
                return API.TIPOBUSCA.FILME.chegando
            }


            R.string.populares -> {
                return API.TIPOBUSCA.FILME.popular
            }

            R.string.top_rated -> {
                return API.TIPOBUSCA.FILME.melhores
            }
        }
        return null
    }

//    private inner class TMDVAsync : AsyncTask<Void, Void, List<MovieDb>>() {
//
//
//        override fun doInBackground(vararg voids: Void): List<MovieDb>? {
//            //  Log.d("doInBackground", "doInBackground");
//            try {
//                val movies = FilmeService.getTmdbMovies()
//                return getTipo(movies)
//            } catch (e: Exception) {
//                // Log.d(TAG, e.getMessage());
//                FirebaseCrash.report(e)
//                if (context != null)
//                    activity.runOnUiThread { Toast.makeText(context, R.string.ops, Toast.LENGTH_SHORT).show() }
//            }
//
//            return null
//        }
//
//
//        override fun onPostExecute(tmdbMovies: List<MovieDb>?) {
//            // Log.d("onPostExecute", "onPostExecute");
//            process!!.visibility = View.GONE
//            if (tmdbMovies != null && pagina != 1) {
//                val x = movies
//                movies = tmdbMovies
//                for (movie in x!!) {
//                    movies!!.add(movie)
//                }
//            } else {
//                movies = tmdbMovies
//            }
//
//            recycleView!!.adapter = FilmesAdapter(context, if (movies != null) movies else null,
//                    onClickListener())
//            pagina++
//        }
//
//    }

}
