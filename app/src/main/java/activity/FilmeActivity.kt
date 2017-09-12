package activity

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Dialog
import android.app.ProgressDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.*
import br.com.icaro.filme.R
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.database.*
import domain.*
import fragment.FilmeInfoFragment
import fragment.ImagemTopFilmeScrollFragment
import info.movito.themoviedbapi.TmdbMovies.MovieMethod.*
import info.movito.themoviedbapi.model.ArtworkType
import info.movito.themoviedbapi.model.MovieDb
import info.movito.themoviedbapi.model.core.MovieResultsPage
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import utils.Constantes
import utils.UtilsApp
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date


class FilmeActivity : BaseActivity() {
    internal var color_fundo: Int = 0
    private var viewPager: ViewPager? = null
    private var menu_item_favorite: FloatingActionButton? = null
    private var menu_item_watchlist: FloatingActionButton? = null
    private var menu_item_rated: FloatingActionButton? = null
    private var fab: FloatingActionMenu? = null
    private var id_filme: Int = 0
    private var progressBar: ProgressBar? = null
    private lateinit var movieDb: Movie
    private var addFavorite = true
    private var addWatch = true
    private var addRated = true
    private var similarMovies: MovieResultsPage? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var bundle: Bundle? = null
    private var mAuth: FirebaseAuth? = null

    private var myWatch: DatabaseReference? = null
    private var myFavorite: DatabaseReference? = null
    private var myRated: DatabaseReference? = null

    private var valueEventWatch: ValueEventListener? = null
    private var valueEventRated: ValueEventListener? = null
    private var valueEventFavorite: ValueEventListener? = null

    private var numero_rated: Float = 0.toFloat()
    //private var tmdvAsync: TMDVAsync? = null
    private var netflix: Netflix? = null
    private var imdbdb: Imdb? = null

