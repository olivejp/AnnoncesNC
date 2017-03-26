package com.orlanth23.annoncesnc.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.activity.ChangePasswordFirebaseActivity;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.dto.CurrentUser;
import com.orlanth23.annoncesnc.dto.Utilisateur;
import com.orlanth23.annoncesnc.interfaces.CustomActivityInterface;
import com.orlanth23.annoncesnc.utility.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByActivity;

public class MyProfileFragment extends Fragment {

    public static final String TAG = MyProfileFragment.class.getName();
    public final static int CODE_CHANGE_PASSWORD = 600;

    @BindView(R.id.emailMyProfile)
    EditText emailMyProfile;
    @BindView(R.id.telephoneMyProfile)
    EditText telephoneMyProfile;
    @BindView(R.id.action_save_change)
    Button action_save_change;
    @BindView(R.id.action_desinscrire)
    Button action_desinscrire;
    @BindView(R.id.action_change_password)
    Button action_change_password;
    @BindView(R.id.action_deconnexion)
    Button action_deconnexion;

    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;

    private FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            mFirebaseUser = firebaseAuth.getCurrentUser();
            CurrentUser cu = CurrentUser.getInstance();
            if (mFirebaseUser != null) {
                cu.setConnected(true);
                Log.d(TAG, "onAuthStateChanged:signed_in:" + mFirebaseUser.getUid());
            } else {
                cu.setConnected(false);
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
        }
    };

    public static MyProfileFragment newInstance() {
        return new MyProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_profile, container, false);
        ButterKnife.bind(this, rootView);

        emailMyProfile.setText(CurrentUser.getInstance().getEmailUTI());
        telephoneMyProfile.setText(String.valueOf(CurrentUser.getInstance().getTelephoneUTI()));

        // Changement de couleur de l'action bar et du titre pour prendre celle de la catégorie
        getActivity().setTitle(getString(R.string.action_profile));
        if (getActivity() instanceof CustomActivityInterface) {
            CustomActivityInterface myCustomActivity = (CustomActivityInterface) getActivity();
            int color = ContextCompat.getColor(getActivity(), R.color.ColorPrimary);
            myCustomActivity.changeColorToolBar(color);
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // Création d'un listener pour la mise à jour
    @OnClick(R.id.action_save_change)
    public void onSaveClick() {
        final String email = emailMyProfile.getText().toString();
        final String telephone = telephoneMyProfile.getText().toString();
        if (checkUserHasChanged(email, telephone)) {
            if (checkUpdate(email)) {

                mFirebaseUser = mAuth.getCurrentUser();

                // On désactive le bouton, le temps qu'on récupère une réponse
                action_save_change.setEnabled(false);

                // Tentative de mise à jour dans la Firebase Auth
                mFirebaseUser.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Utilisateur utilisateur = new Utilisateur();
                            utilisateur.setIdUTI(mFirebaseUser.getUid());
                            utilisateur.setTelephoneUTI(telephone);
                            utilisateur.setEmailUTI(email);

                            // Tentative de mise à jour dans Firebase Database
                            DatabaseReference userRef = mFirebaseDatabase.getReference("users/" + mFirebaseUser.getUid());
                            userRef.setValue(utilisateur).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        CurrentUser.getInstance().setEmailUTI(email);
                                        CurrentUser.getInstance().setTelephoneUTI(telephone);
                                        Toast.makeText(getActivity(), getString(R.string.dialog_update_user_succeed), Toast.LENGTH_LONG).show();

                                        View view = getView();
                                        if (view != null) {
                                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                        }
                                        getFragmentManager().popBackStackImmediate();
                                    } else {
                                        Toast.makeText(getActivity(), getString(R.string.dialog_failed_webservice), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        } else {
            Toast.makeText(getActivity(), R.string.dialog_no_update, Toast.LENGTH_LONG).show();
        }
    }

    // Création du listener pour se désinscrire
    @OnClick(R.id.action_desinscrire)
    public void onUnregisterClick() {
        // On va envoyer un message de confirmation à l'utilisateur
        SendDialogByActivity(getActivity(), getString(R.string.dialog_confirm_unregister), NoticeDialogFragment.TYPE_BOUTON_YESNO, NoticeDialogFragment.TYPE_IMAGE_ERROR, Utility.DIALOG_TAG_UNREGISTER);
        Utility.hideKeyboard(getActivity());
    }

    // Création du listener pour se désinscrire, on va appeler l'activity ChangePassword
    @OnClick(R.id.action_change_password)
    public void onChangePasswordClick() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), ChangePasswordFirebaseActivity.class);
        startActivityForResult(intent, CODE_CHANGE_PASSWORD);
    }

    // Création d'un listener pour se déconnecter
    @OnClick(R.id.action_deconnexion)
    public void onClick() {
        CurrentUser.getInstance().setIdUTI(null);
        CurrentUser.getInstance().setEmailUTI(null);
        CurrentUser.getInstance().setTelephoneUTI(null);
        CurrentUser.getInstance().setConnected(false);

        // Changement de couleur de l'action bar et du titre pour prendre celle de la catégorie
        Activity myActivity = getActivity();
        if (myActivity instanceof CustomActivityInterface) {
            CustomActivityInterface myCustomActivity = (CustomActivityInterface) myActivity;
            myCustomActivity.refreshMenu();
        }

        getFragmentManager().popBackStackImmediate();
    }

    private boolean checkUserHasChanged(String email, String telephone) {
        Boolean hasChanged = false;

        if (!email.equals(CurrentUser.getInstance().getEmailUTI())) {
            hasChanged = true;
        }

        if (!telephone.equals(String.valueOf(CurrentUser.getInstance().getTelephoneUTI()))) {
            hasChanged = true;
        }

        return hasChanged;
    }

    private boolean checkUpdate(String email) {
        boolean retourOK = true;

        // Vérification que l'utilisateur est bien connecté
        mFirebaseUser = mAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            emailMyProfile.setError("Vous n'êtes pas connecté.");
            emailMyProfile.requestFocus();
            retourOK = false;
        }

        // When Email entered is invalid
        if (!Utility.validateEmail(email)) {
            emailMyProfile.setError(getString(R.string.error_invalid_email));
            emailMyProfile.requestFocus();
            retourOK = false;
        }

        return retourOK;
    }
}
