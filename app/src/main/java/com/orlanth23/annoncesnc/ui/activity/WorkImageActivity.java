package com.orlanth23.annoncesnc.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.utility.Constants;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WorkImageActivity extends AppCompatActivity {

    public static final String BUNDLE_KEY_MODE = "MODE";
    public static final String BUNDLE_KEY_ID = "ID";
    public static final String BUNDLE_IN_IMAGE = "BITMAP_IN";
    public static final String BUNDLE_OUT_IMAGE = "BITMAP_OUT";
    public static final String BUNDLE_OUT_MAJ = "MAJ_IMAGE";

    @BindView(R.id.work_image_button_delete_image)
    Button workimagebuttondelete;
    @BindView(R.id.work_image_button_rotate_image)
    Button workimagebuttonrotate;
    @BindView(R.id.work_image_button_save_image)
    Button workimagebuttonsave;
    @BindView(R.id.work_image_view)
    ImageView workimageview;

    private String P_MODE; // Le mode Création Modification qu'on a reçu en paramètre
    private Bitmap P_BITMAP; // Le bitmap qu'on va travailler et renvoyer
    private int P_ID; // Id de la photo dans l'arrayList
    private byte[] P_BYTEARRAY;
    private ByteArrayOutputStream P_STREAM = new ByteArrayOutputStream();
    private boolean P_RETURN_BYTE_ARRAY = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_image);
        ButterKnife.bind(this);

        // Récupération de l'action bar.
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
            ab.setDisplayShowHomeEnabled(false);
        }

        // Soit on vient pour la première fois et savedInstance = null
        // soit on revient sur cette activity et savedInstance contient déjà toutes les données que l'on attend.
        Bundle bundle;
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        } else {
            bundle = getIntent().getExtras();
        }

        // Récupération des paramètres
        P_MODE = bundle.getString(BUNDLE_KEY_MODE);
        if (P_MODE != null) {

            // Changement des titres du bouton Supprimer/Annuler selon le mode dans lequel on entre
            // En mode MAJ, on récupère l'ID dans l'arraylist du bitmap
            switch (P_MODE) {
                case Constants.PARAM_CRE:
                    workimagebuttondelete.setText(getString(R.string.cancel));
                    break;
                case Constants.PARAM_MAJ:
                    P_ID = bundle.getInt(BUNDLE_KEY_ID);
                    workimagebuttondelete.setText(getString(R.string.action_delete));
                    break;
            }

            // On va récupérer le bytearray, puis la bitmap qui est rattachée
            P_BYTEARRAY = bundle.getByteArray(BUNDLE_IN_IMAGE);
            if (P_BYTEARRAY != null) {
                P_BITMAP = BitmapFactory.decodeByteArray(P_BYTEARRAY, 0, P_BYTEARRAY.length);
            }

            // On affecte le bitmap qu'on a récupéré dans l'ImageView
            workimageview.setImageBitmap(P_BITMAP);
        }

        // Création d'un listener pour faire tourner l'image
        View.OnClickListener clickListenerButtonRotate = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (P_BITMAP != null) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(P_BITMAP, P_BITMAP.getWidth(), P_BITMAP.getHeight(), true);
                    P_BITMAP = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                    workimageview.setImageBitmap(P_BITMAP);

                    // On était en mise à jour et on bien fait une modif sur notre image, donc on renverra bien un Byte array
                    if (P_MODE.equals(Constants.PARAM_MAJ)) {
                        P_RETURN_BYTE_ARRAY = true;
                    }
                }
            }
        };

        // Création d'un listener pour sauver et quitter
        View.OnClickListener clickListenerButtonSave = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (P_BITMAP != null) {
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();

                    // En mode MAJ on va aussi renvoyer la position dans l'ArrayList de notre photo
                    // Pour que le programme appelant puisse faire sa mise à jour
                    switch (P_MODE) {
                        case Constants.PARAM_MAJ:
                            bundle.putInt(BUNDLE_KEY_ID, P_ID);
                            break;
                        case Constants.PARAM_CRE:
                            P_RETURN_BYTE_ARRAY = true; // On est en création, alors forcément on renverra un byte array
                            break;
                    }

                    bundle.putBoolean(BUNDLE_OUT_MAJ, P_RETURN_BYTE_ARRAY);

                    if (P_RETURN_BYTE_ARRAY) {
                        // Envoi du bitmap par un byteArray
                        P_STREAM.reset();
                        P_BITMAP.compress(Bitmap.CompressFormat.PNG, 100, P_STREAM);
                        P_BYTEARRAY = P_STREAM.toByteArray();
                        bundle.putByteArray(BUNDLE_OUT_IMAGE, P_BYTEARRAY);
                    }

                    intent.putExtras(bundle);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        };

        // Création d'un listener pour annuler ou supprimer l'image
        View.OnClickListener clickListenerButtonDelete = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();

                // En mode MAJ on va aussi renvoyer la position dans l'ArrayList de notre photo
                if (P_MODE.equals(Constants.PARAM_MAJ)) {
                    bundle.putInt(BUNDLE_KEY_ID, P_ID);
                }

                intent.putExtras(bundle);
                setResult(Activity.RESULT_CANCELED, intent);
                finish();

            }
        };

        // Affectation des listeners
        workimagebuttonrotate.setOnClickListener(clickListenerButtonRotate);
        workimagebuttonsave.setOnClickListener(clickListenerButtonSave);
        workimagebuttondelete.setOnClickListener(clickListenerButtonDelete);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_KEY_MODE, P_MODE);

        // Sauvegarde des paramètres
        if (P_MODE != null) {
            switch (P_MODE) {

                // On est en mode Création
                case Constants.PARAM_CRE:
                    P_STREAM.reset();
                    P_BITMAP.compress(Bitmap.CompressFormat.PNG, 100, P_STREAM);
                    P_BYTEARRAY = P_STREAM.toByteArray();
                    outState.putByteArray(BUNDLE_IN_IMAGE, P_BYTEARRAY);
                    break;

                // On est en mode Mise à jour
                case Constants.PARAM_MAJ:
                    // En mise à jour on a besoin du bitmap et de son id (position dans l'arrayList)
                    outState.putInt(BUNDLE_KEY_ID, P_ID);

                    P_STREAM.reset();
                    P_BITMAP.compress(Bitmap.CompressFormat.PNG, 100, P_STREAM);
                    P_BYTEARRAY = P_STREAM.toByteArray();
                    outState.putByteArray(BUNDLE_IN_IMAGE, P_BYTEARRAY);
                    break;
            }
        }
    }
}