    private var subscriptions = CompositeSubscription()
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private val client: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filme)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setUpToolBar()
        setupNavDrawer()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setTitle(" ")
        getExtras()
        //  Log.d("color", "Cor do fab " + color_fundo);
        menu_item_favorite = findViewById<View>(R.id.menu_item_favorite) as FloatingActionButton
        menu_item_watchlist = findViewById<View>(R.id.menu_item_watchlist) as FloatingActionButton
        menu_item_rated = findViewById<View>(R.id.menu_item_rated) as FloatingActionButton
        fab = findViewById<View>(R.id.fab_menu_filme) as FloatingActionMenu
        progressBar = findViewById<View>(R.id.progress) as ProgressBar
        viewPager = findViewById<View>(R.id.top_img_viewpager) as ViewPager
        viewPager!!.setBackgroundColor(color_fundo)
        viewPager!!.offscreenPageLimit = 3

        iniciarFirebases()
        subscriptions = CompositeSubscription()

        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                bundle = Bundle()
                bundle!!.putString(FirebaseAnalytics.Event.SELECT_CONTENT, "ViewPager")
                bundle!!.putInt(FirebaseAnalytics.Param.TRANSACTION_ID, viewPager!!.currentItem)
                FirebaseAnalytics.getInstance(this@FilmeActivity).logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        if (UtilsApp.isNetWorkAvailable(this)) {
            //tmdvAsync = TMDVAsync()
            //tmdvAsync!!.execute()

            val inscricaoMovie = API(context = this).loadMovieComVideo(18)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                       movieDb =  it
                        setTitle(movieDb.title)
                viewPager!!.adapter = ImagemTopFragment(supportFragmentManager)
                progressBar!!.visibility = View.INVISIBLE

                setFragmentInfo()

                if (mAuth!!.currentUser != null) { // Arrumar

                    setEventListenerFavorite()
                    setEventListenerRated()
                    setEventListenerWatch()

                    //  Log.d("FAB", "FAB " + color_fundo);
                    fab!!.alpha = 1f
                    setColorFab(color_fundo)
                    menu_item_favorite!!.setOnClickListener(addOrRemoveFavorite())
                    menu_item_rated!!.setOnClickListener(RatedFilme())
                    menu_item_watchlist!!.setOnClickListener(addOrRemoveWatch())
                } else {
                    fab!!.alpha = 0f
                }

                    }, { erro ->
                        Toast.makeText(this, getString(R.string.ops), Toast.LENGTH_LONG).show()
                        Log.d(javaClass.simpleName, "Erro " + erro.message)
                    })
            subscriptions.add(inscricaoMovie)

        } else {
            snack()
        }

    }

    private fun setEventListenerWatch() {

        valueEventWatch = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.child(id_filme.toString()).exists()) {
                    addWatch = true
                    //  Log.d(TAG, "False");
                    menu_item_watchlist!!.labelText = resources.getString(R.string.remover_watch)
                } else {
                    addWatch = false
                    // Log.d(TAG, "True");
                    menu_item_watchlist!!.labelText = resources.getString(R.string.adicionar_watch)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        myWatch!!.addValueEventListener(valueEventWatch)

    }

    private fun setEventListenerRated() {
        valueEventRated = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(id_filme.toString()).exists()) {
                    addRated = true
                    // Log.d(TAG, "False");
                    // Log.d(TAG, "nota " + dataSnapshot.child(String.valueOf(id_filme)).child("nota"));
                    if (dataSnapshot.child(id_filme.toString()).child("nota").exists()) {
                        val nota = dataSnapshot.child(id_filme.toString()).child("nota").value.toString()
                        numero_rated = java.lang.Float.parseFloat(nota)
                        menu_item_rated!!.labelText = resources.getString(R.string.remover_rated)
                        if (numero_rated == 0f) {
                            menu_item_rated!!.labelText = resources.getString(R.string.adicionar_rated)
                        }
                    }

                } else {
                    addRated = false
                    numero_rated = 0f
                    menu_item_rated!!.labelText = resources.getString(R.string.adicionar_rated)
                    //  Log.d(TAG, "True");
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        myRated!!.addValueEventListener(valueEventRated)

    }

    private fun setEventListenerFavorite() {
        valueEventFavorite = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(id_filme.toString()).exists()) {
                    addFavorite = true
                    //  Log.d(TAG, "True");
                    menu_item_favorite!!.labelText = resources.getString(R.string.remover_favorite)
                } else {
                    addFavorite = false
                    // Log.d(TAG, "False");
                    menu_item_favorite!!.labelText = resources.getString(R.string.adicionar_favorite)

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        myFavorite!!.addValueEventListener(valueEventFavorite)
    }

    private fun iniciarFirebases() {

        mAuth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()

        if (mAuth!!.currentUser != null) {

            myWatch = database.getReference("users").child(mAuth!!.currentUser!!
                    .uid).child("watch")
                    .child("movie")

            myFavorite = database.getReference("users").child(mAuth!!.currentUser!!
                    .uid).child("favorites")
                    .child("movie")

            myRated = database.getReference("users").child(mAuth!!.currentUser!!
                    .uid).child("rated")
                    .child("movie")
        }
    }

    private fun getExtras() {
        if (intent.action == null) {
            id_filme = intent.getIntExtra(Constantes.FILME_ID, 0)
            color_fundo = intent.getIntExtra(Constantes.COLOR_TOP, R.color.transparent)
        } else {
            id_filme = Integer.parseInt(intent.getStringExtra(Constantes.FILME_ID))
            color_fundo = Integer.parseInt(intent.getStringExtra(Constantes.COLOR_TOP))

        }
    }

    protected fun snack() {
        Snackbar.make(viewPager!!, R.string.no_internet, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry) {
                    if (UtilsApp.isNetWorkAvailable(baseContext)) {
                        //TMDVAsync().execute()
                    } else {
                        snack()
                    }
                }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        try {
            menuInflater.inflate(R.menu.menu_share, menu)

            val searchView = menu.findItem(R.id.search).actionView as SearchView
            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.queryHint = resources.getString(R.string.procurar)

            searchView.isEnabled = false
        } catch (e: Exception) {
            FirebaseCrash.report(e)
            Toast.makeText(this, R.string.ops, Toast.LENGTH_SHORT).show()
        }

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (movieDb != null) {
            if (item.itemId == R.id.share) {

                salvaImagemMemoriaCache(this@FilmeActivity, movieDb!!.posterPath, object : BaseActivity.SalvarImageShare {
                    override fun retornaFile(file: File) {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "message/rfc822"
                        intent.putExtra(Intent.EXTRA_TEXT, movieDb!!.title + " " + buildDeepLink() + " by: " + Constantes.TWITTER_URL)
                        intent.type = "image/*"
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                        startActivity(Intent.createChooser(intent, resources.getString(R.string.compartilhar_filme)))

                        bundle = Bundle()
                        bundle!!.putString(FirebaseAnalytics.Event.SELECT_CONTENT, "NavDrawer_MainActivity:menu_drav_home")
                        mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                        //   Log.d(TAG, "TRUE");

                    }

                    override fun RetornoFalha() {
                        Toast.makeText(this@FilmeActivity, resources.getString(R.string.erro_na_gravacao_imagem), Toast.LENGTH_SHORT).show()
                    }
                })

            }
        } else {
            //  Log.d(TAG, "ELSE");
            Toast.makeText(this@FilmeActivity, resources.getString(R.string.erro_ainda_sem_imagem), Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
    }

    fun buildDeepLink(): String {
        // Get the unique appcode for this app.

        val link = "https://q2p5q.app.goo.gl/?link=https://br.com.icaro.filme/?action%3DFA%26id%3D"
        (movieDb.id).toString() + "&apn=br.com.icaro.filme"

        // If the deep link is used in an advertisement, this value must be set to 1.
        val isAd = false
        if (isAd) {
            // builder.appendQueryParameter("ad", "1");
        }

        // Minimum version is optional.
        //        int minVersion = ;
        //        if (minVersion > 16) {
        //            builder.appendQueryParameter("amv", Integer.toString(minVersion));
        //        }

        //        if (!TextUtils.isEmpty(androidLink)) {
        //            builder.appendQueryParameter("al", androidLink);
        //        }
        //
        //        if (!TextUtils.isEmpty(playStoreAppLink)) {
        //            builder.appendQueryParameter("afl", playStoreAppLink);
        //        }
        //
        //        if (!customParameters.isEmpty()) {
        //            for (Map.Entry<String, String> parameter : customParameters.entrySet()) {
        //                builder.appendQueryParameter(parameter.getKey(), parameter.getValue());
        //            }
        //        }

        // Return the completed deep link.
        //        Log.d(TAG, builder.build().toString());
        //        return builder.build().toString();
        return link
    }


    fun RatedFilme(): View.OnClickListener {
        return View.OnClickListener {
            var date: Date? = null
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                date = sdf.parse(movieDb!!.releaseDate)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (!UtilsApp.verificaLancamento(date)) {
                bundle = Bundle()
                bundle!!.putString(FirebaseAnalytics.Event.SELECT_CONTENT, "Tentativa de Rated fora da data de lançamento")
                mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                Toast.makeText(this@FilmeActivity, getString(R.string.filme_nao_lancado), Toast.LENGTH_SHORT).show()
            } else {
                val alertDialog = Dialog(this@FilmeActivity)
                alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                alertDialog.setContentView(R.layout.adialog_custom_rated)

                val ok = alertDialog.findViewById<View>(R.id.ok_rated) as Button
                val no = alertDialog.findViewById<View>(R.id.cancel_rated) as Button
                val title = alertDialog.findViewById<View>(R.id.rating_title) as TextView
                title.text = movieDb!!.title
                val ratingBar = alertDialog.findViewById<View>(R.id.ratingBar_rated) as RatingBar
                ratingBar.rating = numero_rated
                val width = resources.getDimensionPixelSize(R.dimen.popup_width) //Criar os Dimen do layout do login - 300dp - 300dp ??
                val height = resources.getDimensionPixelSize(R.dimen.popup_height_rated)

                alertDialog.window!!.setLayout(width, height)//??????????????
                alertDialog.show()

                if (addRated) {
                    no.visibility = View.VISIBLE
                } else {
                    no.visibility = View.GONE
                }

                no.setOnClickListener {
                    // Log.d(TAG, "Apagou Rated");
                    myRated!!.child(id_filme.toString()).setValue(null)
                            .addOnCompleteListener {
                                Toast.makeText(this@FilmeActivity,
                                        resources.getText(R.string.remover_rated), Toast.LENGTH_SHORT).show()
                            }
                    alertDialog.dismiss()
                    fab!!.close(true)
                }

                ok.setOnClickListener(View.OnClickListener {
                    //  Log.d(TAG, "Adialog Rated");

                    val progressDialog = ProgressDialog(this@FilmeActivity,
                            android.R.style.Theme_Material_Dialog)
                    progressDialog.isIndeterminate = true
                    progressDialog.setMessage(resources.getString(R.string.salvando))
                    progressDialog.show()

                    if (UtilsApp.isNetWorkAvailable(this@FilmeActivity)) {

                        if (ratingBar.rating == 0f) {
                            progressDialog.dismiss()
                            alertDialog.dismiss()
                            return@OnClickListener
                        }

                        val filmeDB = FilmeDB()
                        filmeDB.id = movieDb.id!!
                        filmeDB.idImdb = movieDb.imdbId
                        filmeDB.title = movieDb.title
                        filmeDB.nota = ratingBar.rating.toInt()
                        filmeDB.poster = movieDb.posterPath

                        myRated!!.child(id_filme.toString()).setValue(filmeDB)
                                .addOnCompleteListener {
                                    Toast.makeText(this@FilmeActivity, resources.getString(R.string.filme_rated), Toast.LENGTH_SHORT)
                                            .show()
                                    bundle = Bundle()
                                    bundle!!.putString(FirebaseAnalytics.Event.SELECT_CONTENT, resources
                                            .getString(R.string.filme_rated))
                                    bundle!!.putString(FirebaseAnalytics.Param.ITEM_NAME, movieDb!!.title)
                                    bundle!!.putInt(FirebaseAnalytics.Param.ITEM_ID, movieDb.id!!)
                                    mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                                    fab!!.close(true)
                                }
                        Thread(Runnable { FilmeService.ratedMovieGuest(id_filme, ratingBar.rating.toInt(), this@FilmeActivity) }).start()


                    }
                    progressDialog.dismiss()
                    alertDialog.dismiss()
                })

            }
        }
    }

    private fun setColorFab(color: Int) {
        fab!!.menuButtonColorNormal = color
        menu_item_favorite!!.colorNormal = color
        menu_item_watchlist!!.colorNormal = color
        menu_item_rated!!.colorNormal = color
    }

    private fun addOrRemoveFavorite(): View.OnClickListener {
        return View.OnClickListener {
            val anim1 = PropertyValuesHolder.ofFloat("scaleX", 1f, 0.2f)
            val anim2 = PropertyValuesHolder.ofFloat("scaley", 1f, 0.2f)
            val anim3 = PropertyValuesHolder.ofFloat("scaleX", 0f, 1f)
            val anim4 = PropertyValuesHolder.ofFloat("scaley", 0f, 1f)
            val animator = ObjectAnimator
                    .ofPropertyValuesHolder(menu_item_favorite, anim1, anim2, anim3, anim4)
            animator.duration = 1600
            animator.start()


            var date: Date? = null
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                date = sdf.parse(movieDb!!.releaseDate)
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (!UtilsApp.verificaLancamento(date)) {
                Toast.makeText(this@FilmeActivity, R.string.filme_nao_lancado, Toast.LENGTH_SHORT).show()
                bundle = Bundle()
                bundle!!.putString(FirebaseAnalytics.Event.SELECT_CONTENT, "Favorite - FILME ainda não foi lançado.")
                mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
            } else {

                if (addFavorite) {
                    //  Log.d(TAG, "Apagou Favorite");
                    myFavorite!!.child(id_filme.toString()).setValue(null)
                            .addOnCompleteListener {
                                Toast.makeText(this@FilmeActivity, getString(R.string.filme_remove_favorite), Toast.LENGTH_SHORT).show()
                                bundle = Bundle()
                                bundle!!.putString(FirebaseAnalytics.Event.SELECT_CONTENT, getString(R.string.filme_add_favorite))
                                bundle!!.putString(FirebaseAnalytics.Param.ITEM_NAME, movieDb!!.title)
                                bundle!!.putInt(FirebaseAnalytics.Param.ITEM_ID, movieDb.id!!)
                                mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                                fab!!.close(true)
                            }
                } else {

                    val filmeDB = FilmeDB()
                    filmeDB.id = movieDb.id!!
                    filmeDB.idImdb = movieDb.imdbId
                    filmeDB.title = movieDb!!.title
                    filmeDB.poster = movieDb!!.posterPath

                    myFavorite!!.child(id_filme.toString()).setValue(filmeDB)
                            .addOnCompleteListener {
                                Toast.makeText(this@FilmeActivity, getString(R.string.filme_add_favorite), Toast.LENGTH_SHORT)
                                        .show()
                                bundle = Bundle()
                                bundle!!.putString(FirebaseAnalytics.Event.SELECT_CONTENT, getString(R.string.filme_remove_favorite))
                                bundle!!.putString(FirebaseAnalytics.Param.ITEM_NAME, movieDb!!.title)
                                bundle!!.putInt(FirebaseAnalytics.Param.ITEM_ID, movieDb.id!!)
                                mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                                fab!!.close(true)
                            }
                }
            }
        }
    }

    private fun addOrRemoveWatch(): View.OnClickListener {
        return View.OnClickListener {
            val anim1 = PropertyValuesHolder.ofFloat("scaleX", 1f, 0f)
            val anim2 = PropertyValuesHolder.ofFloat("scaley", 1f, 0f)
            val anim3 = PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1f)
            val anim4 = PropertyValuesHolder.ofFloat("scaley", 0.5f, 1f)
            val animator = ObjectAnimator
                    .ofPropertyValuesHolder(menu_item_watchlist, anim1, anim2, anim3, anim4)
            animator.duration = 1650
            animator.start()

            if (addWatch) {

                myWatch!!.child(id_filme.toString()).setValue(null)
                        .addOnCompleteListener {
                            Toast.makeText(this@FilmeActivity, getString(R.string.filme_remove), Toast.LENGTH_SHORT).show()
                            bundle = Bundle()
                            bundle!!.putString(FirebaseAnalytics.Event.SELECT_CONTENT, getString(R.string.filme_remove))
                            bundle!!.putString(FirebaseAnalytics.Param.ITEM_NAME, movieDb!!.title)
                            bundle!!.putInt(FirebaseAnalytics.Param.ITEM_ID, movieDb.id!!)
                            mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                            fab!!.close(true)
                        }


            } else {

                val filmeDB = FilmeDB()
                filmeDB.idImdb = movieDb?.imdbId
                filmeDB.id = movieDb.id!!
                filmeDB.title = movieDb!!.title
                filmeDB.poster = movieDb!!.posterPath

                myWatch!!.child(id_filme.toString()).setValue(filmeDB)
                        .addOnCompleteListener {
                            Toast.makeText(this@FilmeActivity, getString(R.string.filme_add_watchlist), Toast.LENGTH_SHORT)
                                    .show()
                            bundle = Bundle()
                            bundle!!.putString(FirebaseAnalytics.Event.SELECT_CONTENT, getString(R.string.filme_add_watchlist))
                            bundle!!.putString(FirebaseAnalytics.Param.ITEM_NAME, movieDb!!.title)
                            bundle!!.putInt(FirebaseAnalytics.Param.ITEM_ID, movieDb.id!!)
                            mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                            fab!!.close(true)
                        }

            }
        }
    }

    private fun setTitle(title: String) {
        val collapsingToolbarLayout = findViewById<View>(R.id.collapsing_toolbar) as CollapsingToolbarLayout
        collapsingToolbarLayout.title = title
    }

    private fun setFragmentInfo() {

        val filmeFrag = FilmeInfoFragment()
        val bundle = Bundle()
        bundle.putSerializable(Constantes.FILME, movieDb)
        bundle.putSerializable(Constantes.SIMILARES, similarMovies)
        bundle.putSerializable(Constantes.NETFLIX, netflix)
        bundle.putSerializable(Constantes.IMDB, imdbdb)
        filmeFrag.arguments = bundle
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (!isDestroyed && !isFinishing /*&& tmdvAsync != null*/) { //Isdestroyed valido apenas acima desta api
                supportFragmentManager
                        .beginTransaction()
                        .add(R.id.filme_container, filmeFrag, null)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commitAllowingStateLoss()
            }
        } else {
            if (!isFinishing /*&& tmdvAsync != null*/) {
                supportFragmentManager
                        .beginTransaction()
                        .add(R.id.filme_container, filmeFrag, null)
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .commitAllowingStateLoss()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

//        if (tmdvAsync != null) {
//            tmdvAsync!!.cancel(true)
//        }

        if (valueEventWatch != null) {
            myWatch!!.removeEventListener(valueEventWatch!!)
        }
        if (valueEventRated != null) {
            myRated!!.removeEventListener(valueEventRated!!)
        }
        if (valueEventFavorite != null) {
            myFavorite!!.removeEventListener(valueEventFavorite!!)
        }

        subscriptions.clear()
    }

    private inner class ImagemTopFragment(supportFragmentManager: FragmentManager) : FragmentPagerAdapter(supportFragmentManager) {

        override fun getItem(position: Int): Fragment? {
            return if (movieDb.images?.backdrops != null) {
                if (position == 0) {
                    ImagemTopFilmeScrollFragment.newInstance(movieDb.backdropPath)
                } else ImagemTopFilmeScrollFragment.newInstance(movieDb.images?.backdrops!![position]?.filePath)
                // Log.d("FilmeActivity", "getItem: ->  " + movieDb.getImages(ArtworkType.BACKDROP).get(position).getFilePath());
            } else null


        }


        override fun getCount(): Int {
            if (movieDb.images?.backdrops != null) {

                val tamanho = movieDb.images?.backdrops?.size
                // Log.d("FilmeActivity", "getCount: ->  " + tamanho);
                return if (tamanho!! > 0) tamanho else 1 // ???????????????? tamahao vai ser nulo?
            }

            return 0
        }
    }


//    private inner class TMDVAsync : AsyncTask<Void, Void, MovieDb>() {
//
//        override fun doInBackground(vararg voids: Void): MovieDb? {//
//            if (UtilsApp.isNetWorkAvailable(this@FilmeActivity)) {
//                var idioma_padrao = false
//                try {
//                    val sharedPref = PreferenceManager.getDefaultSharedPreferences(this@FilmeActivity)
//                    idioma_padrao = sharedPref.getBoolean(SettingsActivity.PREF_IDIOMA_PADRAO, true)
//                } catch (e: Exception) {
//                    FirebaseCrash.report(e)
//                }
//
//                try {
//                    val movies = FilmeService.getTmdbMovies()
//
//                    if (idioma_padrao) {
//                        movieDb = movies.getMovie(id_filme, BaseActivity.getLocale() + ",en,null", credits, releases, videos, similar, alternative_titles, images)
//                        movieDb!!.videos.addAll(movies.getMovie(id_filme, "en", videos).videos)
//                    } else {
//                        movieDb = movies.getMovie(id_filme, "en", credits, releases, videos, similar, alternative_titles, images)
//                    }
//                    similarMovies = movies.getSimilarMovies(movieDb!!.id, null, 1)
//
//
//                } catch (e: Exception) {
//                    FirebaseCrash.report(e)
//                    //if (isDestroyed)
//                        runOnUiThread { Toast.makeText(this@FilmeActivity, R.string.ops, Toast.LENGTH_SHORT).show() }
//                }
//
//                try {
//                    if (movieDb!!.imdbID != null) {
//                        imdbdb = FilmeService.getImdb(movieDb!!.imdbID)
//                    }
//                } catch (e: Exception) {
//                    FirebaseCrash.report(e)
//                    //if (!isDestroyed)
//                        runOnUiThread { Toast.makeText(this@FilmeActivity, R.string.ops, Toast.LENGTH_SHORT).show() }
//                }
//
//                try {
//                    if (movieDb != null) {
//
//                        if (movieDb!!.releaseDate != null) {
//                            if (movieDb!!.releaseDate.length >= 4) {
//                                val date = movieDb!!.releaseDate.substring(0, 4)
//                                netflix = FilmeService.getNetflix(movieDb!!.title, Integer.parseInt(date))
//                            }
//                        }
//                    }
//                } catch (e: Exception) {
//                    FirebaseCrash.report(e)
//                    if (!isFinishing)
//                        runOnUiThread { Toast.makeText(this@FilmeActivity, R.string.ops, Toast.LENGTH_SHORT).show() }
//                }
//
//            }
//            return movieDb
//        }
//
//        override fun onPostExecute(movieDb: MovieDb?) {
//            super.onPostExecute(movieDb)
//            if (movieDb != null) {
//                setTitle(movieDb.title)
//                viewPager!!.adapter = ImagemTopFragment(supportFragmentManager)
//                progressBar!!.visibility = View.INVISIBLE
//
//                setFragmentInfo()
//
//                if (mAuth!!.currentUser != null) { // Arrumar
//
//                    setEventListenerFavorite()
//                    setEventListenerRated()
//                    setEventListenerWatch()
//
//                    //  Log.d("FAB", "FAB " + color_fundo);
//                    fab!!.alpha = 1f
//                    setColorFab(color_fundo)
//                    menu_item_favorite!!.setOnClickListener(addOrRemoveFavorite())
//                    menu_item_rated!!.setOnClickListener(RatedFilme())
//                    menu_item_watchlist!!.setOnClickListener(addOrRemoveWatch())
//                } else {
//                    fab!!.alpha = 0f
//                }
//            }
//        }
//    }
}