package onsignal;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import activity.ElencoActivity;
import activity.FilmeActivity;
import activity.FilmesActivity;
import activity.ListaGenericaActivity;
import activity.MainActivity;
import activity.PersonActivity;
import activity.ReviewsActivity;
import activity.TreilerActivity;
import activity.TvShowActivity;
import applicaton.FilmeApplication;
import utils.Constantes;

/**
 * Created by icaro on 16/10/16.
 */

public class CustomNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
    // This fires when a notification is opened by tapping on it.
    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        Context context = FilmeApplication.getInstance().getBaseContext();
        JSONObject jsonData = result.notification.payload.additionalData;
        if (jsonData != null) {
            Log.d("mesaj", " jsonData.length(): " + jsonData.length());
            try {
                JSONObject object = jsonData;
                String action = (String) object.get("action");
                    /*Filme Activity */
                if (action.equals("FilmeActivity")) {
                    Intent intent = new Intent(context, FilmeActivity.class);

                    if (object.has("color"))
                        intent.putExtra(Constantes.COLOR_TOP, object.getInt("color"));

                    if (object.has("id")) {
                        intent.putExtra(Constantes.FILME_ID, object.getInt("id"));
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(FilmeActivity.class);
                        stackBuilder.addNextIntent(intent);
                        stackBuilder.startActivities();
                    }
                }
                /*TvShow Activity */
                if (action.equals("TvshowActivity")) {
                    Intent intent = new Intent(context, TvShowActivity.class);
                    if (object.has("color"))
                        intent.putExtra(Constantes.COLOR_TOP, object.getInt("color"));

                    if (object.has("nome"))
                        intent.putExtra(Constantes.NOME_TVSHOW, object.getString("nome"));

                    if (object.has("id")) {
                        intent.putExtra(Constantes.TVSHOW_ID, object.getInt("id"));
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(TvShowActivity.class);
                        stackBuilder.addNextIntent(intent);
                        stackBuilder.startActivities();
                    }
                }
                /*Person Activity */
                if (action.equals("PersonActivity")) {
                    Intent intent = new Intent(context, PersonActivity.class);

                    if (object.has("nome"))
                        intent.putExtra(Constantes.NOME_PERSON, object.getString("nome"));

                    if (object.has("id")) {
                        intent.putExtra(Constantes.PERSON_ID, object.getInt("id"));
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(PersonActivity.class);
                        stackBuilder.addNextIntent(intent);
                        stackBuilder.startActivities();
                    }
                }

                if (action.equals("FilmesActivity")) {
                    Intent intent = new Intent(context, FilmesActivity.class);

                   // if (object.has("aba")) {    só funciona para NO CINEMA - ARRUMAR
                       // intent.putExtra(Constantes.ABA, object.getInt("id"));
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(PersonActivity.class);
                        stackBuilder.addNextIntent(intent);
                        stackBuilder.startActivities();
                   // }
                }

                if (action.equals("ListaGenericaActivity")) {
                    Intent intent = new Intent(context, ListaGenericaActivity.class);
                    Log.d("ListaGenericaActivity", "ListaGenericaActivity");
                    if (object.has("nome"))
                        intent.putExtra(Constantes.LISTA_GENERICA, object.getString("nome"));

                    if (object.has("id")) {
                        intent.putExtra(Constantes.LISTA_ID, object.getString("id"));
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(ListaGenericaActivity.class);
                        stackBuilder.addNextIntent(intent);
                        stackBuilder.startActivities();
                    }
                }

                if (action.equals("TreilerActivity")) {
                    Intent intent = new Intent(context, TreilerActivity.class);

                    if (object.has("sinopse"))
                        intent.putExtra(Constantes.SINOPSE, object.getString("sinopse"));

                    if (object.has("youtube_key")) {
                        intent.putExtra(Constantes.YOU_TUBE_KEY, object.getString("youtube_key"));
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(TreilerActivity.class);
                        stackBuilder.addNextIntent(intent);
                        stackBuilder.startActivities();
                    }
                }

                if (action.equals("ReviewsActivity")) {
                    Intent intent = new Intent(context, ReviewsActivity.class);

                    if (object.has("nome"))
                        intent.putExtra(Constantes.NOME_FILME, object.getString("nome"));

                    if (object.has("id")) {
                        intent.putExtra(Constantes.FILME_ID, object.getInt("id"));
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(ReviewsActivity.class);
                        stackBuilder.addNextIntent(intent);
                        stackBuilder.startActivities();
                    }
                }

                if (action.equals("ElencoActivity")) {
                    Intent intent = new Intent(context, ElencoActivity.class);

                    if (object.has("nome"))
                        intent.putExtra(Constantes.NOME, object.getString("nome"));

                    if (object.has("mediatype"))
                        intent.putExtra(Constantes.MEDIATYPE, object.getString("mediatype"));

                    if (object.has("tvseason"))
                        intent.putExtra(Constantes.TVSEASONS, object.getString("tvseason"));

                    if (object.has("id") && object.has("mediatype")) {
                        intent.putExtra(Constantes.ID, object.getInt("id"));
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        intent.putExtra("notification", false);
                        stackBuilder.addParentStack(PersonActivity.class);
                        stackBuilder.addNextIntent(intent);
                        stackBuilder.startActivities();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();

            }
        } else {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }


    }
}

// The following can be used to open an Activity of your choice.
// Replace - getApplicationContext() - with any Android Context.
// Intent intent = new Intent(getApplicationContext(), YourActivity.class);
// intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
// startActivity(intent);

// Add the following to your AndroidManifest.xml to prevent the launching of your main Activity
//   if you are calling startActivity above.

/*
        <application ...>
          <meta-data android:name="com.onesignal.NotificationOpened.DEFAULT" android:value="DISABLE" />
        </application>

 */


