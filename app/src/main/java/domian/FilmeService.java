package domian;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import applicaton.FilmeApplication;
import info.movito.themoviedbapi.TmdbAccount;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbCollections;
import info.movito.themoviedbapi.TmdbCompany;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbSearch;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.config.Account;
import info.movito.themoviedbapi.model.config.TokenSession;
import info.movito.themoviedbapi.model.core.AccountID;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import info.movito.themoviedbapi.model.core.ResponseStatus;
import info.movito.themoviedbapi.model.core.ResponseStatusException;
import info.movito.themoviedbapi.model.core.SessionToken;
import info.movito.themoviedbapi.tools.ApiUrl;
import info.movito.themoviedbapi.tools.MovieDbException;
import info.movito.themoviedbapi.tools.RequestMethod;
import utils.Config;

import static android.R.attr.id;
import static br.com.icaro.filme.R.id.rated;
import static info.movito.themoviedbapi.AbstractTmdbApi.PARAM_LANGUAGE;
import static info.movito.themoviedbapi.TmdbAccount.PARAM_SESSION;
import static info.movito.themoviedbapi.TmdbAccount.TMDB_METHOD_ACCOUNT;

/**
 * Created by icaro on 01/07/16.
 */

public class FilmeService {


    private static final Collection<Integer> SUCCESS_STATUS_CODES = Arrays.asList(
            1, // Success
            12, // The item/record was updated successfully.
            13 // The item/record was updated successfully.
    );

    public static TmdbSearch getTmdbSearch() {
        return new TmdbApi(Config.TMDB_API_KEY).getSearch();
    }

    public static TmdbMovies getTmdbMovies() {
        TmdbMovies movies = new TmdbApi(Config.TMDB_API_KEY).getMovies();

        return movies;
    }

    public static TmdbCompany getTmdbCompany(){
       TmdbCompany company = new TmdbApi(Config.TMDB_API_KEY).getCompany();
        return company;
    }

    public static TmdbCollections getTmdbCollections() {
        TmdbApi tmdbApi = new TmdbApi(Config.TMDB_API_KEY);
        Log.d("getTmdbMovie", "Id: " + id);

        return tmdbApi.getCollections();
    }

    public static Account getAccount(String user, String pass) {
        Log.d("TmdbAuthentication", "TmdbAuthentication");
        TmdbApi tmdbApi = new TmdbApi(Config.TMDB_API_KEY);
        try {
            TokenSession authentication = tmdbApi
                    .getAuthentication().getSessionLogin(user, pass);
            String session = authentication.getSessionId();
            SessionToken token = new SessionToken(session);
            Account account = tmdbApi.getAccount().getAccount(token);

            if (account != null) {
                return account;
            }

        } catch (Exception e) {
            Log.e("getAccount", e.getMessage());
        }
        Log.d("getAccount", "Account Nulo");
        return null;
    }

    public static SessionToken TmdbSessionToken(String user, String password) {
        Log.d("TmdbAuthentication", "TmdbAuthentication");
        TokenSession authentication = new TmdbApi(Config.TMDB_API_KEY)
                .getAuthentication().getSessionLogin("icaronunes", "knabs123");
        String session = authentication.getSessionId();
        SessionToken token = new SessionToken(session);
        TmdbApi tmdbApi = new TmdbApi(Config.TMDB_API_KEY);
        TmdbAccount account = tmdbApi.getAccount();
        AccountID accountID = new AccountID(getAccount(user, password).getId());
        MovieResultsPage movieResultsPage = account.getFavoriteMovies(token, accountID);
        List<MovieDb> dbList = movieResultsPage.getResults();

        Log.d("TmdbAuthentication", dbList.get(0).getOriginalTitle());

        if (token != null) {
            return token;
        } else {
            Log.d("SessionToken", "SessionToken Nulo");
            return null;
        }
    }

    public static TmdbAccount tmdbAccount() {
        TmdbApi tmdbApi = new TmdbApi(Config.TMDB_API_KEY);
        TmdbAccount account = tmdbApi.getAccount();

        return account;

    }

