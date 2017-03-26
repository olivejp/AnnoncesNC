package com.orlanth23.annoncesnc.activity;

import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
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
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.adapter.SpinnerAdapter;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.dto.Annonce;
import com.orlanth23.annoncesnc.dto.Categorie;
import com.orlanth23.annoncesnc.dto.CurrentUser;
import com.orlanth23.annoncesnc.dto.Photo;
import com.orlanth23.annoncesnc.dto.StatutAnnonce;
import com.orlanth23.annoncesnc.dto.StatutPhoto;
import com.orlanth23.annoncesnc.list.ListeCategories;
import com.orlanth23.annoncesnc.provider.ProviderContract;
import com.orlanth23.annoncesnc.provider.contract.AnnonceContract;
import com.orlanth23.annoncesnc.provider.contract.PhotoContract;
import com.orlanth23.annoncesnc.sync.AnnoncesAuthenticatorService;
import com.orlanth23.annoncesnc.utility.Constants;
import com.orlanth23.annoncesnc.utility.Utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostAnnonceActivity extends CustomRetrofitCompatActivity implements NoticeDialogFragment.NoticeDialogListener {
    // Activity request codes
    public static final int DIALOG_REQUEST_IMAGE = 100;
    public static final String BUNDLE_KEY_ANNONCE = "ANNONCE";
    public static final String BUNDLE_KEY_URI = "URI_TEMP";
    public static final String BUNDLE_KEY_MODE = "MODE";
    public static final String BUNDLE_KEY_TITRE = "TITRE";
    private static final int DIALOG_GALLERY_IMAGE = 200;
    private static final int CODE_WORK_IMAGE_CREATION = 300;
    private static final int CODE_WORK_IMAGE_MODIFICATION = 400;
    private static final int REQUEST_CAMERA_PERMISSION = 999;

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

    // Création du listener sur chaque image du scrollView
    private View.OnClickListener clickListenerImageView = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            // Récupération de la bitmap présent dans l'imageview
            ImageView imageView = (ImageView) v;

            Glide.with(mActivity)
                    .load(imageView.getContentDescription())
                    .asBitmap()
                    .into(new BitmapImageViewTarget(imageView) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            super.onResourceReady(resource, glideAnimation);

                            // Transformation de l'image en byteArray, avant de le mettre dans un bundle et de le filer à l'activité suivante
                            byte[] byteArray = Utility.transformBitmapToByteArray(resource);

                            // On va appeler l'activity avec le bitmap qu'on veut modifier et son numéro dans l'arraylist
                            // qui servira à son retour pour le mettre à jour.
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            intent.setClass(mActivity, WorkImageActivity.class);
                            bundle.putString(WorkImageActivity.BUNDLE_KEY_MODE, Constants.PARAM_MAJ);
                            bundle.putByteArray(WorkImageActivity.BUNDLE_IN_IMAGE, byteArray);
                            bundle.putInt(WorkImageActivity.BUNDLE_KEY_ID, v.getId());
                            intent.putExtras(bundle);
                            startActivityForResult(intent, CODE_WORK_IMAGE_MODIFICATION);
                        }
                    });
        }
    };


    // Création du listener pour appeler la capture d'image
    private View.OnClickListener clickListenerButtonPhoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialogImageChoice.show();
        }
    };

    // Constructor de l'activité
    public PostAnnonceActivity() {
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
                mAnnonce.setIdCategorieANO(cat.getIdCAT());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Création du dialog qui va nous permettre de choisir d'où viennent les images
        createDialogImageChoice();

        // Récupération des paramètres
        if (savedInstanceState != null) {
            mAnnonce = savedInstanceState.getParcelable(BUNDLE_KEY_ANNONCE);
            mFileUriTemp = savedInstanceState.getParcelable(BUNDLE_KEY_URI);
            mMode = savedInstanceState.getString(BUNDLE_KEY_MODE);
            mTitreActivity = savedInstanceState.getString(BUNDLE_KEY_TITRE);
            if (mAnnonce != null) {
                Categorie categorie = listeCategories.getCategorieById(mAnnonce.getIdCategorieANO());
                spinnerCategorie.setSelection(listeCategories.getIndexByName(categorie.getNameCAT()));
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
                            mAnnonce.setUUIDANO(UUID.randomUUID().toString());
                            mAnnonce.setStatutANO(StatutAnnonce.ToPost.valeur());
                            mTitreActivity = getString(R.string.action_post);
                            break;
                        case Constants.PARAM_MAJ:
                            // On est en mMode Mise à jour, on va récupérer l'annonce qu'on veut mettre à jour
                            mAnnonce = bundle.getParcelable(BUNDLE_KEY_ANNONCE);
                            mTitreActivity = getString(R.string.action_update);
                            if (mAnnonce != null) {
                                mAnnonce.setStatutANO(StatutAnnonce.ToUpdate.valeur());
                                titreText.setText(mAnnonce.getTitreANO());
                                descriptionText.setText(mAnnonce.getDescriptionANO());
                                prixText.setText(String.valueOf(mAnnonce.getPriceANO()));
                                Categorie categorie = listeCategories.getCategorieById(mAnnonce.getIdCategorieANO());
                                spinnerCategorie.setSelection(listeCategories.getIndexByName(categorie.getNameCAT()));
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

        // Recherche de la toolbar et changement du mTitre
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
    }

    @RequiresApi(23)
    private void requestPermission() {
        if ((checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) ||
                (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            callCaptureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callCaptureIntent();
            }
        }
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

                // Demande de la permission pour utiliser la camera
                if (Build.VERSION.SDK_INT >= 23) {
                    requestPermission();
                } else {
                    callCaptureIntent();
                }
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

    private void callCaptureIntent() {
        // start the image capture Intent
        CurrentUser user = CurrentUser.getInstance();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mFileUriTemp = Utility.getOutputMediaFileUri(Constants.MEDIA_TYPE_IMAGE, user.getIdUTI(), TAG);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUriTemp);
        startActivityForResult(intent, DIALOG_REQUEST_IMAGE);
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

        super.onSaveInstanceState(outState);
    }

    /**
     * Création de la méthode pour sauver l'annonce
     * On va l'insérer dans le content Provider avec un statut ToSend
     */
    @OnClick(R.id.button_save_annonce)
    public void onClick(View v) {
        // Vérification que tous les champs soient remplis
        if (validateAnnonce()) {

            ContentValues values = new ContentValues();
            putAnnonceInContentValue(values, mAnnonce);

            // Sauvegarde de l'annonce dans le contentProvider avant d'appeler le syncAdapter pour envoi
            getContentResolver().insert(ProviderContract.AnnonceEntry.CONTENT_URI, values);

            // Insertion dans le contentProvider des photos
            for (Photo photo : mAnnonce.getPhotos()) {
                putPhotoInContentValue(values, photo);
                getContentResolver().insert(ProviderContract.PhotoEntry.CONTENT_URI, values);
            }

            // Appel du Sync pour envoyer la données
            ContentResolver.requestSync(AnnoncesAuthenticatorService.getAccount(), ProviderContract.CONTENT_AUTHORITY, Bundle.EMPTY);
        }
    }

    private void putPhotoInContentValue(ContentValues contentValues, Photo photo) {
        contentValues.clear();
        contentValues.put(PhotoContract.COL_UUID_PHOTO, photo.getUUIDPhoto());
        contentValues.put(PhotoContract.COL_CHEMIN_LOCAL_PHOTO, photo.getPathPhoto());
        contentValues.put(PhotoContract.COL_UUID_ANNONCE, photo.getIdAnnoncePhoto());
        contentValues.put(PhotoContract.COL_STATUT_PHOTO, photo.getStatutPhoto());
    }

    private void putAnnonceInContentValue(ContentValues contentValues, Annonce annonce) {
        // Récupération des données sur le layout
        contentValues.clear();
        contentValues.put(AnnonceContract.COL_UUID_ANNONCE, annonce.getUUIDANO());
        contentValues.put(AnnonceContract.COL_ID_CATEGORY, annonce.getIdCategorieANO());
        contentValues.put(AnnonceContract.COL_ID_UTILISATEUR, annonce.getUtilisateurANO().getIdUTI());
        contentValues.put(AnnonceContract.COL_TITRE_ANNONCE, annonce.getTitreANO().replace("'", "''"));
        contentValues.put(AnnonceContract.COL_DESCRIPTION_ANNONCE, annonce.getDescriptionANO().replace("'", "''"));
        contentValues.put(AnnonceContract.COL_PRIX_ANNONCE, annonce.getPriceANO());
        contentValues.put(AnnonceContract.COL_STATUT_ANNONCE, annonce.getStatutANO());
    }

    @NonNull
    private Boolean validateAnnonce() {
        textError.setVisibility(View.GONE);
        // Récupération des views
        boolean save = true;
        View focus = null;

        if (CurrentUser.getInstance().isConnected()) {
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
            if (mAnnonce.getIdCategorieANO() == null) {
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
                mAnnonce.setUtilisateurANO(CurrentUser.getInstance());
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
     * Va lire toutes les images dans la liste de l'annonce et va insérer des imageView dans la liste graphique
     */
    private void presentPhoto() {

        // On enlève toutes les vues du ScrollView
        imageContainer.removeAllViews();

        if (!mAnnonce.getPhotos().isEmpty()) {
            horizontalScrollView.setVisibility(View.VISIBLE);

            // Pour toutes les images qu'on a, on va créer un nouveau imageView et insérer le bitmap à l'intérieur
            int id = 0;

            // Récupération de la dimension standard d'une image
            int dimension = (int) getResources().getDimension(R.dimen.image_list_view);

            for (Photo photo : mAnnonce.getPhotos()) {
                if (!photo.getStatutPhoto().equals(StatutPhoto.ToDelete.valeur())) {
                    // Création du nouveau widget
                    ImageView image = new ImageView(this);
                    image.setId(id);
                    image.setMinimumWidth(dimension);
                    image.setMaxWidth(dimension);
                    image.setMinimumHeight(dimension);
                    image.setMaxHeight(dimension);
                    image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    image.setContentDescription(photo.getPathPhoto());

                    if (photo.getPathPhoto().contains("http://") || photo.getPathPhoto().contains("https://")) {
                        // Chargement d'une photo à partir d'internet
                        Glide.with(this).load(photo.getPathPhoto()).placeholder(R.drawable.ic_camera).error(R.drawable.ic_camera_black).into(image);
                    } else {
                        // Chargement à partir du local
                        Uri uri = Uri.parse(photo.getPathPhoto());
                        Glide.with(this).load(new File(String.valueOf(uri))).placeholder(R.drawable.ic_camera).error(R.drawable.ic_camera_black).into(image);
                    }
                    imageContainer.addView(image);

                    // On affecte un clickListener sur chacune des photos
                    image.setOnClickListener(clickListenerImageView);

                    // Incrémentation de la variable Id
                    id++;
                }
            }
        } else {
            // Inutile d'afficher le scroll horiontal s'il n'y a pas de photo
            horizontalScrollView.setVisibility(View.GONE);
        }
    }

    /**
     * Méthode qui va récupérer une image et la retailler et ensuite va l'attacher à l'annonce
     * On va créer un nouveau fichier et y enregistrer le ByteArray qu'on a recu.
     * Si c'est une nouvelle photo on ajoute cette photo à la liste des photos
     * Si c'est une photo existante on met juste à jour dans notre liste son path
     * On supprime ensuite le fichier temporaire dont on s'était servi
     *
     * @param byteArray   Nouvelle Photo en format Byte Array
     * @param nouvelleImg Boolean pour savoir si c'est une nouvelle photo ou pas
     * @param position    Si c'est une photo existante qu'on met à jour, ceci est la position dans la liste des photos de l'annonce
     */
    @NonNull
    protected boolean travailImage(byte[] byteArray, boolean nouvelleImg, int position) {
        String path;
        boolean retour = true;

        if (byteArray == null) {
            retour = false;
        }

        File f = Utility.getOutputMediaFile(Constants.MEDIA_TYPE_IMAGE, CurrentUser.getInstance().getIdUTI(), TAG);

        if (f == null) {
            retour = false;
        }

        try {
            if (f.createNewFile()) {
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(byteArray);
                fo.close();
                path = f.getPath();

                if (nouvelleImg) {
                    // On est en mode création
                    // On insère un nouvel enregistrement dans l'arrayList
                    Photo photo = new Photo(UUID.randomUUID().toString(), path, mAnnonce.getUUIDANO(), StatutPhoto.ToSend.valeur());
                    mAnnonce.getPhotos().add(photo);
                } else {
                    // On est en mode modification
                    // On va se positionner sur la bonne photo pour faire la modification de chemin
                    Photo photo = mAnnonce.getPhotos().get(position);
                    photo.setPathPhoto(path);
                    photo.setStatutPhoto(StatutPhoto.ToUpdate.valeur());
                }
            }
        } catch (IOException e) {
            Log.e("IOException", TAG + ":travailImage:" + e.getMessage(), e);
            retour = false;
        }
        return retour;
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

    private void deleteTempFile() {
        // Si le fichier temporaire existe, il faut le supprimer
        File file = new File(String.valueOf(mFileUriTemp));
        if (file.exists()) {
            if (!file.delete()) {
                Log.e("FileDelete", TAG + ":travailImage:Fichier Temporaire non supprimé");
            }
        }
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
                        if (travailImage(byteArray, true, 0)) {
                            deleteTempFile();
                        }
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
                            if (travailImage(byteArray, false, position)) {
                                deleteTempFile();
                            }
                        }
                        presentPhoto();
                        break;

                    // On veut supprimer la photo
                    case RESULT_CANCELED:
                        if (data != null) {
                            if (data.getExtras() != null) {
                                position = data.getExtras().getInt(WorkImageActivity.BUNDLE_KEY_ID);
                                Photo photo = mAnnonce.getPhotos().get(position);
                                photo.setStatutPhoto(StatutPhoto.ToDelete.valeur());
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
