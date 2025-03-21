package com.example.tprecyleview.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.tprecyleview.R;
import com.example.tprecyleview.beans.Star;
import com.example.tprecyleview.service.StarService;

import java.util.ArrayList;
import java.util.List;

public class StarAdapter extends RecyclerView.Adapter<StarAdapter.StarViewHolder> implements Filterable {
    private static final String TAG = "StarAdapter";
    private List<Star> stars;
    private List<Star> starsFilter;
    private Context context;
    private NewFilter mfilter;

    public StarAdapter(Context context, List<Star> stars) {
        this.stars = stars;
        this.context = context;
        this.starsFilter = new ArrayList<>(stars);
        this.mfilter = new NewFilter();
    }

    @NonNull
    @Override
    public StarViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(this.context).inflate(R.layout.star_item, viewGroup, false);
        final StarViewHolder holder = new StarViewHolder(v);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate de la vue de la popup
                View popup = LayoutInflater.from(context).inflate(R.layout.star_edit_item, null, false);
                final ImageView img = popup.findViewById(R.id.img);
                final RatingBar bar = popup.findViewById(R.id.ratingBar);
                final TextView idss = popup.findViewById(R.id.idss);

                ImageView starImageView = v.findViewById(R.id.img);
                Drawable drawable = starImageView.getDrawable();

                if (drawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    img.setImageBitmap(bitmap);
                } else {
                    // Si l'image n'est pas un BitmapDrawable, vous pouvez définir une image par défaut
                    img.setImageResource(R.mipmap.star);  // Exemple d'image par défaut
                }

                // Définir la note dans la popup
                bar.setRating(((RatingBar) v.findViewById(R.id.stars)).getRating());
                idss.setText(((TextView) v.findViewById(R.id.ids)).getText().toString());

                // Créer et afficher le dialog
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Notez : ")
                        .setMessage("Donner une note entre 1 et 5 :")
                        .setView(popup)
                        .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                float s = bar.getRating();
                                int ids = Integer.parseInt(idss.getText().toString());
                                Star star = StarService.getInstance().findById(ids);
                                star.setStar(s);
                                StarService.getInstance().update(star);
                                notifyItemChanged(holder.getAdapterPosition());
                            }
                        })
                        .setNegativeButton("Annuler", null)
                        .create();
                dialog.show();
            }
        });
        // Ajout de l'événement de long click pour supprimer
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Récupérer la position et l'identifiant de la star
                int position = holder.getAdapterPosition();
                final Star starToDelete = starsFilter.get(position);

                // Créer une boîte de dialogue de confirmation pour la suppression
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Supprimer")
                        .setMessage("Voulez-vous supprimer " + starToDelete.getName() + " ?")
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Supprimer la star du service
                                StarService.getInstance().delete(starToDelete);

                                // Mettre à jour les listes
                                starsFilter.remove(starToDelete);
                                stars.remove(starToDelete);

                                // Notifier l'adapter du changement
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("Non", null)
                        .create();

                dialog.show();
                return true; // Retourne true pour indiquer que l'événement a été consommé
            }
        });
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull StarViewHolder starViewHolder, int i) {
        Log.d(TAG, "onBindView call ! " + i);
        Glide.with(context)
                .asBitmap()
                .load(starsFilter.get(i).getImg())
                .apply(new RequestOptions().override(100, 100))
                .into(starViewHolder.img);

        starViewHolder.name.setText(starsFilter.get(i).getName().toUpperCase());
        starViewHolder.stars.setRating(starsFilter.get(i).getStar());
        starViewHolder.idss.setText(String.valueOf(starsFilter.get(i).getId()));
    }


    @Override
    public int getItemCount() {
        return starsFilter.size();
    }

    @Override
    public Filter getFilter() {
        return mfilter;
    }

    public class StarViewHolder extends RecyclerView.ViewHolder {
        TextView idss;
        ImageView img;
        TextView name;
        RatingBar stars;
        RelativeLayout parent;

        public StarViewHolder(@NonNull View itemView) {
            super(itemView);
            idss = itemView.findViewById(R.id.ids);
            img = itemView.findViewById(R.id.img);
            name = itemView.findViewById(R.id.name);
            stars = itemView.findViewById(R.id.stars);
            parent = itemView.findViewById(R.id.parent);
        }
    }

    //  Classe interne pour le filtrage
    private class NewFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Star> filteredList = new ArrayList<>();
            final FilterResults results = new FilterResults();

            if (charSequence == null || charSequence.length() == 0) {
                filteredList.addAll(stars);
            } else {
                final String filterPattern = charSequence.toString().toLowerCase().trim();
                for (Star p : stars) {
                    if (p.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(p);
                    }
                }
            }

            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            starsFilter.clear();
            starsFilter.addAll((List<Star>) filterResults.values);
            notifyDataSetChanged();
        }
    }
}


