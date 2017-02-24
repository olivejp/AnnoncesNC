package com.orlanth23.annoncesnc.adapter;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.dto.Annonce;
import com.orlanth23.annoncesnc.fragment.DetailAnnonceFragment;
import com.orlanth23.annoncesnc.utility.Utility;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CardViewDataAdapter extends
        RecyclerView.Adapter<CardViewDataAdapter.ViewHolder> {

    public static final String tag = CardViewDataAdapter.class.getName();
    private Context context;
    private List<Annonce> listAnnonces;
    private String mode;

    public CardViewDataAdapter(Context p_context, List<Annonce> p_annonces, String p_mode) {
        context = p_context;
        listAnnonces = p_annonces;
        mode = p_mode;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemLayoutView = inflater.inflate(R.layout.annonce_row, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Annonce annonce = listAnnonces.get(position);
        viewHolder.singleAnnonce = annonce;

        int color = Color.parseColor(annonce.getCategorieANO().getCouleurCAT());        // Récupération de la couleur

        // Attribution des données au valeurs graphiques
        viewHolder.textIdAnnonce.setText(String.valueOf(annonce.getIdANO()));

        viewHolder.textTitreAnnonce.setText(annonce.getTitreANO());
        String description = annonce.getDescriptionANO();

        // Si la description fait moins que le nombre maximum de caractère, on prend la taille de la description
        int nb_caractere = (Utility.getPrefNumberCar(context) > description.length()) ? description.length() : Utility.getPrefNumberCar(context);

        viewHolder.textDescriptionAnnonce.setText(description.substring(0, nb_caractere).concat("..."));
        viewHolder.textPrixAnnonce.setText(Utility.convertPrice(annonce.getPriceANO()));

        // Récupération de la date de publication
        String datePublished = annonce.getDatePublished().toString();
        viewHolder.textDatePublicationAnnonce.setText(Utility.convertDate(datePublished));

        // On fait apparaitre une petite photo seulement si l'annonce a une photo
        if (!annonce.getPhotos().isEmpty()) {
            viewHolder.imgPhoto.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imgPhoto.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listAnnonces.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.textIdAnnonce)
        TextView textIdAnnonce;
        @BindView(R.id.textTitreAnnonce)
        TextView textTitreAnnonce;
        @BindView(R.id.textDescriptionAnnonce)
        TextView textDescriptionAnnonce;
        @BindView(R.id.textPrixAnnonce)
        TextView textPrixAnnonce;
        @BindView(R.id.textDatePublicationAnnonce)
        TextView textDatePublicationAnnonce;
        @BindView(R.id.imgPhoto)
        ImageView imgPhoto;
       // @BindView(R.id.imageColorCategory)
       // ImageView imageColorCategory;

        Annonce singleAnnonce;

        ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            ButterKnife.bind(this, itemLayoutView);

            itemLayoutView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DetailAnnonceFragment detailAnnonceFragment = DetailAnnonceFragment.newInstance(mode, singleAnnonce);
                    FragmentTransaction transaction = ((Activity) context).getFragmentManager().beginTransaction();
                    transaction.replace(R.id.frame_container, detailAnnonceFragment, DetailAnnonceFragment.tag);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
        }
    }
}
