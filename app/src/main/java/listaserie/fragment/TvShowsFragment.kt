package listaserie.fragment

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import br.com.icaro.filme.R
import domain.Api
import fragment.FragmentBase
import kotlinx.android.synthetic.main.fragment_list_filme.*
import listaserie.adapter.ListaSeriesAdapter
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import utils.Constantes
import utils.InfiniteScrollListener
import utils.UtilsApp
import utils.getIdiomaEscolhido

/**
 * Created by icaro on 14/09/16.
 */
class TvShowsFragment : FragmentBase() {

    private var abaEscolhida: Int? = null
    private var pagina = 1
    private var totalPagina: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            this.abaEscolhida = arguments?.getInt(Constantes.NAV_DRAW_ESCOLIDO)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_list_filme, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        recycle_listas.apply {
            val gridLayout = GridLayoutManager(activity, 2)
            layoutManager = gridLayout
            itemAnimator = DefaultItemAnimator()
            addOnScrollListener(InfiniteScrollListener( {getListaSereies()} , gridLayout))
            setHasFixedSize(true)
            adapter = ListaSeriesAdapter(context)
        }

        if (!UtilsApp.isNetWorkAvailable(context)) {
            txt_listas.visibility = View.VISIBLE
            txt_listas.text = resources.getString(R.string.no_internet)
            snack()

        } else {
            getListaSereies()
        }
    }

    private fun snack() {
        Snackbar.make(frame_list_filme, R.string.no_internet, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry) {
                    if (UtilsApp.isNetWorkAvailable(context)) {
                        txt_listas.visibility = View.INVISIBLE
                        getListaSereies()
                    } else {
                        snack()
                    }
                }.show()
    }


    fun getListaSereies() {

        val inscricao = Api(context!!)
                .buscaDeSeries(getListaTipo(), pagina = pagina, local = getIdiomaEscolhido(context!!))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (view != null) {
                        (recycle_listas.adapter as ListaSeriesAdapter).addSeries(it.results, it?.totalResults!!)
                        pagina = it.page!!
                        totalPagina = it.totalPages!!
                        ++pagina
                    }
                }, { erro ->
                    if (view != null) {
                        Toast.makeText(context, getString(R.string.ops), Toast.LENGTH_LONG).show()
                    }
                })

        subscriptions.add(inscricao)
    }

    fun getListaTipo(): String? {

        when (abaEscolhida) {

            R.string.air_date -> return Api.TIPOBUSCA.SERIE.semana

            R.string.today -> return Api.TIPOBUSCA.SERIE.hoje

            R.string.populares -> return Api.TIPOBUSCA.SERIE.popular

            R.string.top_rated -> return Api.TIPOBUSCA.SERIE.melhores
        }
        return null
    }
}

