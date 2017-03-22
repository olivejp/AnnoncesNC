package com.orlanth23.annoncesnc.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.activity.ImageViewerActivity;
import com.orlanth23.annoncesnc.activity.PostAnnonceActivity;
import com.orlanth23.annoncesnc.dto.Annonce;
import com.orlanth23.annoncesnc.dto.Categorie;
import com.orlanth23.annoncesnc.dto.Photo;
import com.orlanth23.annoncesnc.interfaces.CustomActivityInterface;
import com.orlanth23.annoncesnc.list.ListeCategories;
import com.orlanth23.annoncesnc.utility.Constants;
import com.orlanth23.annoncesnc.utility.Utility;
import com.orlanth23.annoncesnc.webservice.Proprietes;
import com.orlanth23.annoncesnc.webservice.ReturnWS;
import com.orlanth23.annoncesnc.webservice.ServiceAnnonce;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailAnnonceFragment extends Fragment {

    public static final String tag = DetailAnnonceFragment.class.getName();
    public static final String ARG_PARAM_MODE = "MODE";
    public final static int SEND_SMS_FROM_DETAIL = 600;
    public final static int SEND_CALL_FROM_DETAIL = 700;
    public final static int SEND_MAIL_FROM_DETAIL = 800;
    public final static int UPDATE_DETAIL = 900;
    private static final String ARG_PARAM_ANNONCE = "ANNONCE";
    @BindView(R.id.value_id_annonce)
    TextView value_id_annonce;
    @BindView(R.id.value_user)
    TextView value_user;
    @BindView(R.id.value_titre)
    TextView value_titre;
    @BindView(R.id.value_description)
    TextView value_description;
    @BindView(R.id.value_prix_annonce)
    TextView value_prix_annonce;
    @BindView(R.id.actionAppel)
    Button btnActionAppel;
    @BindView(R.id.actionEmail)
    Button btnActionEmail;
    @BindView(R.id.actionSms)
    Button btnActionSms;
    @BindView(R.id.actionDelete)
    Button btnActionDelete;
    @BindView(R.id.actionUpdate)
    Button btnActionUpdate;
    @BindView(R.id.linearButtonMaj)
    LinearLayout linearButtonMaj;
    @BindView(R.id.linearButtonVis)
    LinearLayout linearButtonVis;
    @BindView(R.id.image_container)
    LinearLayout P_IMAGE_CONTAINER;
    private String mMode;
    private Annonce mAnnonce;
    private HorizontalScrollView horizontalScrollView;
    private ScrollView scrollImageView;

    private ProgressDialog prgDialog;

    private String telephoneUser = "";
    private String emailUser = "";
    private Dialog dialogDeleteChoice;

    private Activity mActivity;

    // Création du listener pour supprimer une annonce
    private View.OnClickListener clickListenerDeleteBouton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialogDeleteChoice.show();
        }
    };


    // Création du listener pour envoyer un sms
    private View.OnClickListener clickListenerSmsBouton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.putExtra("sms_body", "Votre annonce m\'intéresse");
            sendIntent.putExtra("address", telephoneUser);
            sendIntent.setType("vnd.android-dir/mms-sms");
            startActivityForResult(sendIntent, SEND_SMS_FROM_DETAIL);
        }
    };
    // Création du listener pour appeler la personne qui a publié l'annonce
    private View.OnClickListener clickListenerCallBouton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.setData(Uri.parse("tel:" + telephoneUser));
            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivityForResult(intent, SEND_CALL_FROM_DETAIL);
        }
    };

    // Création du listener pour envoyer un email
    private View.OnClickListener clickListenerEmailBouton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String[] TO = {emailUser};
            String[] CC = {""};
            Intent emailIntent = new Intent(Intent.ACTION_SEND);

            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
            emailIntent.putExtra(Intent.EXTRA_CC, CC);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, value_titre.getText());
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Votre annonce m\'intéresse");

            try {
                startActivityForResult(Intent.createChooser(emailIntent, "Envoi de mail..."), SEND_MAIL_FROM_DETAIL);
            } catch (android.content.ActivityNotFoundException ex) {
                Log.e("clickListenerEmailBouto", ex.getMessage(), ex);
                Toast.makeText(getActivity(), "There is no email client installed.", Toast.LENGTH_LONG).show();
            }
        }
    };
    // Création du listener pour mettre à jour l'annonce
    private View.OnClickListener clickListenerUpdateBouton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mMode.equals(Constants.PARAM_MAJ)) {
                // Passage d'un paramètre Mise à jour
                Bundle bd = new Bundle();
                bd.putString(PostAnnonceActivity.BUNDLE_KEY_MODE, mMode);
                bd.putParcelable(PostAnnonceActivity.BUNDLE_KEY_ANNONCE, mAnnonce);

                Intent intent = new Intent();
                intent.setClass(getActivity(), PostAnnonceActivity.class).putExtras(bd);
                startActivityForResult(intent, UPDATE_DETAIL);
            }
        }
    };

    // Création du listener pour agrandir l'image
    private View.OnClickListener clickListenerImageBouton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent();
            i.setClass(getActivity(), ImageViewerActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(ImageViewerActivity.BUNDLE_KEY_URI, mAnnonce.getPhotos().get(v.getId()).getNamePhoto());
            i.putExtras(bundle);
            startActivity(i);
        }
    };

    public DetailAnnonceFragment() {
        // Required empty public constructor
    }

    public static DetailAnnonceFragment newInstance(String mode, Annonce annonce) {
        DetailAnnonceFragment fragment = new DetailAnnonceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_MODE, mode);
        args.putParcelable(ARG_PARAM_ANNONCE, annonce);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMode = getArguments().getString(ARG_PARAM_MODE);
            mAnnonce = getArguments().getParcelable(ARG_PARAM_ANNONCE);
        }


        // Fenêtre de confirmation avant de supprimer une annonce
        dialogDeleteChoice = new Dialog(getActivity());
        prgDialog = new ProgressDialog(getActivity());
        mActivity = getActivity();
        createDialogDeleteChoice();
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup viewGroup) {
        viewGroup.removeAllViewsInLayout();
        View rootView = inflater.inflate(R.layout.fragment_detail_annonces, viewGroup);
        initUI(rootView);
    }

    private void initUI(View rootView) {
        // On inflate la vue
        ButterKnife.bind(this, rootView);

        horizontalScrollView = (HorizontalScrollView) rootView.findViewById(R.id.horizontalScrollView);
        scrollImageView = (ScrollView) rootView.findViewById(R.id.scrollImageView);

        // Intégration de AdMob
//        AdView mAdView = (AdView) rootView.findViewById(R.id.adView);
//        AdRequest mAdRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(mAdRequest);


        // On crée un bouton de suppression et de mise à jour
        switch (mMode) {
            case Constants.PARAM_MAJ:
                linearButtonVis.setVisibility(View.GONE);
                btnActionDelete.setOnClickListener(clickListenerDeleteBouton);
                btnActionUpdate.setOnClickListener(clickListenerUpdateBouton);
                break;
            default:
                linearButtonMaj.setVisibility(View.GONE);
                btnActionAppel.setOnClickListener(clickListenerCallBouton);
                btnActionEmail.setOnClickListener(clickListenerEmailBouton);
                btnActionSms.setOnClickListener(clickListenerSmsBouton);
                break;
        }

        presentAnnonce();  // On présente l'annonce
        presentPhoto(); // Présentation des photo
    }

    private void presentPhoto() {
        // On enlève toutes les vues du ScrollView
        P_IMAGE_CONTAINER.removeAllViews();

        // Pour toutes les images qu'on a on va créer un nouveau imageView et insérer le bitmap à l'intérieur
        int id = 0;
        if (!mAnnonce.getPhotos().isEmpty()) {
            if (horizontalScrollView != null) {
                horizontalScrollView.setVisibility(View.VISIBLE);
            } else {
                scrollImageView.setVisibility(View.VISIBLE);
            }
            P_IMAGE_CONTAINER.setVisibility(View.VISIBLE);
            for (Photo photo : mAnnonce.getPhotos()) {
                // Récupération de la dimension standard d'une image
                int dimension = (int) getResources().getDimension(R.dimen.image_list_view);

                // Création du nouveau widget
                ImageView image = new ImageView(getActivity());
                image.setId(id);
                image.setMinimumWidth(dimension);
                image.setMaxWidth(dimension);
                image.setMinimumHeight(dimension);
                image.setMaxHeight(dimension);
                image.setScaleType(ImageView.ScaleType.FIT_CENTER);

                if (photo.getNamePhoto().contains("http://") || photo.getNamePhoto().contains("https://")) {
                    // Chargement d'une photo à partir d'internet
                    Glide.with(this).load(photo.getNamePhoto()).placeholder(R.drawable.progress_refresh).error(R.drawable.ic_camera_black).into(image);
                } else {
                    // Chargement à partir du local
                    Uri uri = Uri.parse(photo.getNamePhoto());
                    Glide.with(this).load(new File(String.valueOf(uri))).placeholder(R.drawable.progress_refresh).error(R.drawable.ic_camera_black).into(image);
                }
                P_IMAGE_CONTAINER.addView(image);

                // On affecte un clickListener sur chacune des photos
                image.setOnClickListener(clickListenerImageBouton);

                id++; // Incrémentation de la variable Id
            }
        } else {
            // Si il n'y a pas de photo, inutile d'afficher la scroll horizontal
            if (horizontalScrollView != null) {
                horizontalScrollView.setVisibility(View.GONE);
            } else {
                scrollImageView.setVisibility(View.VISIBLE);
            }
            P_IMAGE_CONTAINER.setVisibility(View.GONE);
        }
    }

    private void createDialogDeleteChoice() {
        dialogDeleteChoice.setContentView(R.layout.dialog_delete_choice);
        dialogDeleteChoice.setTitle("Supprimer l'annonce ?");

        Button dialogButtonYes = (Button) dialogDeleteChoice.findViewById(R.id.dialog_delete_yes);
        Button dialogButtonNo = (Button) dialogDeleteChoice.findViewById(R.id.dialog_delete_no);

        dialogButtonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prgDialog.setMessage(getString(R.string.dialog_msg_patience));

                // Envoi d'un webservice pour supprimer l'annonce en question
                // Définition d'un nouveau callback

                Callback<ReturnWS> myCallback = new Callback<ReturnWS>() {
                    @Override
                    public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
                        if (response.isSuccessful()) {
                            ReturnWS rs = response.body();
                            prgDialog.hide();
                            // Suppression de l'enregistrement dans la liste
                            if (rs.statusValid()) {
                                // Retour au fragment précédent
                                Toast.makeText(getActivity(), rs.getMsg(), Toast.LENGTH_LONG).show();
                                getFragmentManager().popBackStackImmediate();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ReturnWS> call, Throwable t) {
                        prgDialog.hide();
                        Toast.makeText(getActivity(), getString(R.string.dialog_failed_webservice), Toast.LENGTH_LONG).show();
                    }
                };
                prgDialog.show();
                ServiceAnnonce serviceAnnonce = new Retrofit.Builder().baseUrl(Proprietes.getServerEndpoint()).addConverterFactory(GsonConverterFactory.create()).build().create(ServiceAnnonce.class);
                Call<ReturnWS> call = serviceAnnonce.deleteAnnonce(mAnnonce.getIdANO());
                call.enqueue(myCallback);
            }
        });

        dialogButtonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDeleteChoice.dismiss();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_annonces, container, false);

        initUI(view);

        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    private void presentAnnonce() {
        String color = "#00000";

        // Récupération de toutes les valeurs de l'annonce dans les zones graphiques
        value_id_annonce.setText(String.valueOf(mAnnonce.getIdANO()));
        value_titre.setText(mAnnonce.getTitreANO());
        value_description.setText(mAnnonce.getDescriptionANO());
        value_prix_annonce.setText(Utility.convertPrice(mAnnonce.getPriceANO()));

        String maDate = mAnnonce.getDatePublished().toString();
        if (mAnnonce.getUtilisateurANO() != null) {
            telephoneUser = mAnnonce.getUtilisateurANO().getTelephoneUTI().toString();
            emailUser = mAnnonce.getUtilisateurANO().getEmailUTI();
        }

        if (mAnnonce.getIdCategorieANO() != null){
            Categorie categorie = ListeCategories.getInstance(getActivity()).getCategorieById(mAnnonce.getIdCategorieANO());
            color = categorie.getCouleurCAT();
        }

        // On tente de formater la date correctement
        value_user.setText(getString(R.string.text_post_by).concat(" ").concat(emailUser).concat(" ").concat(getString(R.string.text_pre_date)).concat(" ").concat(Utility.convertDate(maDate)));

        // Si on est en mode visualisation, on peut cliquer sur les boutons téléphoner, email et sms
        if (mMode.equals(Constants.PARAM_VIS)) {

            if (telephoneUser.isEmpty()) {
                btnActionAppel.setActivated(false);
                btnActionAppel.setVisibility(View.GONE);
                btnActionSms.setActivated(false);
                btnActionSms.setVisibility(View.GONE);
            } else {
                btnActionAppel.setActivated(true);
                btnActionAppel.setVisibility(View.VISIBLE);
                btnActionSms.setActivated(true);
                btnActionSms.setVisibility(View.VISIBLE);
            }

            if (emailUser.isEmpty()) {
                btnActionEmail.setActivated(false);
                btnActionEmail.setVisibility(View.GONE);
            } else {
                btnActionEmail.setActivated(true);
                btnActionEmail.setVisibility(View.VISIBLE);
            }
        }

        // Changement de couleur de l'action bar et du titre pour prendre celle de la catégorie
        Categorie categorie = ListeCategories.getInstance(mActivity).getCategorieById(mAnnonce.getIdCategorieANO());
        mActivity.setTitle(categorie.getNameCAT());
        if (mActivity instanceof CustomActivityInterface) {
            CustomActivityInterface myCustomActivity = (CustomActivityInterface) mActivity;
            myCustomActivity.changeColorToolBar(Color.parseColor(color));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                // On a bien fait la mise à jour de l'annonce, on va retourner aux annonces
                getFragmentManager().popBackStackImmediate();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        dialogDeleteChoice.dismiss();
        prgDialog.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dialogDeleteChoice.dismiss();
        prgDialog.dismiss();
    }
}
