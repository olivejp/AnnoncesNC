package com.orlanth23.annoncesnc.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.adapter.SpinnerAdapter;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.dto.Annonce;
import com.orlanth23.annoncesnc.dto.Categorie;
import com.orlanth23.annoncesnc.dto.CurrentUser;
import com.orlanth23.annoncesnc.dto.Photo;
import com.orlanth23.annoncesnc.list.ListeCategories;
import com.orlanth23.annoncesnc.utility.Constants;
import com.orlanth23.annoncesnc.utility.ReturnClassUFTS;
import com.orlanth23.annoncesnc.utility.UploadFileToServer;
import com.orlanth23.annoncesnc.utility.Utility;
import com.orlanth23.annoncesnc.webservice.Proprietes;
import com.orlanth23.annoncesnc.webservice.ReturnWS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByFragmentManager;

public class PostAnnonceActivity extends CustomRetrofitCompatActivity implements NoticeDialogFragment.NoticeDialogListener {
    // Activity request codes
    public static final int DIALOG_REQUEST_IMAGE = 100;
    public static final String BUNDLE_KEY_ANNONCE = "ANNONCE";
    public static final String BUNDLE_KEY_URI = "URI_TEMP";
    public static final String BUNDLE_KEY_MODE = "MODE";
    public static final String BUNDLE_KEY_TITRE = "TITRE";
    public static final String BUNDLE_KEY_BITMAP_ARRAY = "BITMAP_ARRAY";
    public static final String BUNDLE_KEY_PHOTO_TO_DELETE = "PHOTO_TO_DELETE";
    private static final int DIALOG_GALLERY_IMAGE = 200;
    private static final int CODE_WORK_IMAGE_CREATION = 300;
    private static final int CODE_WORK_IMAGE_MODIFICATION = 400;
    private static final String TAG = PostAnnonceActivity.class.getName();
    @BindView(R.id.buttonPhoto)
    Button buttonPhoto;
    @BindView(R.id.spinner_categorie)
    Spinner spinnerCategorie;
    @BindView(R.id.edit_description_annonce)
    EditText descriptionText;
    @BindView(R.id.edit_prix_annonce)
    EditText prixText;
    @BindView(R.id.edit_titre_annonce)
    EditText titreText;
    @BindView(R.id.post_error)
    TextView textError;
    @BindView(R.id.button_save_annonce)
    Button btnSaveAnnonce;
    @BindView(R.id.image_container)
    LinearLayout imageContainer;
    @BindView(R.id.horizontalScrollView)
    HorizontalScrollView horizontalScrollView;
    private Annonce mAnnonce;
    private Uri mFileUriTemp;
    private String mMode;
    private String mTitreActivity;
    private Dialog dialogImageChoice;
    private AppCompatActivity mActivity = this;
    private UploadFileToServer mUploadFileToServer;
    private ArrayList<String> P_PHOTO_TO_SEND = new ArrayList<>();
    private ArrayList<Photo> P_PHOTO_TO_DELETE = new ArrayList<>();
    private ArrayList<Photo> P_PHOTO_TO_UPDATE = new ArrayList<>();
    private int mCptPhotoTotal;

