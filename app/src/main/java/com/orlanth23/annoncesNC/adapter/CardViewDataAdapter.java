package com.orlanth23.annoncesNC.adapter;

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
import com.orlanth23.annoncesNC.fragment.DetailAnnonceFragment;
import com.orlanth23.annoncesNC.utility.Utility;

import java.util.List;

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
    public CardViewDataAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemLayoutView = inflater.inflate(R.layout.cardview_row, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Annonce annonce = listAnnonces.get(position);
        viewHolder.singleAnnonce = annonce;

        int color = Utility.getColorFromString(annonce.getCategorieANO().getImageCAT());        // Récupération de la couleur

        // Attribution des données au valeurs graphiques
        viewHolder.txtIdAnnonce.setText(String.valueOf(annonce.getIdANO()));
        viewHolder.txtColor.setBackgroundColor(color);
        viewHolder.txtTitle.setText(annonce.getTitreANO());
        String description = annonce.getDescriptionANO();

        // Si la description fait moins que le nombre maximum de caractère, on prend la taille de la description
        int nb_caractere = (Utility.getPrefNumberCar(context) > description.length()) ? description.length() : Utility.getPrefNumberCar(context);

        viewHolder.txtDescription.setText(description.substring(0, nb_caractere));
        viewHolder.txtPrix.setText(Utility.convertPrice(annonce.getPriceANO()));

        // Récupération de la date de publication
        String datePublished = annonce.getDatePublished().toString();
        viewHolder.txtDatePublish.setText(Utility.convertDate(datePublished));

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

    @Override
    public int getItemCount() {
        return listAnnonces.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtIdAnnonce;
        TextView txtColor;
        TextView txtTitle;
        TextView txtDescription;
        TextView txtPrix;
        TextView txtDatePublish;
        ImageView imgView;
        Annonce singleAnnonce;

        ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            txtIdAnnonce = (TextView) itemLayoutView.findViewById(R.id.textIdAnnonce);
            txtColor = (TextView) itemLayoutView.findViewById(R.id.textColorCategory);
            txtTitle = (TextView) itemLayoutView.findViewById(R.id.textTitreAnnonce);
            txtDescription = (TextView) itemLayoutView.findViewById(R.id.textDescriptionAnnonce);
            txtPrix = (TextView) itemLayoutView.findViewById(R.id.textPrixAnnonce);
            txtDatePublish = (TextView) itemLayoutView.findViewById(R.id.textDatePublicationAnnonce);
            imgView = (ImageView) itemLayoutView.findViewById(R.id.imgPhoto);

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
