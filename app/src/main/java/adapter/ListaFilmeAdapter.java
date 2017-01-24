package adapter;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import br.com.icaro.filme.R;
import domain.FilmeDB;
import utils.UtilsFilme;


/**
 * Created by icaro on 01/08/16.
 */
public class ListaFilmeAdapter extends RecyclerView.Adapter<ListaFilmeAdapter.FavoriteViewHolder> {

    List<FilmeDB> filmes;
    Context context;
    ListaOnClickListener onClickListener;
    boolean status = false;

    public ListaFilmeAdapter(FragmentActivity favotireActivity, List<FilmeDB> favoritos,
                             ListaOnClickListener onClickListener, boolean b) {
        this.context = favotireActivity;
        this.filmes = favoritos;
        this.onClickListener = onClickListener;
        status = b;
    }

    @Override
    public FavoriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.usuario_list_adapter, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FavoriteViewHolder holder, final int position) {

        final FilmeDB movie = filmes.get(position);
       // Log.d("onBindViewHolder", "position" + position);
        if (movie != null) {

        if (status) {
            String valor = String.valueOf(movie.getNota());
           // Log.d("Rated", "" + valor);
            if (valor.length() > 3) {
                valor = valor.substring(0, 2);
               // Log.d("Rated 2", "" + valor);
                holder.text_rated_favoritos.setText(valor);
            }
            holder.text_rated_favoritos.setText(valor);
            holder.text_rated_favoritos.setVisibility(View.VISIBLE);
        }

            holder.img_button_coracao_favorite.setVisibility(View.GONE);

            Picasso.with(context)
                    .load(UtilsFilme
                    .getBaseUrlImagem(UtilsFilme.getTamanhoDaImagem(context, 2)) + movie.getPoster())
                    .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .into(holder.img_favorite, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {

                        }
                    });

            holder.img_favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickListener.onClick(view, position);
                }
            });

            holder.img_favorite.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onClickListener.onClickLong(view, position);
                    return true;
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        if (filmes != null) {
            return filmes.size();
        }
        return 0;
    }


    // Colocar em apenas um lugar
    public interface ListaOnClickListener {
        void onClick(View view, int posicao);
        void onClickLong(View view, final int posicao);
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView img_favorite;
        ImageButton img_button_coracao_favorite;
        ProgressBar progressBar;
        TextView text_rated_favoritos;

        public FavoriteViewHolder(View itemView) {
            super(itemView);
            img_favorite = (ImageView) itemView.findViewById(R.id.img_filme_usuario);
            text_rated_favoritos = (TextView) itemView.findViewById(R.id.text_rated_favoritos);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress);

        }
    }

}