    private Callback<ReturnWS> callbackDeletePhoto = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            if (response.isSuccessful()) {
                Log.d("callbackDeletePhoto", "Suppression achevée avec succès");
            } else {
                Log.d("callbackDeletePhoto", response.body().getMsg());
            }
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            t.printStackTrace();
            SendDialogByFragmentManager(getFragmentManager(), "Erreur lors de l'appel d'un Webservice", NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, TAG);
        }
    };

    private void onFailurePostUploadPhoto(){
        mCptPhotoTotal++;
        if (mCptPhotoTotal == mAnnonce.getPhotos().size()) {
            mUploadFileToServer.execute();
        }
        SendDialogByFragmentManager(getFragmentManager(), getString(R.string.dialog_failed_webservice), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, TAG);
    }
    private Callback<ReturnWS> callbackPostUploadPhoto = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            if (response.isSuccessful()) {
                ReturnWS rs = response.body();
                mCptPhotoTotal++;
                if (rs.statusValid()) {
                    // Si la photo n'existe pas encore, on l'ajoute à la liste des photos à envoyer
                    if (!rs.getMsg().equals("PHOTO_ALREADY_EXIST")) {
                        P_PHOTO_TO_SEND.add(rs.getMsg()); // Ajout de la photo à la liste des photos à envoyer
                    }

                    // Quand on a parcouru toutes les photos, on les envoie.
                    if (mCptPhotoTotal == mAnnonce.getPhotos().size()) {
                        mUploadFileToServer.execute();
                    }
                } else {
                    SendDialogByFragmentManager(getFragmentManager(), rs.getMsg(), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, TAG);
                }
            } else{
                onFailurePostUploadPhoto();
            }
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            onFailurePostUploadPhoto();
        }
    };
    private Callback<ReturnWS> callbackPostAnnonce = new Callback<ReturnWS>() {
        @Override
        public void onResponse(Call<ReturnWS> call, Response<ReturnWS> response) {
            if (response.isSuccessful()) {
                ReturnWS rs = response.body();
                if (rs.statusValid()) {
                    prgDialog.setProgress(100);
                    mAnnonce.setIdANO(rs.getId());  // Récupération de l'ID de l'annonce, dans le cas d'une mise à jour c'est utile, sinon c'est inutile mais on le fait quand meme

                    deletePhotos(); // S'il y a des photos à supprimer on le fait ici

                    if (!mAnnonce.getPhotos().isEmpty()) { // On a réussi à sauver l'annonce, on va maintenant uploader les photos
                        postPhotos();
                    } else {
                        endPostAnnonceActivity(Activity.RESULT_OK, getString(R.string.dialog_success_post));
                    }
                } else {
                    endPostAnnonceActivity(Activity.RESULT_CANCELED, rs.getMsg());
                }
            }
        }

        @Override
        public void onFailure(Call<ReturnWS> call, Throwable t) {
            SendDialogByFragmentManager(getFragmentManager(), getString(R.string.dialog_failed_webservice), NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_ERROR, TAG);
        }
    };
    // Création du listener sur chaque image du scrollView
    private View.OnClickListener clickListenerImageView = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Récupération de la bitmap présent dans l'imageview
            ImageView imageView = (ImageView) v;
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

            // Transformation de l'image en byteArray, avant de le mettre dans un bundle et de le filer à l'activité suivante
            byte[] p_BYTEARRAY = Utility.transformBitmapToByteArray(bitmap);

            // On va appeler l'activity avec le bitmap qu'on veut modifier et son numéro dans l'arraylist
            // qui servira à son retour pour le mettre à jour.
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            intent.setClass(mActivity, WorkImageActivity.class);
            bundle.putString(WorkImageActivity.BUNDLE_KEY_MODE, Constants.PARAM_MAJ);
            bundle.putByteArray(WorkImageActivity.BUNDLE_IN_IMAGE, p_BYTEARRAY);
            bundle.putInt(WorkImageActivity.BUNDLE_KEY_ID, v.getId());
            intent.putExtras(bundle);
            startActivityForResult(intent, CODE_WORK_IMAGE_MODIFICATION);
        }
    };
    /**
     * Création du listener pour sauver l'annonce
     * Appel des webServices pour poster l'annonce et pour poster la photo
     */
    private View.OnClickListener clickListenerSaveButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Vérification que tous les champs soient remplis
            if (checkAnnonceCreate()) {
                prgDialog.setMessage(getString(R.string.dialog_sending_post));
                prgDialog.show();

                switch (mMode) {
                    case Constants.PARAM_CRE:
                    case Constants.PARAM_MAJ:
                        doPost();
                        break;
                }
            }
        }
    };
    // Création du listener pour appeler la capture d'image
    private View.OnClickListener clickListenerButtonPhoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialogImageChoice.show();
        }
    };

    // Constructor du fragment
    public PostAnnonceActivity() {
    }

    private void deletePhotos() {
        // In update case, user could have delete some photos. So we call deletePhoto WS.
        if (!P_PHOTO_TO_DELETE.isEmpty()) {
            for (Photo photo : P_PHOTO_TO_DELETE) {
                Integer idPhoto = photo.getIdPhoto();
                Call<ReturnWS> callDeletePhoto = retrofitService.deletePhoto(mAnnonce.getIdANO(), idPhoto);
                callDeletePhoto.enqueue(callbackDeletePhoto);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate de la vue
        setContentView(R.layout.activity_annonce_post);

        // Récupération des zones graphiques
        ButterKnife.bind(this);

        ListeCategories listeCategories = ListeCategories.getInstance(this);

        dialogImageChoice = new Dialog(this);

        // Création de la progress Dialog
        prgDialog = new ProgressDialog(this);
        prgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        prgDialog.setCancelable(false);

        // Récupération de la liste des catégories
        ArrayList<Categorie> myListCategorie = listeCategories.getListCategorie();
        if (myListCategorie != null && !myListCategorie.isEmpty()) {
            SpinnerAdapter adapter = new SpinnerAdapter(this, R.layout.drawer_list_categorie, myListCategorie);
            spinnerCategorie.setAdapter(adapter);
        }

        // Evenement sur le spinner
        spinnerCategorie.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Categorie cat = (Categorie) parent.getItemAtPosition(position);
                mAnnonce.setCategorieANO(cat);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        // Création d'un UploadFileToServer
        mUploadFileToServer = new UploadFileToServer() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                prgDialog.setMessage("Envoi des photos...");
                prgDialog.setMax(P_PHOTO_TO_SEND.size());
            }

            @Override
            protected String doInBackground(Void... params) {
                Gson gson = new Gson();
                String reponse = null;
                for (String sourceFile : P_PHOTO_TO_SEND) {
                    reponse = postHttpRequest(sourceFile, Proprietes.getServerPageUpload(), Proprietes.getServerDirectoryUploads());
                    if (reponse != null) {
                        if (!reponse.isEmpty()) {
                            ReturnClassUFTS rs = gson.fromJson(reponse, ReturnClassUFTS.class);
                            if (rs != null) {
                                if (rs.getError().equals("false")) {
                                    prgDialog.setProgress(prgDialog.getProgress() + 1);
                                }
                            }
                        }
                    }
                }
                return reponse;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                endPostAnnonceActivity(Activity.RESULT_OK, getString(R.string.dialog_success_post));
            }
        };


        // Création du dialog qui va nous permettre de choisir d'où viennent les images
        createDialogImageChoice();

        // Récupération des paramètres
        if (savedInstanceState != null) {
            mAnnonce = savedInstanceState.getParcelable(BUNDLE_KEY_ANNONCE);
            mFileUriTemp = savedInstanceState.getParcelable(BUNDLE_KEY_URI);
            mMode = savedInstanceState.getString(BUNDLE_KEY_MODE);
            mTitreActivity = savedInstanceState.getString(BUNDLE_KEY_TITRE);
            P_PHOTO_TO_DELETE = savedInstanceState.getParcelableArrayList(BUNDLE_KEY_PHOTO_TO_DELETE);
            if (mAnnonce != null) {
                spinnerCategorie.setSelection(listeCategories.getIndexByName(mAnnonce.getCategorieANO().getNameCAT()));
            }
        } else {

            // Récupération des paramètres
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                mMode = bundle.getString(BUNDLE_KEY_MODE);

                if (mMode != null) {
                    switch (mMode) {
                        case Constants.PARAM_CRE:
                            // On est en mMode Création
                            mAnnonce = new Annonce();
                            mTitreActivity = getString(R.string.action_post);
                            break;
                        case Constants.PARAM_MAJ:
                            // On est en mMode Mise à jour, on va récupérer l'annonce qu'on veut mettre à jour
                            mAnnonce = bundle.getParcelable(BUNDLE_KEY_ANNONCE);
                            mTitreActivity = getString(R.string.action_update);
                            if (mAnnonce != null) {
                                titreText.setText(mAnnonce.getTitreANO());
                                descriptionText.setText(mAnnonce.getDescriptionANO());
                                prixText.setText(String.valueOf(mAnnonce.getPriceANO()));
                                spinnerCategorie.setSelection(listeCategories.getIndexByName(mAnnonce.getCategorieANO().getNameCAT()));
                            }
                            break;
                    }
                }
            }
        }

        // Libellé de message d'erreur est invisible au départ; il n'apparait que s'il y a des erreurs
        textError.setVisibility(View.GONE);

        // Cette méthode va présenter la liste des photos présentes dans notre ArrayList
        presentPhoto();

        // Recherche de la toolbar et changement du titre
        ActionBar tb = getSupportActionBar();
        if (tb != null) {
            tb.setTitle(mTitreActivity);
        }

        // Changement du texte du bouton selon le mMode avec lequel on rentre
        if (mMode != null) {
            switch (mMode) {
                case Constants.PARAM_MAJ:
                    btnSaveAnnonce.setText(getString(R.string.text_update_annonce));
                    break;
                case Constants.PARAM_CRE:
                    btnSaveAnnonce.setText(getString(R.string.text_save_annonce));
                    break;
            }
        }

        // On attache les listeners
        buttonPhoto.setOnClickListener(clickListenerButtonPhoto);
        btnSaveAnnonce.setOnClickListener(clickListenerSaveButton);
    }

    /**
     * Méthode de création de dialog qui va permettre d'interroger l'utilisateur sur l'action qu'il veut entreprendre
     * Choix entre prendre une nouvelle photo et choisir une photo depuis la gallerie
     */
    private void createDialogImageChoice() {
        dialogImageChoice.setContentView(R.layout.dialog_photo_choice);
        dialogImageChoice.setTitle("Que voulez vous faire ?");

        Button dialogButtonAnnuler = (Button) dialogImageChoice.findViewById(R.id.dialog_button_annuler);
        Button dialogButtonNewPhoto = (Button) dialogImageChoice.findViewById(R.id.dialog_button_new_photo);
        Button dialogButtonGallery = (Button) dialogImageChoice.findViewById(R.id.dialog_button_gallery_photo);

        // if button is clicked, close the custom dialog
        dialogButtonAnnuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogImageChoice.dismiss();
            }
        });

        dialogButtonNewPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start the image capture Intent
                CurrentUser user = CurrentUser.getInstance();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mFileUriTemp = Utility.getOutputMediaFileUri(Constants.MEDIA_TYPE_IMAGE, user.getIdUTI(), TAG);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUriTemp);
                startActivityForResult(intent, DIALOG_REQUEST_IMAGE);
            }
        });

        dialogButtonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, DIALOG_GALLERY_IMAGE);
            }
        });
    }

    /*
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BUNDLE_KEY_ANNONCE, mAnnonce);
        outState.putParcelable(BUNDLE_KEY_URI, mFileUriTemp);
        outState.putString(BUNDLE_KEY_MODE, mMode);
        outState.putString(BUNDLE_KEY_TITRE, mTitreActivity);
        outState.putParcelableArrayList(BUNDLE_KEY_PHOTO_TO_DELETE, P_PHOTO_TO_DELETE);

        super.onSaveInstanceState(outState);
    }

    /**
     * Méthode de Post pour créer/mettre à jour l'annonce sur le serveur
     */
    private void doPost() {

        final Integer idAnnonce = mAnnonce.getIdANO();
        Integer idCat = mAnnonce.getCategorieANO().getIdCAT();
        Integer idUser = mAnnonce.getOwnerANO().getIdUTI();
        String titre = mAnnonce.getTitreANO().replace("'", "''");
        String description = mAnnonce.getDescriptionANO().replace("'", "''");
        Integer prix = mAnnonce.getPriceANO();

        // ------------------------------------------Appel premier retrofitservice---------------------------------------------
        Call<ReturnWS> call = retrofitService.postAnnonce(idCat, idUser, idAnnonce, titre, description, prix);
        call.enqueue(callbackPostAnnonce);
    }


    /**
     * Lecture de la liste mAnnonce.getPhotos()
     * pour chaque enregistrement, on va vérifier si l'enregistrement existe déjà sur notre serveur, avec le même nom.
     * -s'il existe déjà avec le même nom, on ne fait rien : ça veut dire que l'utilisateur n'a pas touché à cette image.
     * -si on trouve un nom différent, il faut faire une mise à jour et renvoyer la nouvelle Bitmap.
     * -si on ne trouve rien, c'est qu'on a une nouvelle image, on envoie donc les infos de cette photo plus le Bitmap
     */
    private void postPhotos() {
        prgDialog.setProgress(0);
        mCptPhotoTotal = 0;
        for (Photo photo : mAnnonce.getPhotos()) {
            Call<ReturnWS> callPostUploadPhoto = retrofitService.postPhoto(mAnnonce.getIdANO(), photo.getIdPhoto(), photo.getNamePhoto());
            callPostUploadPhoto.enqueue(callbackPostUploadPhoto);
        }

    }

    private void endPostAnnonceActivity(int p_activityResult, String message) {
        prgDialog.hide();

        Utility.hideKeyboard(this);

        if (p_activityResult == Activity.RESULT_OK) {
            if (getParent() == null) {
                setResult(p_activityResult, new Intent());
            } else {
                getParent().setResult(p_activityResult, new Intent());
            }
            Toast.makeText(PostAnnonceActivity.this, message, Toast.LENGTH_LONG).show();
            finish();
        } else {
            SendDialogByFragmentManager(getFragmentManager(), message, NoticeDialogFragment.TYPE_BOUTON_OK, NoticeDialogFragment.TYPE_IMAGE_INFORMATION, null);
        }

    }

    private Boolean checkAnnonceCreate() {
        textError.setVisibility(View.GONE);
        // Récupération des views
        boolean save = true;
        View focus = null;

        if (CurrentUser.isConnected()) {
            if (titreText.getText().toString().length() == 0) {
                titreText.setError(getString(R.string.error_post_title));
                focus = titreText;
                save = false;
            }

            if (descriptionText.getText().toString().length() == 0) {
                descriptionText.setError(getString(R.string.error_post_description));
                focus = descriptionText;
                save = false;
            }
            if (prixText.getText().toString().length() == 0) {
                prixText.setError(getString(R.string.error_post_price));
                focus = descriptionText;
                save = false;
            }
            if (mAnnonce.getCategorieANO() == null) {
                textError.setVisibility(View.VISIBLE);
                textError.setText(getString(R.string.error_choose_category));
                focus = spinnerCategorie;
                save = false;
            }

            // on sauve l'annonce
            if (save) {
                mAnnonce.setDescriptionANO(descriptionText.getText().toString());
                mAnnonce.setPriceANO(Integer.parseInt(prixText.getText().toString()));
                mAnnonce.setTitreANO(titreText.getText().toString());
                mAnnonce.setOwnerANO(CurrentUser.getInstance());
            } else {
                focus.requestFocus();
            }

        } else {
            textError.setText(getString(R.string.error_need_user_connection));
            save = false;
        }
        return save;
    }

    /**
     * Va lire P_WORKING_BITMAP et va insérer des imageView dans la liste graphique
     */
    private void presentPhoto() {

        // On enlève toutes les vues du ScrollView
        imageContainer.removeAllViews();

        if (!mAnnonce.getPhotos().isEmpty()) {
            horizontalScrollView.setVisibility(View.VISIBLE);
            // Pour toutes les images qu'on a, on va créer un nouveau imageView et insérer le bitmap à l'intérieur
            int id = 0;
            for (Photo photo : mAnnonce.getPhotos()) {
                // Récupération de la dimension standard d'une image
                int dimension = (int) getResources().getDimension(R.dimen.image_list_view);

                // Création du nouveau widget
                ImageView image = new ImageView(this);
                image.setId(id);
                image.setMinimumWidth(dimension);
                image.setMaxWidth(dimension);
                image.setMinimumHeight(dimension);
                image.setMaxHeight(dimension);
                image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                image.setContentDescription(photo.getNamePhoto());

                if (photo.getNamePhoto().contains("http://") || photo.getNamePhoto().contains("https://")) {
                    // Chargement d'une photo à partir d'internet
                    Glide.with(this).load(photo.getNamePhoto()).placeholder(R.drawable.ic_camera).error(R.drawable.ic_camera_black).into(image);
                } else {
                    // Chargement à partir du local
                    Uri uri = Uri.parse(photo.getNamePhoto());
                    Glide.with(this).load(new File(String.valueOf(uri))).placeholder(R.drawable.ic_camera).error(R.drawable.ic_camera_black).into(image);
                }
                imageContainer.addView(image);

                // On affecte un clickListener sur chacune des photos
                image.setOnClickListener(clickListenerImageView);

                id++; // Incrémentation de la variable Id
            }
        } else {
            horizontalScrollView.setVisibility(View.GONE);
        }
    }

    /**
     * Méthode qui va récupérer une image et la retailler et ensuite va l'attacher à l'annonce
     */
    protected void travailImage(byte[] byteArray, boolean nouvelleImg, int position) {
        String path;

        if (byteArray != null) {
            File f = Utility.getOutputMediaFile(Constants.MEDIA_TYPE_IMAGE, CurrentUser.getInstance().getIdUTI(), TAG);
            try {
                if (f != null) {
                    if (f.createNewFile()) {
                        FileOutputStream fo = new FileOutputStream(f);
                        fo.write(byteArray);
                        fo.close();
                        path = f.getPath();

                        if (nouvelleImg) {
                            // On est en mode création
                            // On insère un nouvel enregistrement dans l'arrayList
                            mAnnonce.getPhotos().add(new Photo(0, path, mAnnonce.getIdANO()));
                        } else {
                            // On est en mode modification
                            // On va se positionner sur la bonne photo pour faire la modification de chemin
                            mAnnonce.getPhotos().get(position).setNamePhoto(path);
                        }
                        // Si le fichier temporaire existe, il faut le supprimer
                        File file = new File(String.valueOf(mFileUriTemp));
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
            } catch (IOException e) {
                Log.e("IOException", e.getMessage(), e);
            }
        }
    }

    private void callWorkingImageActivity(Uri uri, String mode, int requestCode) {
        Bitmap bitmap;
        Bitmap bitmapResized;
        byte[] byteArray;

        Intent intent = new Intent();
        intent.setClass(this, WorkImageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(WorkImageActivity.BUNDLE_KEY_MODE, mode);

        // Récupération du bitmap à partir de l'Uri qu'on a reçu.
        InputStream imageStream;
        try {
            imageStream = getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(imageStream);
            bitmapResized = Utility.resizeBitmap(bitmap, Constants.MAX_IMAGE_SIZE);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmapResized.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byteArray = stream.toByteArray();
            bundle.putByteArray(WorkImageActivity.BUNDLE_IN_IMAGE, byteArray);
        } catch (FileNotFoundException e) {
            Log.e(e.getClass().getName(), e.getMessage(), e);
        }

        intent.putExtras(bundle);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int code_request, int resultCode, Intent data) {
        // if the result is capturing Image

        int position;
        byte[] byteArray;

        switch (code_request) {
            case CODE_WORK_IMAGE_CREATION:
                if (resultCode == RESULT_OK) {

                    if (data.getExtras().getBoolean(WorkImageActivity.BUNDLE_OUT_MAJ)) {
                        // Récupération du Byte Array
                        byteArray = data.getExtras().getByteArray(WorkImageActivity.BUNDLE_OUT_IMAGE);
                        travailImage(byteArray, true, 0);
                    }
                    presentPhoto();
                }
                break;
            case CODE_WORK_IMAGE_MODIFICATION:
                switch (resultCode) {
                    case RESULT_OK:
                        // Récupération de l'ancienne position
                        position = data.getExtras().getInt(WorkImageActivity.BUNDLE_KEY_ID);

                        // Récupération du BITMAP
                        if (data.getExtras().getBoolean(WorkImageActivity.BUNDLE_OUT_MAJ)) {
                            byteArray = data.getExtras().getByteArray(WorkImageActivity.BUNDLE_OUT_IMAGE);
                            travailImage(byteArray, false, position);
                        }
                        presentPhoto();
                        break;

                    // On veut supprimer la photo
                    case RESULT_CANCELED:
                        if (data != null) {
                            if (data.getExtras() != null) {
                                position = data.getExtras().getInt(WorkImageActivity.BUNDLE_KEY_ID);
                                Photo photo = mAnnonce.getPhotos().get(position);
                                P_PHOTO_TO_DELETE.add(photo);
                                mAnnonce.getPhotos().remove(position);
                                presentPhoto();
                            }
                        }
                }
                break;
            case DIALOG_REQUEST_IMAGE:
                dialogImageChoice.dismiss(); // On ferme la boite de dialogue
                if (resultCode == RESULT_OK) {
                    callWorkingImageActivity(mFileUriTemp, Constants.PARAM_CRE, CODE_WORK_IMAGE_CREATION);  // On va appeler WorkImageActivity
                } else if (resultCode == RESULT_CANCELED) {
                    // user cancelled Image capture
                    Toast.makeText(mActivity,
                            getString(R.string.action_cancelled),
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    // failed to capture image
                    Toast.makeText(mActivity,
                            getString(R.string.action_failed),
                            Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case DIALOG_GALLERY_IMAGE:
                dialogImageChoice.dismiss();
                if (resultCode == RESULT_OK) {
                    // On revient de la galerie où on a choisit une image.
                    Uri uri = data.getData();

                    // On va appeler WorkImageActivity avec l'uri récupéré
                    callWorkingImageActivity(uri, Constants.PARAM_CRE, CODE_WORK_IMAGE_CREATION);

                } else if (resultCode == RESULT_CANCELED) {
                    // user cancelled Image capture
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.action_cancelled),
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    // failed to capture image
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.action_failed),
                            Toast.LENGTH_SHORT)
                            .show();
                }
                break;
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        finish();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        prgDialog.dismiss();
        dialogImageChoice.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();
        prgDialog.dismiss();
        dialogImageChoice.dismiss();
    }
}
