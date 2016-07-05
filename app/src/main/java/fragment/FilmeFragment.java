package fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.icaro.filme.R;
import domian.FilmeService;
import info.movito.themoviedbapi.model.Genre;
import info.movito.themoviedbapi.model.MovieDb;
import utils.Constantes;

import static br.com.icaro.filme.R.id.img_poster;


/**
 * Created by icaro on 03/07/16.
 */
public class FilmeFragment extends Fragment {

    //************* Alguns metodos senco chamados 2 vezes

    public MovieDb movieDb;
    ImageView img_top;
    int id_filme;
    CollapsingToolbarLayout collapsingToolbarLayout;
    String titulo;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            id_filme = getArguments().getInt(Constantes.FILME_ID);
            titulo = getArguments().getString(Constantes.NOME_FILME);
            Log.d("FilmeFragment", "onCreate Titulo: " + titulo);
            Log.d("FilmeFragment", "onCreate " + id_filme);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_header_filme, container, false);
        img_top = (ImageView) view.findViewById(R.id.img_top);
        collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (id_filme != 0) {
            TMDVAsync tmdvAsync = new TMDVAsync();
            tmdvAsync.execute();

            FilmeFragmentBotton filmeFragmentBotton = new FilmeFragmentBotton();
            Bundle bundle = new Bundle();
            Log.d("FilmeFragment", "onActivityCreated: -> " + id_filme);
            bundle.putInt(Constantes.FILME_ID, id_filme);
            filmeFragmentBotton.setArguments(bundle);
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.filme_container, filmeFragmentBotton)
                    .commit();
        }
    }

    public void getImagemTopo(final MovieDb movieDb) {

        String urlBase = "http://image.tmdb.org/t/p/";
        final StringBuilder stringBuilder = new StringBuilder(urlBase);
        stringBuilder.append("/")
                .append("w780");
        Log.d("Aqui", stringBuilder.toString());
        Picasso.with(getContext()).load(stringBuilder+movieDb.getBackdropPath()).into(img_top);
    }


    public Context getContext() {
        return this.getActivity();
    }

    public class TMDVAsync extends AsyncTask<Void, Void, MovieDb> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected MovieDb doInBackground(Void... voids) {//
            Log.d("FilmeFragment", "doInBackground :" + id_filme);
            return FilmeService.getTmdbMovie(id_filme, "pt-BR");
        }

        @Override
        protected void onPostExecute(MovieDb movieDb) {
            super.onPostExecute(movieDb);
            collapsingToolbarLayout.setTitle(movieDb.getTitle());
            collapsingToolbarLayout.setExpandedTitleMarginEnd(6);
            getImagemTopo(movieDb);
        }
    }

}
