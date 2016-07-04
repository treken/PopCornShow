package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

import domian.FilmeService;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;

/**
 * Created by icaro on 24/06/16.
 */

public class UtilsFilme {

    public static void toast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private static Context getContext() {
        return getContext();
    }

    public static boolean isNetWorkAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
            if (connectivityManager == null) {
                return false;
            } else {

                NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
                if (networkInfos != null) {
                    for (int i = 0; i < networkInfos.length; i++) {
                        if (networkInfos[i].isConnected()) {
                            return true;
                        }
                    }
                }
            }


        } catch (SecurityException e) {
            Log.d("UtilsFilme", "isNetWorkAvailable: " + e);
        }
        return false;

    }

    public static String buscarUrlImagemw185(MovieDb movie) {
        String urlBase = "http://image.tmdb.org/t/p/";
        StringBuilder stringBuilder = new StringBuilder(urlBase);
        stringBuilder.append("/")
                .append("w185")
                .append(movie.getPosterPath());
        Log.d("buscarUrlImagemw185", "" + stringBuilder.toString());

        return stringBuilder.toString();
    }

    public static String buscarUrlImagemw342(MovieDb movieDb) {
        String urlBase = "http://image.tmdb.org/t/p/";
        StringBuilder stringBuilder = new StringBuilder(urlBase);
        stringBuilder.append("/")
                .append("w342")
                .append(movieDb.getPosterPath());
        Log.d("buscarUrlImagemw185", "" + stringBuilder.toString());

        return stringBuilder.toString();
    }
}
