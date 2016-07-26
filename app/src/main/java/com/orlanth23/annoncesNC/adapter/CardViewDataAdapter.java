package com.orlanth23.annoncesNC.adapter;

/**
 * Created by olivejp on 05/04/2016.
 */


import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.orlanth23.annoncesNC.R;
import com.orlanth23.annoncesNC.dto.Annonce;
import com.orlanth23.annoncesNC.fragment.FragmentDetailAnnonce;
import com.orlanth23.annoncesNC.utility.Utility;

import java.util.List;

public class CardViewDataAdapter extends
        RecyclerView.Adapter<CardViewDataAdapter.ViewHolder> {

    public static final String tag = CardViewDataAdapter.class.getName();

    private static Context context;
    private static List<Annonce> listAnnonces;
    private static String mode;

    public CardViewDataAdapter(Context context, List<Annonce> annonces, String p_mode) {
        CardViewDataAdapter.context = context;
        listAnnonces = annonces;
        mode = p_mode;
    }

    // Create new views
    @Override
    public CardViewDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        // create a new view
        View itemLayoutView = inflater.inflate(R.layout.cardview_row, parent, false);

        // create ViewHolder
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Annonce annonce = listAnnonces.get(position);
        viewHolder.singleAnnonce = listAnnonces.get(position);

        // Récupération de la couleur
        int color = Utility.getColorFromString(annonce.getCategorieANO().getImageCAT());

        // Attribution des données au valeurs graphiques
        viewHolder.txtIdAnnonce.setText(String.valueOf(annonce.getIdANO()));
        viewHolder.txtColor.setBackgroundColor(color);
        viewHolder.txtTitle.setText(annonce.getTitreANO());

        int nb_caractere = Utility.getPrefNumberCar(context);
        String description = annonce.getDescriptionANO();

        // Si la description fait moins que le nombre maximum de caractère, on prend la taille de la description
        if (description.length() <= nb_caractere) {
            nb_caractere = description.length();
        }

        viewHolder.txtDescription.setText(description.substring(0, nb_caractere));
        viewHolder.txtPrix.setText(Utility.convertPrice(annonce.getPriceANO()));

        // Récupération de la date de publication
        String maDate = annonce.getDatePublished().toString();
        viewHolder.txtDatePublish.setText(Utility.convertDate(maDate));

        // On fait apparaitre une petite photo seulement si l'annonce a une photo
        if (!annonce.getPhotos().isEmpty()) {
            viewHolder.imgView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imgView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Return the size arraylist
    @Override
    public int getItemCount() {
        return listAnnonces.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtIdAnnonce;
        public TextView txtColor;
        public TextView txtTitle;
        public TextView txtDescription;
        public TextView txtPrix;
        public TextView txtDatePublish;
        public ImageView imgView;

        public Annonce singleAnnonce;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            txtIdAnnonce = (TextView) itemLayoutView.findViewById(R.id.textIdAnnonce);
            txtColor = (TextView) itemLayoutView.findViewById(R.id.textColorCategory);
            txtTitle = (TextView) itemLayoutView.findViewById(R.id.textTitreAnnonce);
            txtDescription = (TextView) itemLayoutView.findViewById(R.id.textDescriptionAnnonce);
            txtPrix = (TextView) itemLayoutView.findViewById(R.id.textPrixAnnonce);
            txtDatePublish = (TextView) itemLayoutView.findViewById(R.id.textDatePublicationAnnonce);
            imgView = (ImageView) itemLayoutView.findViewById(R.id.imgPhoto);

            // Onclick event for the row to show the data in toast
            itemLayoutView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // J'attache un listener sur cette nouvelle vue
                    // Passage des paramètres

                    FragmentDetailAnnonce detailAnnonceFragment = FragmentDetailAnnonce.newInstance(mode, singleAnnonce);

                    // On va remplacer le fragment par celui de la liste d'annonce
                    FragmentTransaction transaction = ((Activity) context).getFragmentManager().beginTransaction();
                    transaction.replace(R.id.frame_container, detailAnnonceFragment, FragmentDetailAnnonce.tag);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
        }
    }
}
