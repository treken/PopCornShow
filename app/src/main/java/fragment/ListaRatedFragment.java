package fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.List;

import adapter.ListaFilmeAdapter;
import adapter.ListaTvShowAdapter;
import br.com.icaro.filme.R;
import domain.FilmeDB;
import domain.TvshowDB;
import filme.activity.FilmeActivity;
import tvshow.activity.TvShowActivity;
import tvshow.fragment.TvShowFragment;
import utils.Constantes;
import utils.UtilsApp;


/**
 * Created by icaro on 23/08/16.
 */
public class ListaRatedFragment extends Fragment {

    final String TAG = TvShowFragment.class.getName();
    private int tipo;
    private List<FilmeDB> movies;
    private List<TvshowDB> tvSeries;
    private RecyclerView recyclerViewFilme;
    private RecyclerView recyclerViewTvShow;
    private FirebaseAnalytics firebaseAnalytics;

    public static Fragment newInstanceMovie(int tipo, List<FilmeDB> filmeDBs) {
        ListaRatedFragment fragment = new ListaRatedFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constantes.INSTANCE.getFILME(), (Serializable) filmeDBs);
        bundle.putInt(Constantes.INSTANCE.getABA(), tipo);
        fragment.setArguments(bundle);

        return fragment;
    }

    public static Fragment newInstanceTvShow(int tvshow, List<TvshowDB> tvshowDBs) {
        ListaRatedFragment fragment = new ListaRatedFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constantes.INSTANCE.getSERIE(), (Serializable) tvshowDBs);
        bundle.putInt(Constantes.INSTANCE.getABA(), tvshow);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tipo = getArguments().getInt(Constantes.INSTANCE.getABA());
            movies = (List<FilmeDB>) getArguments().getSerializable(Constantes.INSTANCE.getFILME());
            tvSeries = (List<TvshowDB>) getArguments().getSerializable(Constantes.INSTANCE.getSERIE());
        }
        firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
       // Log.d(TAG, "onCreateView");
        switch (tipo) {

            case R.string.filme: {
                return getViewMovie(inflater, container);
            }
            case R.string.tvshow: {
                return getViewTvShow(inflater, container);
            }
        }
        return null;
    }

    private ListaFilmeAdapter.ListaOnClickListener onclickListerne() {
        return new ListaFilmeAdapter.ListaOnClickListener() {
            @Override
            public void onClick(final View view, final int position) {
                Intent intent = new Intent(getActivity(), FilmeActivity.class);
              //  Log.d("ListaFilmeAdapter", "ListaFilmeAdapter");
                ImageView imageView = (ImageView) view;
                int color = UtilsApp.loadPalette(imageView);
                intent.putExtra(Constantes.INSTANCE.getCOLOR_TOP(), color);
                intent.putExtra(Constantes.INSTANCE.getFILME_ID(), movies.get(position).getId());
                intent.putExtra(Constantes.INSTANCE.getNOME_FILME(), movies.get(position).getTitle());
                startActivity(intent);


            }

            @Override
            public void onClickLong(View view, final int position) {

                final Dialog alertDialog = new Dialog(getContext());
                alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                alertDialog.setContentView(R.layout.adialog_custom_rated);

                Button ok = (Button) alertDialog.findViewById(R.id.ok_rated);
                Button no = (Button) alertDialog.findViewById(R.id.cancel_rated);
                final RatingBar ratingBar = (RatingBar) alertDialog.findViewById(R.id.ratingBar_rated);
                int width = getResources().getDimensionPixelSize(R.dimen.popup_width); //Criar os Dimen do layout do login - 300dp - 300dp ??
                int height = getResources().getDimensionPixelSize(R.dimen.popup_height_rated);

                ratingBar.setRating(movies.get(position).getNota() / 2);


                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       // Log.d(TAG, "Apagou Rated");

                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();

                        DatabaseReference myRated = database.getReference("users").child(mAuth.getCurrentUser()
                                .getUid()).child("rated")
                                .child("movie").child(String.valueOf(movies.get(position).getId()));

                        myRated.setValue(null)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        movies.remove(movies.get(position));
                                        recyclerViewFilme.getAdapter().notifyItemRemoved(position);
                                        recyclerViewFilme.getAdapter().notifyItemChanged(position);
                                    }
                                });
                        alertDialog.dismiss();
                    }
                });

                alertDialog.getWindow().setLayout(width, height);

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();

                        DatabaseReference myRated = database.getReference("users").child(mAuth.getCurrentUser()
                                .getUid()).child("rated")
                                .child("movie").child(String.valueOf(movies.get(position).getId()));

                        if (ratingBar.getRating() > 0) {

                            movies.get(position).setNota(ratingBar.getRating() * 2);

                            myRated.setValue(movies.get(position))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            recyclerViewFilme.getAdapter().notifyItemChanged(position);

                                        }
                                    });
                        }

                        alertDialog.dismiss();
                    }

                });
                alertDialog.show();
            }
        };
    }


    private ListaTvShowAdapter.ListaOnClickListener onclickTvShowListerne() {
        return new ListaTvShowAdapter.ListaOnClickListener() {
            @Override
            public void onClick(final View view, final int position) {
                Intent intent = new Intent(getActivity(), TvShowActivity.class);
               // Log.d("OnClick", "Onclick");
                ImageView imageView = (ImageView) view;
                int color = UtilsApp.loadPalette(imageView);
                intent.putExtra(Constantes.INSTANCE.getCOLOR_TOP(), color);
                intent.putExtra(Constantes.INSTANCE.getTVSHOW_ID(), tvSeries.get(position).getId());
                intent.putExtra(Constantes.INSTANCE.getNOME_TVSHOW(), tvSeries.get(position).getTitle());
                startActivity(intent);

            }

            @Override
            public void onClickLong(View view, final int position) {

                final Dialog alertDialog = new Dialog(getActivity());
                alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                alertDialog.setContentView(R.layout.adialog_custom_rated);

                Button ok = (Button) alertDialog.findViewById(R.id.ok_rated);
                Button no = (Button) alertDialog.findViewById(R.id.cancel_rated);
                final RatingBar ratingBar = (RatingBar) alertDialog.findViewById(R.id.ratingBar_rated);
                int width = getResources().getDimensionPixelSize(R.dimen.popup_width); //Criar os Dimen do layout do login - 300dp - 300dp ??
                int height = getResources().getDimensionPixelSize(R.dimen.popup_height_rated);
                ratingBar.setRating(tvSeries.get(position).getNota() / 2);
                alertDialog.getWindow().setLayout(width, height);

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();

                        DatabaseReference myRated = database.getReference("users").child(mAuth.getCurrentUser()
                                .getUid()).child("rated")
                                .child("tvshow").child(String.valueOf(tvSeries.get(position).getId()));

                        myRated.setValue(null)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        tvSeries.remove(tvSeries.get(position));
                                        recyclerViewTvShow.getAdapter().notifyItemRemoved(position);
                                        recyclerViewTvShow.getAdapter().notifyItemChanged(position);
                                    }
                                });
                        alertDialog.dismiss();
                    }
                });

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();

                        DatabaseReference myRated = database.getReference("users").child(mAuth.getCurrentUser()
                                .getUid()).child("rated")
                                .child("tvshow").child(String.valueOf(tvSeries.get(position).getId()));

                        if (ratingBar.getRating() > 0) {

                            tvSeries.get(position).setNota( ratingBar.getRating() * 2);

                            myRated.setValue(tvSeries.get(position))
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            recyclerViewTvShow.getAdapter().notifyItemChanged(position);

                                        }
                                    });
                        }

                        alertDialog.dismiss();
                    }

                });
                alertDialog.show();
            }
        };
    }


    private View getViewMovie(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.temporadas, container, false); // Criar novo layout
        view.findViewById(R.id.progressBarTemporadas).setVisibility(View.GONE);
        recyclerViewFilme = (RecyclerView) view.findViewById(R.id.temporadas_recycle);
        recyclerViewFilme.setHasFixedSize(true);
        recyclerViewFilme.setItemAnimator(new DefaultItemAnimator());
        recyclerViewFilme.setLayoutManager(new GridLayoutManager(getContext(), 2));
        if (movies.size() > 0) {
            recyclerViewFilme.setAdapter(new ListaFilmeAdapter(getActivity(), movies,
                    onclickListerne(), true));
        } else {
            view.findViewById(R.id.text_search_empty).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.text_search_empty)).setText(R.string.empty_rated);
        }

        return view;
    }

    private View getViewTvShow(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.temporadas, container, false);// Criar novo layout
        view.findViewById(R.id.progressBarTemporadas).setVisibility(View.GONE);
        recyclerViewTvShow = (RecyclerView) view.findViewById(R.id.temporadas_recycle);
        recyclerViewTvShow.setHasFixedSize(true);
        recyclerViewTvShow.setItemAnimator(new DefaultItemAnimator());
        recyclerViewTvShow.setLayoutManager(new GridLayoutManager(getContext(), 2));
        if (tvSeries.size() > 0) {
            recyclerViewTvShow.setAdapter(new ListaTvShowAdapter(getActivity(), tvSeries,
                    onclickTvShowListerne(), true));
        } else {
            view.findViewById(R.id.text_search_empty).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.text_search_empty)).setText(R.string.empty_rated);
        }

        return view;
    }
}
