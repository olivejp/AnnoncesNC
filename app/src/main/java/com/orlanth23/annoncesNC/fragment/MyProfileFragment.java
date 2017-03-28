package com.orlanth23.annoncesnc.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    @BindView(R.id.checkBox_remember_me_login)
    CheckBox checkBoxRememberMe;

    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private OnCompleteListener<Void> onUpdateProfileCompleteListener;
    private OnFailureListener onUpdateProfileFailureListener;
    private OnCompleteListener<Void> onFirebaseCompleteListener;
    private OnFailureListener onFirebaseFailureListener;
    private String mEmail;
    private String mIdUser;
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

        onUpdateProfileFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mPrgDialog.hide();
            }
        };

        onUpdateProfileCompleteListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Utilisateur utilisateur = new Utilisateur();
                    utilisateur.setIdUTI(mFirebaseUser.getUid());
                    utilisateur.setEmailUTI(mFirebaseUser.getEmail());
                    utilisateur.setTelephoneUTI(mTelephone);

                    // Tentative de mise à jour dans Firebase Database
                    DatabaseReference userRef = mFirebaseDatabase.getReference("users/" + mFirebaseUser.getUid());
                    userRef.setValue(utilisateur).addOnCompleteListener(onFirebaseCompleteListener).addOnFailureListener(onFirebaseFailureListener);
                }
            }
        };
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

    // Création d'un listener pour la mise à jour
    @OnClick(R.id.action_save_change)
    public void onSaveClick() {
        Utility.hideKeyboard(getActivity());

        // Affichage d'un message de mise à jour
        mPrgDialog = mActivity.getPrgDialog();
        mPrgDialog.setMessage("Mise à jour des informations de l'utilisateur");
        mPrgDialog.show();

        mEmail = emailMyProfile.getText().toString();
        mTelephone = telephoneMyProfile.getText().toString();
        if (checkUserHasChanged(mEmail, mTelephone)) {
            if (checkUpdate(mEmail)) {
                mFirebaseUser = mAuth.getCurrentUser();

                // Tentative de mise à jour dans la Firebase Auth
                mFirebaseUser.updateEmail(mEmail).addOnCompleteListener(onUpdateProfileCompleteListener).addOnFailureListener(onUpdateProfileFailureListener);
            }
        } else {
            mPrgDialog.hide();
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