    public static ResponseStatus addOrRemoverWatchList(String user, String password, Integer id_filme, boolean opcao) {

        TmdbApi tmdbApi = new TmdbApi(Config.TMDB_API_KEY);
        TokenSession authentication = tmdbApi
                .getAuthentication().getSessionLogin(user, password);
        String session = authentication.getSessionId();
        SessionToken token = new SessionToken(session);
        TmdbAccount account = tmdbApi.getAccount();
        AccountID accountID = new AccountID(getAccount(user, password).getId());
        if (opcao) {
            ResponseStatus status = account.addToWatchList(token, accountID, id_filme, TmdbAccount.MediaType.MOVIE);
            Log.d("addOrRemoverWatchList", status.toString());
            return status;
        } else {
            ResponseStatus status = account.removeFromWatchList(token, accountID, id_filme, TmdbAccount.MediaType.MOVIE);
            Log.d("addOrRemoverWatchList", status.toString());
            return status;
        }

    }

    public static ResponseStatus addOrRemoverFavorite(String user, String password, Integer id_filme, boolean opcao) {

        TmdbApi tmdbApi = new TmdbApi(Config.TMDB_API_KEY);
        TokenSession authentication = tmdbApi
                .getAuthentication().getSessionLogin(user, password);
        String session = authentication.getSessionId();
        SessionToken token = new SessionToken(session);
        TmdbAccount account = tmdbApi.getAccount();
        AccountID accountID = new AccountID(getAccount(user, password).getId());
        if (opcao) {
            ResponseStatus status = account.addFavorite(token, accountID, id_filme, TmdbAccount.MediaType.MOVIE);
            Log.d("addOrRemoverFavorite", status.toString());
            return status;
        } else {
            ResponseStatus status = account.removeFavorite(token, accountID, id_filme, TmdbAccount.MediaType.MOVIE);
            Log.d("addOrRemoverFavorite", status.toString());
            return status;
        }
    }

    public static MovieResultsPage getTotalFavorite() {
        String user = FilmeApplication.getInstance().getUser();
        String pass = FilmeApplication.getInstance().getPass();
        MovieResultsPage favoritos;
        favoritos = getFavorite(user, pass);
        if (favoritos != null) {
            if (favoritos.getTotalPages() > 1) {
                for (int i = 2; i <= favoritos.getTotalPages(); i++) {
                    favoritos.getResults().addAll(FilmeService.getFavoriteMovies(user, pass, i).getResults());
                }
            }
            Log.d("getTotalFavorite", "Total page" + favoritos.getTotalPages());
        }
        return favoritos;
    }

    //Copia de TMDBAPI para pegar paginas do Favoritos

    public static MovieResultsPage getFavorite(String user, String password) {
        //Criado novo getFavorito que aceita numero da pagina
        TmdbApi tmdbApi = new TmdbApi(Config.TMDB_API_KEY);
        TokenSession authentication = tmdbApi
                .getAuthentication().getSessionLogin(user, password);
        String session = authentication.getSessionId();
        SessionToken token = new SessionToken(session);
        TmdbAccount account = tmdbApi.getAccount();
        AccountID accountID = new AccountID(getAccount(user, password).getId());
        return account.getFavoriteMovies(token, accountID);
    }

    public static MovieResultsPage getFavoriteMovies(String user, String password, Integer page) {
        TmdbApi tmdbApi = new TmdbApi(Config.TMDB_API_KEY);
        TokenSession authentication = tmdbApi
                .getAuthentication().getSessionLogin(user, password);
        String session = authentication.getSessionId();
        SessionToken token = new SessionToken(session);
        ApiUrl apiUrl = new ApiUrl(TMDB_METHOD_ACCOUNT, getAccount(user, password).getId(), "favorite/movies" );
        apiUrl.addParam(PARAM_SESSION, token);
        apiUrl.addPage(page);

        return mapJsonResult(apiUrl, MovieResultsPage.class);
    }

