package fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import activity.PersonActivity;
import applicaton.FilmeApplication;
import br.com.icaro.filme.R;
import domian.FilmeService;
import domian.UserEp;
import domian.UserSeasons;
import info.movito.themoviedbapi.model.Credits;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import utils.Constantes;
import utils.UtilsFilme;

import static br.com.icaro.filme.R.id.ep_rating;


/**
 * Created by icaro on 27/08/16.
 */
public class EpsodioFragment extends Fragment {

    int tvshow_id, color, position, temporada_position;
    Credits credits;
    TvEpisode episode;
    final String TAG = this.getClass().getName();

    String nome_serie;
    LinearLayout linear_director, linear_air_date, linear_write, linear_vote;
    FrameLayout frame_meio_ep_cima, frame_meio_ep_baixo;
    TextView ep_title, ep_tvshow, ep_director, air_date, ep_write, ep_votos, ep_sinopse;
    ImageView ep_image;
    RatingBar ep_ratingBar;
    Button ep_rating_button;
    UserEp userEp;
    boolean seguindo;

    FirebaseAuth auth;
    DatabaseReference referenceEps;
    private ValueEventListener postListener;


    public static Fragment newInstance(TvEpisode tvEpisode, String nome_serie,
                                       int tvshow_id, int color, boolean seguindo, int position, UserSeasons seasons, int temporada_position) {

        EpsodioFragment fragment = new EpsodioFragment();
        Bundle bundle = new Bundle();
        Log.d("TvShowFragment", "Series " + tvEpisode.getName());
        bundle.putSerializable(Constantes.EPSODIO, tvEpisode);
        bundle.putInt(Constantes.TVSHOW_ID, tvshow_id);
        bundle.putInt(Constantes.COLOR_TOP, color);
        bundle.putString(Constantes.NOME_TVSHOW, nome_serie);
        bundle.putBoolean(Constantes.SEGUINDO, seguindo);
        bundle.putInt(Constantes.POSICAO, position);
        bundle.putSerializable(Constantes.USER, seasons);
        bundle.putInt(Constantes.TEMPORADA_POSITION, temporada_position);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            episode = (TvEpisode) getArguments().getSerializable(Constantes.EPSODIO);
            nome_serie = getArguments().getString(Constantes.NOME_TVSHOW);
            tvshow_id = getArguments().getInt(Constantes.TVSHOW_ID);
            color = getArguments().getInt(Constantes.COLOR_TOP);
            seguindo = getArguments().getBoolean(Constantes.SEGUINDO);
            position = getArguments().getInt(Constantes.POSICAO);
            temporada_position = getArguments().getInt(Constantes.TEMPORADA_POSITION);
        }

        if (seguindo) {
            auth = FirebaseAuth.getInstance();
            //referenceUser = FirebaseDatabase.getInstance().getReference("users");

            referenceEps = FirebaseDatabase.getInstance().getReference("users").child(auth.getCurrentUser().getUid())
                    .child(String.valueOf(tvshow_id))
                    .child("seasons")
                    .child(String.valueOf(temporada_position))
                    .child("userEps")
                    .child(String.valueOf(position));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.epsodio_fragment, container, false);

        linear_air_date = (LinearLayout) view.findViewById(R.id.linear_air_date);
        linear_director = (LinearLayout) view.findViewById(R.id.linear_director);
        linear_write = (LinearLayout) view.findViewById(R.id.linear_write);
        linear_vote = (LinearLayout) view.findViewById(R.id.linear_vote);
        frame_meio_ep_cima = (FrameLayout) view.findViewById(R.id.linear_meio_ep_cima);
        frame_meio_ep_baixo = (FrameLayout) view.findViewById(R.id.linear_meio_ep_baixo);

        ep_title = (TextView) view.findViewById(R.id.ep_title);
        ep_tvshow = (TextView) view.findViewById(R.id.ep_tvshow);
        ep_director = (TextView) view.findViewById(R.id.ep_director);
        ep_write = (TextView) view.findViewById(R.id.ep_write);
        ep_votos = (TextView) view.findViewById(R.id.ep_votos);
        ep_sinopse = (TextView) view.findViewById(R.id.ep_sinopse);
        air_date = (TextView) view.findViewById(R.id.air_date);

