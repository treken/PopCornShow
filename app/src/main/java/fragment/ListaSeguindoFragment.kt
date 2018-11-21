package fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.crashlytics.android.Crashlytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.io.Serializable
import java.util.ArrayList

import adapter.ProximosAdapter
import adapter.SeguindoRecycleAdapter
import br.com.icaro.filme.R
import domain.UserEp
import domain.UserSeasons
import domain.UserTvshow
import utils.Constantes

/**
 * Created by icaro on 25/11/16.
 */
class ListaSeguindoFragment : Fragment() {

    private val TAG = ListaSeguindoFragment::class.java.name
    private var userTvshows: MutableList<UserTvshow>? = null
    private var tipo: Int = 0
    private var recyclerViewMissing: RecyclerView? = null
    private var recyclerViewSeguindo: RecyclerView? = null
    private var eventListener: ValueEventListener? = null
    private var seguindoDataBase: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            tipo = arguments!!.getInt(Constantes.ABA)
            userTvshows = arguments!!.getSerializable(Constantes.SEGUINDO) as MutableList<UserTvshow>
        }

        val mAuth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        seguindoDataBase = database.getReference("users").child(mAuth.currentUser!!
                .uid).child("seguindo")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        when (tipo) {

            0 -> {
                return getViewMissing(inflater, container)

            }
            1 -> {
                return getViewSeguindo(inflater, container)
            }
        }
        return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userTvshows = ArrayList()
                if (dataSnapshot.exists()) {

                    for (snapshot in dataSnapshot.children) {
                        try {
                            val userTvshow = snapshot.getValue(UserTvshow::class.java)
                            userTvshows!!.add(userTvshow!!)
                        } catch (e: Exception) {
                            Crashlytics.logException(e)
                            if (snapshot.hasChild("nome") && activity != null) {
                                val nome = snapshot.child("nome").getValue(String::class.java)
                                Toast.makeText(activity, resources.getString(R.string.ops_seguir_novamente) + " - " + nome, Toast.LENGTH_LONG).show()
                            } else {
                                if (activity != null) {
                                    Toast.makeText(activity, resources.getString(R.string.ops_seguir_novamente), Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                    }
                    if (getView() != null) {
                        recyclerViewMissing = getView()!!.rootView.findViewById<View>(R.id.temporadas_recycle) as RecyclerView
                        recyclerViewSeguindo = getView()!!.rootView.findViewById<View>(R.id.seguindo_recycle) as RecyclerView
                        recyclerViewMissing!!.adapter = ProximosAdapter(activity, setSeriesMissing(userTvshows!!))
                        recyclerViewSeguindo!!.adapter = SeguindoRecycleAdapter(activity, userTvshows)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        seguindoDataBase!!.addValueEventListener(eventListener)

    }

    private fun setSeriesMissing(userTvshows: List<UserTvshow>): List<UserTvshow> {
        val temp = ArrayList<UserTvshow>()

        for (userTvshow in userTvshows) {
            var season = true
            for (seasons in userTvshow.seasons) {
                if (seasons.seasonNumber != 0 && seasons.userEps != null && season)
                    for (userEp in seasons.userEps) {
                        if (!userEp.isAssistido) {
                            temp.add(userTvshow)
                            season = false
                            break
                        }
                    }
            }
        }// gambiara. Arrumar!

        return temp
    }

    private fun getViewMissing(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.temporadas, container, false) // Criar novo layout
        view.findViewById<View>(R.id.progressBarTemporadas).visibility = View.GONE
        recyclerViewMissing = view.findViewById<View>(R.id.temporadas_recycle) as RecyclerView
        recyclerViewMissing!!.setHasFixedSize(true)
        recyclerViewMissing!!.itemAnimator = DefaultItemAnimator()
        recyclerViewMissing!!.layoutManager = LinearLayoutManager(context)
        val missing = setSeriesMissing(userTvshows!!)
        if (missing.size > 0) {
            recyclerViewMissing!!.adapter = ProximosAdapter(activity, missing)
        } else {
            view.findViewById<View>(R.id.text_search_empty).visibility = View.VISIBLE
            (view.findViewById<View>(R.id.text_search_empty) as TextView).setText(R.string.empty)
        }
        return view
    }

    private fun getViewSeguindo(inflater: LayoutInflater, container: ViewGroup?): View {
        val view = inflater.inflate(R.layout.seguindo, container, false) // Criar novo layout
        view.findViewById<View>(R.id.progressBarTemporadas).visibility = View.GONE
        recyclerViewSeguindo = view.findViewById<View>(R.id.seguindo_recycle) as RecyclerView
        recyclerViewSeguindo!!.setHasFixedSize(true)
        recyclerViewSeguindo!!.itemAnimator = DefaultItemAnimator()
        recyclerViewSeguindo!!.layoutManager = GridLayoutManager(context, 4)
        if (userTvshows!!.size > 0) {
            recyclerViewSeguindo!!.adapter = SeguindoRecycleAdapter(activity, userTvshows)
        } else {
            view.findViewById<View>(R.id.text_search_empty).visibility = View.VISIBLE
            (view.findViewById<View>(R.id.text_search_empty) as TextView).setText(R.string.empty)
        }
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        if (eventListener != null) {
            seguindoDataBase!!.removeEventListener(eventListener!!)
        }
    }

    companion object {

        fun newInstance(tipo: Int, userTvshows: List<UserTvshow>): Fragment {
            val fragment = ListaSeguindoFragment()
            val bundle = Bundle()
            bundle.putSerializable(Constantes.SEGUINDO, userTvshows as Serializable)
            bundle.putInt(Constantes.ABA, tipo)
            fragment.arguments = bundle

            return fragment
        }
    }
}
