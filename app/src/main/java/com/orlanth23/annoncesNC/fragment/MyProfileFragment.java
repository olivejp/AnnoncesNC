package com.orlanth23.annoncesnc.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.activity.ChangePasswordFirebaseActivity;
import com.orlanth23.annoncesnc.activity.CustomCompatActivity;
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
    EditText emailET;
    @BindView(R.id.telephoneMyProfile)
    EditText telephoneET;
    @BindView(R.id.displayName)
    EditText displayNameET;
    @BindView(R.id.action_save_change)
    Button saveBT;
    @BindView(R.id.action_desinscrire)
    Button desinscrireBT;
    @BindView(R.id.action_change_password)
    Button changePasswordBT;
    @BindView(R.id.action_deconnexion)
    Button deconnexionBT;

    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;

    private OnCompleteListener<Void> onUpdateProfileCompleteListener;
    private OnFailureListener onUpdateProfileFailureListener;
    private OnCompleteListener<Void> onFirebaseCompleteListener;
    private OnFailureListener onFirebaseFailureListener;
    private OnCompleteListener<Void> onUpdateProfileListener;
    private OnFailureListener onUpdateProfileFListener;

    private String mIdUser;
    private String mEmail;
    private String mDisplayName;
    private String mTelephone;

    private CustomCompatActivity mActivity;
    private ProgressDialog mPrgDialog;

    public static MyProfileFragment newInstance() {
        return new MyProfileFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (CustomCompatActivity) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        onFirebaseCompleteListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mPrgDialog.hide();
                if (task.isSuccessful()) {
                    mIdUser = mFirebaseUser.getUid();
                    CurrentUser.getInstance().setIdUTI(mIdUser);
                    CurrentUser.getInstance().setEmailUTI(mEmail);
                    CurrentUser.getInstance().setDisplayNameUTI(mDisplayName);
                    CurrentUser.getInstance().setTelephoneUTI(mTelephone);

                    Toast.makeText(getActivity(), getString(R.string.dialog_update_user_succeed), Toast.LENGTH_LONG).show();
                    getFragmentManager().popBackStackImmediate();
                } else {
                    Toast.makeText(getActivity(), "Appel Firebase effectuée, mais réponse invalide.", Toast.LENGTH_LONG).show();
                }
            }
        };

        onFirebaseFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mPrgDialog.hide();
                Toast.makeText(getActivity(), "Appel à la base Firebase échoué.", Toast.LENGTH_LONG).show();
            }
        };

        onUpdateProfileListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "User profile updated.");

                    // Mise à jour des infos dans la DB firebase
                    updateFirebaseDatabaseUser();
                }
            }
        };

        onUpdateProfileFListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mPrgDialog.hide();
                Toast.makeText(getActivity(), "Mise à jour des données du profil échouée.", Toast.LENGTH_LONG).show();
            }
        };

        onUpdateProfileFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mPrgDialog.hide();
                Toast.makeText(getActivity(), "Mise à jour du profil échouée.", Toast.LENGTH_LONG).show();
            }
        };

        onUpdateProfileCompleteListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // Tentative de mise à jour du nom d'affichage
                    updateDisplayName();
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_profile, container, false);
        ButterKnife.bind(this, rootView);

        emailET.setText(CurrentUser.getInstance().getEmailUTI());
        displayNameET.setText(CurrentUser.getInstance().getDisplayNameUTI());
        telephoneET.setText(String.valueOf(CurrentUser.getInstance().getTelephoneUTI()));

        // Changement de couleur de l'action bar et du titre pour prendre celle de la catégorie
        getActivity().setTitle(getString(R.string.action_profile));
        if (getActivity() instanceof CustomActivityInterface) {
            CustomActivityInterface myCustomActivity = (CustomActivityInterface) getActivity();
            int color = ContextCompat.getColor(getActivity(), R.color.ColorPrimary);
            myCustomActivity.changeColorToolBar(color);
        }
        return rootView;
    }

    // Création d'un listener pour la mise à jour
    @OnClick(R.id.action_save_change)
    public void onSaveClick() {
        Utility.hideKeyboard(getActivity());

        // Affichage d'un message de mise à jour
        mPrgDialog = new ProgressDialog(getActivity());
        mPrgDialog.setMessage("Mise à jour des informations de l'utilisateur");
        mPrgDialog.show();

        mEmail = emailET.getText().toString();
        mDisplayName = displayNameET.getText().toString();
        mTelephone = telephoneET.getText().toString();

        if (!checkUserHasChanged(mEmail, mDisplayName, mTelephone)) {
            mPrgDialog.hide();
            Toast.makeText(getActivity(), R.string.dialog_no_update, Toast.LENGTH_LONG).show();
        } else {
            if (isEmailCorrect(mEmail, emailET)) {
                // Tentative de mise à jour de l'email la Firebase Auth
                mFirebaseUser = mAuth.getCurrentUser();

                if (mFirebaseUser == null) {
                    mPrgDialog.hide();
                    Toast.makeText(mActivity, "Vous n'êtes pas authentifié.", Toast.LENGTH_LONG).show();
                } else {
                    mFirebaseUser.updateEmail(mEmail)
                            .addOnCompleteListener(onUpdateProfileCompleteListener)
                            .addOnFailureListener(onUpdateProfileFailureListener);
                }
            }
        }
    }

    private void updateFirebaseDatabaseUser() {
        mPrgDialog.setMessage("Mise à jour du profil dans la base de données.");

        // Récupération de l'id de l'utilisateur
        mIdUser = mFirebaseUser.getUid();

        // Récupération de l'utilisateur
        Utilisateur user = new Utilisateur();
        user.setIdUTI(mIdUser);
        user.setEmailUTI(mEmail);
        user.setDisplayNameUTI(mDisplayName);
        user.setTelephoneUTI(mTelephone);

        // Enregistrement de cet utilisateur dans la RealTimeDatabase de Firebase
        DatabaseReference userRef = mFirebaseDatabase.getReference("users/" + mIdUser);

        userRef.setValue(user)
                .addOnCompleteListener(onFirebaseCompleteListener)
                .addOnFailureListener(onFirebaseFailureListener);
    }

    private void updateDisplayName() {
        // Mise à jour du nom d'affichage
        mPrgDialog.setMessage("Mise à jour des infos complémentaires du profil.");

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(mDisplayName)
                .build();

        mFirebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(onUpdateProfileListener)
                .addOnFailureListener(onUpdateProfileFListener);
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
        CurrentUser.getInstance().clear();

        // Changement de couleur de l'action bar et du titre pour prendre celle de la catégorie
        Activity myActivity = getActivity();
        if (myActivity instanceof CustomActivityInterface) {
            CustomActivityInterface myCustomActivity = (CustomActivityInterface) myActivity;
            myCustomActivity.refreshMenu();
        }

        getFragmentManager().popBackStackImmediate();
    }

    private boolean checkUserHasChanged(String email, String displayName, String telephone) {
        boolean hasChanged = false;

        if (!email.equals(CurrentUser.getInstance().getEmailUTI())) {
            hasChanged = true;
        }

        if (!displayName.equals(CurrentUser.getInstance().getDisplayNameUTI())) {
            hasChanged = true;
        }

        if (!telephone.equals(String.valueOf(CurrentUser.getInstance().getTelephoneUTI()))) {
            hasChanged = true;
        }
        return hasChanged;
    }

    // When Email entered is invalid
    private boolean isEmailCorrect(String email, EditText emailEditText) {
        boolean retourOK = true;

        if (!Utility.validateEmail(email)) {
            emailEditText.setError(getString(R.string.error_invalid_email));
            emailEditText.requestFocus();
            retourOK = false;
        }
        return retourOK;
    }
}
