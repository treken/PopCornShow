package domian;

import android.util.Log;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;

/**
 * Created by icaro on 01/07/16.
 */

public class FilmeService {

    TmdbMovies movies;


    public static TmdbMovies getTmdbMovies() {

        TmdbMovies movies = new TmdbApi("fb14e77a32282ed59a8122a266010b70").getMovies();
        return movies;
    }

    public static MovieDb getTmdbMovie(int id, String idioma) {
        TmdbApi tmdbApi = new TmdbApi("fb14e77a32282ed59a8122a266010b70");
        Log.d("getTmdbMovie", "Id: "+ id);
        //Log.d("FilmeService", tmdbApi.getMovies().getMovie(id, idioma).toString());
        return tmdbApi.getMovies().getMovie(id, idioma);
    }

//    public static TmdbMovies getTmdbMoviesPortugues() {
//        TmdbMovies movies = FilmeService.getTmdbMovies();
//        List<MovieDb> dbList = movies.getTopRatedMovies("en", pagina).getResults();
//    }
//
//    public static TmdbMovies getLastePortugues() {
//        TmdbMovies movies = FilmeService.getTmdbMovies();
//        List<MovieDb> dbList = movies.getLatestMovie().getPosterPath();
//    }
//
//    public class TMDVAsync extends AsyncTask<Void, Void, List<MovieDb>> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected List<MovieDb> doInBackground(Void... voids) {
//            Log.d("doInBackground", "doInBackground");
//            TmdbMovies movies = FilmeService.getTmdbMovies();
//            List<MovieDb> dbList = getListaTipo(movies);
//            return dbList;
//        }
//
//
//
//        @Override
//        protected void onPostExecute(List<MovieDb> tmdbMovies) {
//            Log.d("onPostExecute", "onPostExecute");
//
//        }
//    }
//
//    protected List<MovieDb> getListaTipo(TmdbMovies tmdbMovies) {
//
//        switch (abaEscolhida) {
//
//            case R.string.now_playing: {
//                return tmdbMovies.getNowPlayingMovies("pt_BR", pagina).getResults();
//            }
//
//            case R.string.upcoming: {
//                return tmdbMovies.getUpcoming("pt-BR", pagina).getResults();
//            }
//
//
//            case R.string.popular: {
//                return tmdbMovies.getPopularMovies("pt-BR", pagina).getResults();
//            }
//
//            case R.string.top_rated: {
//                return tmdbMovies.getTopRatedMovies("pt-BR", pagina).getResults();
//            }
//
//        }
//        return tmdbMovies.getNowPlayingMovies("pt_BR", pagina).getResults();
//    }

}
