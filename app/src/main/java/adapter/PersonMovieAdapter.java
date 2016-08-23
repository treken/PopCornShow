package adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import activity.FilmeActivity;
import br.com.icaro.filme.R;
import info.movito.themoviedbapi.model.people.PersonCredit;
import info.movito.themoviedbapi.model.people.PersonCredits;
import utils.Constantes;
import utils.UtilsFilme;

import static android.R.attr.id;

/**
 * Created by icaro on 18/08/16.
 */
public class PersonMovieAdapter extends RecyclerView.Adapter<PersonMovieAdapter.PersonMovieViewHolder> {
    Context context;
    PersonCredits personCredits;

    public PersonMovieAdapter(Context context, PersonCredits personCredits) {

        this.context = context;
        this.personCredits = personCredits;

    }

    @Override
    public PersonMovieAdapter.PersonMovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.poster_grid_image, parent, false);
        PersonMovieViewHolder holder = new PersonMovieViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final PersonMovieAdapter.PersonMovieViewHolder holder, int position) {

        PersonCredit movie = personCredits.getCast().get(position);
        final int id = movie.getId();
        final String title = movie.getMovieTitle();
        Log.d("PersonMovieAdapter", "True - " + personCredits.getCast().get(position).getMovieTitle() +" " + movie.getPosterPath());
            Picasso.with(context).load(UtilsFilme.getBaseUrlImagem(3) + movie.getPosterPath())
                    .error(R.drawable.poster_empty)
                    .into(holder.poster, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progressBar.setVisibility(View.INVISIBLE);
                            Log.d("PersonMovieAdapter", "Sucesso");
                        }

                        @Override
                        public void onError() {
                            Log.d("PersonMovieAdapter", "ERRO");
                            holder.progressBar.setVisibility(View.INVISIBLE);
                            holder.title.setText(title);
                            holder.title.setVisibility(View.VISIBLE);
                        }
                    });

        holder.poster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FilmeActivity.class);
                ImageView imageView = (ImageView) view;
                int color = UtilsFilme.loadPalette(imageView);
                intent.putExtra(Constantes.COLOR_TOP, color);
                intent.putExtra(Constantes.FILME_ID, id);
                intent.putExtra(Constantes.NOME_FILME, title);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (personCredits.getCast() != null) {
            Log.d("getItemCount", "Tamanho "+personCredits.getCast().size());
            return personCredits.getCast().size();
        }
        return 0;
    }

    public class PersonMovieViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;
        ImageView poster;
        TextView title;

        public PersonMovieViewHolder(View itemView) {
            super(itemView);
            poster = (ImageView) itemView.findViewById(R.id.img_poster_grid);
            title = (TextView) itemView.findViewById(R.id.text_title_crew);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_poster_grid);
        }
    }
}
