package fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import br.com.icaro.filme.R;
import domian.FilmeService;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import utils.Constantes;

import static info.movito.themoviedbapi.TmdbMovies.MovieMethod.credits;
import static info.movito.themoviedbapi.TmdbMovies.MovieMethod.images;
import static info.movito.themoviedbapi.TmdbMovies.MovieMethod.releases;
import static info.movito.themoviedbapi.TmdbMovies.MovieMethod.reviews;
import static info.movito.themoviedbapi.TmdbMovies.MovieMethod.similar;
import static info.movito.themoviedbapi.TmdbMovies.MovieMethod.videos;


/**
 * Created by icaro on 03/07/16.
 */

public class FilmeFragmentBotton extends Fragment {

    TextView titulo, categoria, time, descricao, rating, rating_total, metascore_text;
    ImageView img_poster;
    int id_filme;
    MovieDb movieDb;

    //************* Alguns metodos senco chamados 2 vezes

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            id_filme = getArguments().getInt(Constantes.FILME_ID);
            Log.d("FilmeFragmentBotton", "onCreate -> " + id_filme);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container_filme, container, false);

        titulo = (TextView) view.findViewById(R.id.titulo_text);
        categoria = (TextView) view.findViewById(R.id.categoria_filme);
        time = (TextView) view.findViewById(R.id.time_filme);
        descricao = (TextView) view.findViewById(R.id.descricao);
        rating = (TextView) view.findViewById(R.id.rating);
        rating_total = (TextView) view.findViewById(R.id.rating_total);
        metascore_text = (TextView) view.findViewById(R.id.metascore_text);
        img_poster = (ImageView) view.findViewById(R.id.img_poster);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

//        titulo.setText(movieDb.getTitle());
//        categoria.setText(movieDb.getGenres().get(0).toString());
//        time.setText(movieDb.getRuntime());
//        rating.setText((int) movieDb.getUserRating());
//        rating_total.setText(movieDb.getVoteCount());
//        metascore_text.setText((int) movieDb.getVoteAverage());
        //Picasso.with(getActivity()).load(movie.get)
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
            Log.d("FilmeFragmentBotton", "onActivityCreated -> " + id_filme);
        if (id_filme != 0) {
            TMDVAsync tmdvAsync = new TMDVAsync();
            tmdvAsync.execute();
        }
    }

    public class TMDVAsync extends AsyncTask<Void, Void, MovieDb> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected MovieDb doInBackground(Void... voids) {
            TmdbMovies movies = FilmeService.getTmdbMovies();
            Log.d("FilmeFragmentBotton", "doInBackground: -> " + id_filme);
            movieDb = movies.getMovie(id_filme, Constantes.PORTUGUES, credits, videos, releases, images, similar, reviews);
            Log.d("FilmeFragmentBotton", "doInBackground: <-> " + movieDb.toString());

            return movieDb;
        }

        @Override
        protected void onPostExecute(MovieDb movieDb) {
            super.onPostExecute(movieDb);
            titulo.setText(movieDb.getTitle());
            categoria.setText(movieDb.getGenres().get(0).toString());
            time.setText(String.valueOf(movieDb.getRuntime()));
            rating.setText(String.valueOf(movieDb.getUserRating()));
            rating_total.setText(String.valueOf(movieDb.getVoteCount()));
            metascore_text.setText(String.valueOf(movieDb.getVoteAverage()));
            descricao.setText(movieDb.getOverview());
        }
    }
}