    public static <T> T mapJsonResult(ApiUrl apiUrl, Class<T> someClass) {
        return mapJsonResult(apiUrl, someClass, null);
    }

    public static <T> T mapJsonResult(ApiUrl apiUrl, Class<T> someClass, String jsonBody) {
        return mapJsonResult(apiUrl, someClass, jsonBody, RequestMethod.GET);
    }

    public static <T> T mapJsonResult(ApiUrl apiUrl, Class<T> someClass, String jsonBody, RequestMethod requestMethod) {
        TmdbApi tmdbApi = new TmdbApi(Config.TMDB_API_KEY);
        final ObjectMapper jsonMapper = new ObjectMapper();
        String webpage = tmdbApi.requestWebPage(apiUrl, jsonBody, requestMethod);

//        System.out.println(webpage);

        try {
//            if(someClass.equals(TmdbTimezones.class)) {
//            	return (T) new TimezoneJsonMapper(webpage);
//            }

            // check if was error responseStatus
            ResponseStatus responseStatus = jsonMapper.readValue(webpage, ResponseStatus.class);
            // work around the problem that there's no status code for suspected spam names yet
            String suspectedSpam = "Unable to create list because: Description is suspected to be spam.";
            if (webpage.contains(suspectedSpam)) {
                responseStatus = new ResponseStatus(-100, suspectedSpam);
            }

            // if null, the json response was not a error responseStatus code, and but something else
            Integer statusCode = responseStatus.getStatusCode();
            if (statusCode != null && !SUCCESS_STATUS_CODES.contains(statusCode)) {
                throw new ResponseStatusException(responseStatus);
            }

            return jsonMapper.readValue(webpage, someClass);
        } catch (IOException ex) {
            throw new MovieDbException("mapping failed:\n" + webpage);
        }
    }

    //Copia de TMDBAPI para pegar paginas do Favoritos ^^^^^^^^


    public static MovieResultsPage getRated(String user, String password, int pagina) {
        TokenSession authentication = new TmdbApi(Config.TMDB_API_KEY)
                .getAuthentication().getSessionLogin(user, password);
        String session = authentication.getSessionId();
        SessionToken token = new SessionToken(session);
        TmdbApi tmdbApi = new TmdbApi(Config.TMDB_API_KEY);
        TmdbAccount account = tmdbApi.getAccount();
        AccountID accountID = new AccountID(getAccount(user, password).getId());
        return account.getRatedMovies(token, accountID, pagina);
    }

    public static MovieResultsPage getRatedListTotal() {
        String user = FilmeApplication.getInstance().getUser();
        String pass = FilmeApplication.getInstance().getPass();
        MovieResultsPage rated = FilmeService.getRated(user, pass, 1);
        if (rated != null) {
            if (rated.getTotalPages() > 1) {
                for (int i = 2; i <= rated.getTotalPages(); i++) {
                    rated.getResults().addAll(FilmeService.getRated(user, pass, i).getResults());
                }
            }
        }
        return rated;
    }

    public static MovieResultsPage getWatchList(String user, String password, int pagina) {
        TokenSession authentication = new TmdbApi(Config.TMDB_API_KEY)
                .getAuthentication().getSessionLogin(user, password);
        String session = authentication.getSessionId();
        SessionToken token = new SessionToken(session);
        TmdbApi tmdbApi = new TmdbApi(Config.TMDB_API_KEY);
        TmdbAccount account = tmdbApi.getAccount();
        AccountID accountID = new AccountID(getAccount(user, password).getId());
        return account.getWatchListMovies(token, accountID, pagina);
    }

    public static MovieResultsPage getWatchListTotal() {
        String user = FilmeApplication.getInstance().getUser();
        String pass = FilmeApplication.getInstance().getPass();

        MovieResultsPage watch = getWatchList(user, pass, 1);
        if (watch != null) {
            if (watch.getTotalPages() > 1) {
                for (int i = 2; i <= watch.getTotalPages(); i++) {
                    watch.getResults().addAll(getWatchList(user, pass, i).getResults());
                }
            }
        }
        return watch;
    }
}
