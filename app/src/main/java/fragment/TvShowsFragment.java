package fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import activity.TvShowActivity;
import adapter.TvShowsAdapter;
import br.com.icaro.filme.R;
import domain.FilmeService;
import info.movito.themoviedbapi.TmdbTV;
import info.movito.themoviedbapi.model.config.Timezone;
import info.movito.themoviedbapi.model.tv.TvSeries;
import utils.Constantes;
import utils.UtilsFilme;

/**
 * Created by icaro on 14/09/16.
 */
public class TvShowsFragment extends Fragment {


    private final String TAG = TvShowsFragment.class.getName();
    private List<TvSeries> tvSeries = null;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textView;
    private FrameLayout frameLayout;
    private ProgressBar process;
    private String abaEscolhida;
    int pagina = 1;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.abaEscolhida = getArguments().getString(Constantes.NAV_DRAW_ESCOLIDO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list_filme, container, false);
        textView = (TextView) view.findViewById(R.id.textLayoutFilmes);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeToRefresh);
        frameLayout = (FrameLayout) view.findViewById(R.id.frame_list_filme);
        process = (ProgressBar) view.findViewById(R.id.progress);


        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        swipeRefreshLayout.setOnRefreshListener(OnRefreshListener());

        swipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);

        recyclerView.setAdapter(new TvShowsAdapter(getContext(), tvSeries != null ? tvSeries : null,
                onClickListener()));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!UtilsFilme.isNetWorkAvailable(getContext())) {
            //  Log.d("onActivityCreated", "Sem internet");
            textView.setVisibility(View.VISIBLE);
            textView.setText(R.string.no_internet);
            swipeRefreshLayout.setEnabled(false);
            snack();

        } else {
            new TMDVAsync().execute();
        }
    }

    protected void snack() {
        Snackbar.make(frameLayout, R.string.no_internet, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (UtilsFilme.isNetWorkAvailable(getContext())) {
                            textView.setVisibility(View.INVISIBLE);
                            TMDVAsync tmdvAsync = new TMDVAsync();
                            tmdvAsync.execute();
                        } else {
                            snack();
                        }
                    }
                }).show();
    }


    protected SwipeRefreshLayout.OnRefreshListener OnRefreshListener() {
        return new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (UtilsFilme.isNetWorkAvailable(getContext())) {
                    new TMDVAsync().execute();
                }
            }
        };
    }

    private TvShowsAdapter.TvshowOnClickListener onClickListener() {
        return new TvShowsAdapter.TvshowOnClickListener() {
            @Override
            public void onClickTvshow(View view, int position) {
                Intent intent = new Intent(getActivity(), TvShowActivity.class);
                intent.putExtra(Constantes.COLOR_TOP, UtilsFilme.loadPalette(view));
                intent.putExtra(Constantes.TVSHOW_ID, tvSeries.get(position).getId());
                intent.putExtra(Constantes.NOME_TVSHOW, tvSeries.get(position).getName());
                getContext().startActivity(intent);

                FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Event.SELECT_CONTENT, TvShowActivity.class.getName());
                bundle.putInt(FirebaseAnalytics.Param.ITEM_NAME, tvSeries.get(position).getId());
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, tvSeries.get(position).getName());
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            }

        };
    }

    private class TMDVAsync extends AsyncTask<Void, Void, List<TvSeries>> {


        @Override
        protected void onPreExecute() {
            if (pagina != 1) {
                getView().findViewById(R.id.swipeToRefresh).setEnabled(false);
            }
            super.onPreExecute();
        }

        @Override
        protected List<TvSeries> doInBackground(Void... voids) {

            try {
                TmdbTV tvShow = FilmeService.getTmdbTvShow();
                return getListaTipo(tvShow);
            } catch (Exception e) {
                if (getActivity() != null)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), R.string.ops, Toast.LENGTH_SHORT).show();
                        }
                    });
            }
            return null;
        }


        @Override
        protected void onPostExecute(List<TvSeries> tmdbMovies) {
            // Log.d("onPostExecute", "onPostExecute");
            process.setVisibility(View.GONE);
            if (tmdbMovies != null && pagina != 1) {
                List<TvSeries> x = tvSeries;
                tvSeries = tmdbMovies;
                for (TvSeries movie : x) {
                    tvSeries.add(movie);
                }
            } else {
                tvSeries = tmdbMovies;
            }
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(true);
            recyclerView.setAdapter(new TvShowsAdapter(getContext(), tvSeries != null ? tvSeries : null,
                    onClickListener()));
            pagina++;
        }

        private List<TvSeries> getListaTipo(TmdbTV tmdbTV) {
            String language = UtilsFilme.getLocale();
            if (language != null) {

                if (abaEscolhida.equals(getResources().getString(R.string.air_date))) {
                //    Log.d(TAG, "getListaTipo: getOnTheAir");
                    return tmdbTV.getOnTheAir(language, pagina).getResults();
                }

                if (abaEscolhida.equals(getResources().getString(R.string.populares))) {
                  //  Log.d(TAG, "getListaTipo: getPopular");
                    return tmdbTV.getPopular(language, pagina).getResults();
                }

                if (abaEscolhida.equals(getResources().getString(R.string.top_rated))) {
                   // Log.d(TAG, "getListaTipo: getTopRated");
                    return tmdbTV.getTopRated(language, pagina).getResults();
                }

                Timezone timezone = UtilsFilme.getTimezone();
               // Log.d(TAG, "getListaTipo: getAiringToday");
                return tmdbTV.getAiringToday(language, pagina, timezone).getResults();

            }
           // Log.d(TAG, "getListaTipo: getAiringToday default");
            return tmdbTV.getOnTheAir("en", pagina).getResults();
        }

    }

}
