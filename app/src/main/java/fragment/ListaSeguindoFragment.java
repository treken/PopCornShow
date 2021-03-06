package fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import adapter.ProximosAdapter;
import adapter.SeguindoRecycleAdapter;
import br.com.icaro.filme.R;
import domain.UserEp;
import domain.UserSeasons;
import domain.UserTvshow;
import utils.Constantes;

/**
 * Created by icaro on 25/11/16.
 */
public class ListaSeguindoFragment extends Fragment {

    private final String TAG = ListaSeguindoFragment.class.getName();
    private List<UserTvshow> userTvshows;
    private int tipo;
    private RecyclerView recyclerViewMissing;
    private RecyclerView recyclerViewSeguindo;
    private ValueEventListener eventListener;
    private DatabaseReference seguindoDataBase;

    public static Fragment newInstance(int tipo, List<UserTvshow> userTvshows) {
        ListaSeguindoFragment fragment = new ListaSeguindoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constantes.INSTANCE.getSEGUINDO(), (Serializable) userTvshows);
        bundle.putInt(Constantes.INSTANCE.getABA(), tipo);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tipo = getArguments().getInt(Constantes.INSTANCE.getABA());
            userTvshows = (List<UserTvshow>) getArguments().getSerializable(Constantes.INSTANCE.getSEGUINDO());
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        seguindoDataBase = database.getReference("users").child(mAuth.getCurrentUser()
                .getUid()).child("seguindo");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        switch (tipo) {

            case 0: {
                return getViewMissing(inflater, container);

            }
            case 1: {
                return getViewSeguindo(inflater, container);
            }
        }
        return null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userTvshows = new ArrayList<>();
                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            UserTvshow userTvshow = snapshot.getValue(UserTvshow.class);
                            userTvshows.add(userTvshow);
                        } catch (Exception e) {
                            Crashlytics.logException(e);
                            if (snapshot.hasChild("nome") && getActivity() != null) {
                                String nome = snapshot.child("nome").getValue(String.class);
                                Toast.makeText(getActivity(), getResources().getString(R.string.ops_seguir_novamente) + " - " + nome, Toast.LENGTH_LONG).show();
                            } else {
                                if (getActivity() != null) {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.ops_seguir_novamente), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                    if (getView() != null) {
                        recyclerViewMissing = (RecyclerView) getView().getRootView().findViewById(R.id.temporadas_recycle);
                        recyclerViewSeguindo = (RecyclerView) getView().getRootView().findViewById(R.id.seguindo_recycle);
                        recyclerViewMissing.setAdapter(new ProximosAdapter(getActivity(), setSeriesMissing(userTvshows)));
                        recyclerViewSeguindo.setAdapter(new SeguindoRecycleAdapter(getActivity(), userTvshows));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        seguindoDataBase.addValueEventListener(eventListener);

    }

    private List<UserTvshow> setSeriesMissing(List<UserTvshow> userTvshows) {
        List<UserTvshow> temp = new ArrayList<>();

        for (UserTvshow userTvshow : userTvshows) {
            boolean season = true;
            for (UserSeasons seasons : userTvshow.getSeasons()) {
                if (seasons.getSeasonNumber() != 0 && seasons.getUserEps() != null && season)
                    for (UserEp userEp : seasons.getUserEps()) {
                        if (!userEp.isAssistido()) {
                            temp.add(userTvshow);
                            season = false;
                            break;
                        }
                    }
            }
        }// gambiara. Arrumar!

        return temp;
    }

    private View getViewMissing(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.temporadas, container, false); // Criar novo layout
        view.findViewById(R.id.progressBarTemporadas).setVisibility(View.GONE);
        recyclerViewMissing = (RecyclerView) view.findViewById(R.id.temporadas_recycle);
        recyclerViewMissing.setHasFixedSize(true);
        recyclerViewMissing.setItemAnimator(new DefaultItemAnimator());
        recyclerViewMissing.setLayoutManager(new LinearLayoutManager(getContext()));
        List<UserTvshow> missing = setSeriesMissing(userTvshows);
        if (missing.size() > 0) {
            recyclerViewMissing.setAdapter(new ProximosAdapter(getActivity(), missing));
        } else {
            view.findViewById(R.id.text_search_empty).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.text_search_empty)).setText(R.string.empty);
        }
        return view;
    }

    private View getViewSeguindo(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.seguindo, container, false); // Criar novo layout
        view.findViewById(R.id.progressBarTemporadas).setVisibility(View.GONE);
        recyclerViewSeguindo = (RecyclerView) view.findViewById(R.id.seguindo_recycle);
        recyclerViewSeguindo.setHasFixedSize(true);
        recyclerViewSeguindo.setItemAnimator(new DefaultItemAnimator());
        recyclerViewSeguindo.setLayoutManager(new GridLayoutManager(getContext(), 4));
        if (userTvshows.size() > 0) {
            recyclerViewSeguindo.setAdapter(new SeguindoRecycleAdapter(getActivity(), userTvshows));
        } else {
            view.findViewById(R.id.text_search_empty).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.text_search_empty)).setText(R.string.empty);
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (eventListener != null) {
            seguindoDataBase.removeEventListener(eventListener);
        }
    }
}