        ep_image = (ImageView) view.findViewById(R.id.ep_image);
        ep_ratingBar = (RatingBar) view.findViewById(ep_rating);
        ep_rating_button = (Button) view.findViewById(R.id.ep_rating_button);
        ep_rating_button.setTextColor(color);

        AdView adview = (AdView) view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
                .addTestDevice("AC98C820A50B4AD8A2106EDE96FB87D4")  // An example device ID
                .build();
        adview.loadAd(adRequest);

        new TvEpisodeAsync().execute();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListener();

        setAirDate();
        setVote();
        setImage();
        setTvshow();
        setSinopse();
        setName();

        if (episode.getAirDate() != null ) {
            setButtonRating();
        }
    }

    private void setListener(){

        postListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userEp = dataSnapshot.getValue(UserEp.class);

                if (userEp != null) {
                    Log.d(TAG, "onDataChange");
                    Log.d(TAG, "key: " + dataSnapshot.getKey());
                    Log.d(TAG, "assistido " + userEp.isAssistido());

                    if (userEp.isAssistido()){
                        ep_rating_button.setBackground(getResources().getDrawable(R.drawable.button_visto));
                        ep_rating_button.setText(getResources().getText(R.string.classificar_visto));
                    } else {
                        ep_rating_button.setBackground(getResources().getDrawable(R.drawable.button_nao_visto));
                        ep_rating_button.setText(getResources().getText(R.string.classificar));
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        };

        if (seguindo) {
            referenceEps.
                    addValueEventListener(postListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (seguindo) {
            referenceEps.removeEventListener(postListener);
        }
    }

    private void setButtonRating() {
        //Arrumar. Ta esquisito.

        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = sdf.parse(episode.getAirDate() != null ? episode.getAirDate() : null );
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (UtilsFilme.verificaLancamento(date) && FilmeApplication.getInstance().isLogado()) {


            ep_rating_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog alertDialog = new Dialog(getContext());
                    alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    alertDialog.setContentView(R.layout.adialog_custom_rated);

                    Button ok = (Button) alertDialog.findViewById(R.id.ok_rated);
                    Button nao_visto = (Button )  alertDialog.findViewById(R.id.cancel_rated);

                    if (userEp != null) {
                        if (!userEp.isAssistido()) {
                            nao_visto.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        nao_visto.setVisibility(View.INVISIBLE);
                    }


                    TextView title = (TextView) alertDialog.findViewById(R.id.rating_title);
                    title.setText(episode.getName() != null ? episode.getName() : "");
                    final RatingBar ratingBar = (RatingBar) alertDialog.findViewById(R.id.ratingBar_rated);
                    int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
                    int height = getResources().getDimensionPixelSize(R.dimen.popup_height_rated);

                    alertDialog.getWindow().setLayout(width, height);
                    final ProgressDialog progressDialog = new ProgressDialog(getContext(),
                            android.R.style.Theme_Material_Dialog);


                    nao_visto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (seguindo) {
                                Log.d(TAG, "não visto");
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(auth.getCurrentUser().getUid())
                                        .child(String.valueOf(tvshow_id))
                                        .child("seasons")
                                        .child(String.valueOf(temporada_position));
                                Map<String, Object> childUpdates = new HashMap<String, Object>();

                                childUpdates.put("/userEps/" + position + "/assistido", false);
                                childUpdates.put("/visto/", false);
                                databaseReference.updateChildren(childUpdates);
                            }
                            alertDialog.dismiss();
                        }
                    });

                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {


                            progressDialog.setIndeterminate(true);
                            progressDialog.setMessage(getContext().getResources().getString(R.string.salvando));
                            progressDialog.show();

                            new Thread() {
                                boolean status = false;

                                @Override
                                public void run() {
                                    if (UtilsFilme.isNetWorkAvailable(getContext())) {
                                        status = FilmeService
                                                .setRatedTvShowEpsodio(tvshow_id, episode.getSeasonNumber(),
                                                        episode.getEpisodeNumber(), ratingBar.getRating());
                                        try {
                                            if (getActivity() != null) { //usada para não crash quando activity e destruida antes do fim do metodo
                                                Thread.sleep(200);
                                                if (getActivity() != null) {
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(getContext(), getResources().getString(R.string.tvshow_rated), Toast.LENGTH_SHORT)
                                                                    .show();
                                                            if (seguindo) {

                                                                referenceEps.child("assistido").setValue(true);
                                                                ep_rating_button.setText(R.string.classificar_visto);
                                                                ep_rating_button.setBackground(getResources().getDrawable(R.drawable.button_visto));
                                                            }
                                                            progressDialog.dismiss();
                                                        }
                                                    });
                                                }
                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }.start();
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
            });
        } else {
            ep_rating_button.setVisibility(View.GONE);
        }
    }


    private void setTvshow() {
        if (nome_serie != null) {
            ep_tvshow.setText(nome_serie);
        } else {
            ep_tvshow.setVisibility(View.GONE);
        }
    }

    private void setSinopse() {
        if (episode.getOverview() != null) {
            ep_sinopse.setText(episode.getOverview());
        }
    }

    private void setImage() {


        Picasso.with(getContext()).load(UtilsFilme.getBaseUrlImagem(4) + episode.getStillPath())
                .error(R.drawable.top_empty)
                .into(ep_image, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });

    }

    private void setVote() {

        if (episode.getVoteAverage() > 0) {
            String votos = (String) String.valueOf(episode.getVoteAverage()).subSequence(0, 3);

            if (episode.getVoteAverage() < 10) {
                ep_votos.setText(votos + "/" + episode.getVoteCount());
            } else {
                votos = votos.replace(".", "");
                ep_votos.setText(votos + "/" + episode.getVoteCount());
            }
        } else {
            linear_vote.setVisibility(View.GONE);
            frame_meio_ep_baixo.setVisibility(View.GONE);
        }
    }

    private void setAirDate() {
        if (episode.getAirDate() != null && episode.getAirDate() != "") {
            air_date.setText(episode.getAirDate());
        } else {
            linear_air_date.setVisibility(View.GONE);
            frame_meio_ep_baixo.setVisibility(View.GONE);
        }
    }

    private void setWrite() {
        boolean visible = true;
        if (credits.getCrew() != null) {
            for (final PersonCrew crew : credits.getCrew()) {
                // Log.d("EpsodioFragment", crew.getName()+ " - "+ crew.getJob());
                if (crew.getJob().contains("Writer") && crew.getName() != "") {
                    ep_write.setText(crew.getName());
                    visible = false;

                    ep_write.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), PersonActivity.class);
                            intent.putExtra(Constantes.PERSON_ID, crew.getId());
                            intent.putExtra(Constantes.NOME_PERSON, crew.getName());
                            getContext().startActivity(intent);
                        }
                    });

                    return;
                }
            }
        }

        if (visible) {
            linear_write.setVisibility(View.GONE);
            frame_meio_ep_baixo.setVisibility(View.GONE);
        }

    }

    private void setName() {

        ep_title.setText(!episode.getName().isEmpty() ? episode.getName() : getContext().getString(R.string.sem_nome));
    }

    private void setDirector() {
        boolean visible = true;
        if (credits.getCrew() != null) {
            for (final PersonCrew crew : credits.getCrew()) {
                if (crew.getJob().contains("Director") && crew.getName() != "") {
                    ep_director.setText(crew.getName());

                    visible = false;


                    ep_director.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getContext(), PersonActivity.class);
                            intent.putExtra(Constantes.PERSON_ID, crew.getId());
                            intent.putExtra(Constantes.NOME_PERSON, crew.getName());
                            getContext().startActivity(intent);
                        }
                    });

                    return;
                }
            }
        }

        if (visible) {
            linear_director.setVisibility(View.GONE);
            frame_meio_ep_cima.setVisibility(View.GONE);
        }

    }


    private class TvEpisodeAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "" + tvshow_id + " " + episode.getSeasonNumber() + " " + episode.getEpisodeNumber());

            credits = FilmeService.getTmdbTvEpisodes()
                    .getCredits(tvshow_id, episode.getSeasonNumber(), episode.getEpisodeNumber(), "en");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setDirector();
            setWrite();
        }
    }

}
