package adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.icaro.filme.R;
import info.movito.themoviedbapi.model.MovieDb;
import utils.UtilsFilme;

/**
 * Created by icaro on 30/06/16.
 */
public class FilmesAdapter extends RecyclerView.Adapter<FilmesAdapter.FilmeViewHolder> {

    private final Context context;
    private List<MovieDb> tmdbMovies;
    private FilmeOnClickListener filmeOnClickListener;

    public FilmesAdapter(Context context, List<MovieDb> tmdbMovies,
                         FilmeOnClickListener filmeOnClickListener) {

        this.context = context;
        this.tmdbMovies = tmdbMovies;
        this.filmeOnClickListener = filmeOnClickListener;
    }

    @Override
    public FilmeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_filmes_list, parent, false);
        return new FilmeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FilmesAdapter.FilmeViewHolder holder, final int position) {
        final MovieDb movie = tmdbMovies.get(position);
        holder.progressBar.setVisibility(View.VISIBLE);

        if (movie != null) {

            Picasso.with(context)
                    .load(UtilsFilme.getBaseUrlImagem(UtilsFilme.getTamanhoDaImagem(context, 4)) + movie.getPosterPath())
                    .error(R.drawable.poster_empty)
                    .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .into(holder.imagem_filme, new Callback() {
                        @Override
                        public void onSuccess() {
                            String date = movie.getReleaseDate();
                            holder.title.setText(date.length() >= 4 ? date.substring(0, 4) : "");
                            holder.progressBar.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError() {
                            String title = movie.getTitle();
                            String release = movie.getReleaseDate();
                            holder.title.setText(title + " - " + release);
                            holder.progressBar.setVisibility(View.INVISIBLE);
                        }
                    });


            if (filmeOnClickListener != null) {
                holder.imagem_filme.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        filmeOnClickListener.onClickFilme(holder.imagem_filme, position);
                    }
                });
            }
        }

    }

    @Override
    public int getItemCount() {
        if (tmdbMovies != null) {
            return tmdbMovies.size();
        }
        return 0;
    }

    public interface FilmeOnClickListener {
        void onClickFilme(View view, int position);
    }

    class FilmeViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private ImageView imagem_filme;
        private ProgressBar progressBar;

        FilmeViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.titleTextView);
            imagem_filme = (ImageView) itemView.findViewById(R.id.imgFilme);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress);
        }
    }


}
