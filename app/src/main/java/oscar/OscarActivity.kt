package oscar

import activity.BaseActivity
import adapter.ListUserAdapter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import br.com.icaro.filme.R
import domain.Api
import kotlinx.android.synthetic.main.activity_lista.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import utils.InfiniteScrollListener
import utils.UtilsApp

/**
 * Created by icaro on 04/10/16.
 */
class OscarActivity : BaseActivity() {

    private val list_id = "28" // Id da lista com Ganhadores do Oscar 28
    private var pagina = 1
    private var totalPagina = 1

    private var subscriptions = CompositeSubscription()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setUpToolBar()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.oscar)

        recycleView_favorite.apply {
            val gridLayout = GridLayoutManager(this@OscarActivity, 3)
            layoutManager = gridLayout
            itemAnimator = DefaultItemAnimator()
            addOnScrollListener(InfiniteScrollListener({ getOscar() }, gridLayout))
            setHasFixedSize(true)
            recycleView_favorite.adapter = ListUserAdapter(this@OscarActivity)
        }
    }


    override fun onResume() {
        super.onResume()
        subscriptions = CompositeSubscription()
        if (UtilsApp.isNetWorkAvailable(baseContext)) {
            getOscar()
        } else {
            snack()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        subscriptions.clear()
    }

    protected fun snack() {
        Snackbar.make(linear_lista, R.string.no_internet, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry) {
                    if (UtilsApp.isNetWorkAvailable(baseContext)) {
                        getOscar()
                    } else {
                        snack()
                    }
                }.show()
    }

    private fun getOscar() {

        val teste = Api(context = this).loadMovieComVideo(18)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                   it.id

                }, { erro ->
                    Toast.makeText(this, getString(R.string.ops), Toast.LENGTH_LONG).show()
                    Log.d(javaClass.simpleName, "Erro " + erro.message)
                })

        if (totalPagina >= pagina) {
            val inscricao = Api(context = this).getLista(id = list_id, pagina = pagina)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        (recycleView_favorite.adapter as ListUserAdapter).addItens(it.results, it?.totalResults!!)
                        pagina = it?.page!!
                        totalPagina = it?.totalPages!!
                        ++pagina
                    }, { erro ->
                        Toast.makeText(this, getString(R.string.ops), Toast.LENGTH_LONG).show()
                        Log.d(javaClass.simpleName, "Erro " + erro.message)
                    })
            subscriptions.add(inscricao)
            subscriptions.add(teste)
        }

    }


}
