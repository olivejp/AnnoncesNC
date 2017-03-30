package com.orlanth23.annoncesnc.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.activity.ChangePasswordFirebaseActivity;
import com.orlanth23.annoncesnc.activity.CustomCompatActivity;
import com.orlanth23.annoncesnc.dialog.NoticeDialogFragment;
import com.orlanth23.annoncesnc.dto.CurrentUser;
import com.orlanth23.annoncesnc.dto.Utilisateur;
import com.orlanth23.annoncesnc.interfaces.CallbackUpdateDisplayName;
import com.orlanth23.annoncesnc.interfaces.CallbackUpdateFirebaseUser;
import com.orlanth23.annoncesnc.interfaces.CustomActivityInterface;
import com.orlanth23.annoncesnc.interfaces.CustomUpdateEmailCallback;
import com.orlanth23.annoncesnc.service.UserService;
import com.orlanth23.annoncesnc.utility.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.orlanth23.annoncesnc.utility.Utility.SendDialogByActivity;

public class MyProfileFragment extends Fragment implements CallbackUpdateFirebaseUser, CustomUpdateEmailCallback, CallbackUpdateDisplayName {

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

    private String mIdUser;
    private String mEmail;
    private String mDisplayName;
    private String mTelephone;

    private CustomCompatActivity mActivity;

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

        mEmail = emailET.getText().toString();
        mDisplayName = displayNameET.getText().toString();
        mTelephone = telephoneET.getText().toString();

        if (!checkUserHasChanged(mEmail, mDisplayName, mTelephone)) {
            Toast.makeText(getActivity(), R.string.dialog_no_update, Toast.LENGTH_LONG).show();
        } else {
            if (isEmailCorrect(mEmail, emailET)) {
                // Tentative de mise à jour de l'email la Firebase Auth
                mFirebaseUser = mAuth.getCurrentUser();

                if (mFirebaseUser == null) {
                    Toast.makeText(mActivity, "Vous n'êtes pas authentifié.", Toast.LENGTH_LONG).show();
                } else {
                    UserService.updateEmailUser(mAuth, getActivity(), mEmail, this);
                }
            }
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
        CurrentUser.getInstance().clear();

        // Changement de couleur de l'action bar et du titre pour prendre celle de la catégorie
        Activity myActivity = getActivity();
        if (myActivity instanceof CustomActivityInterface) {
            CustomActivityInterface myCustomActivity = (CustomActivityInterface) myActivity;
            myCustomActivity.refreshProfileMenu();
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

    @Override
    public void onCompleteUpdateEmail() {
        UserService.updateDisplayName(mAuth, getActivity(), mDisplayName, this);
    }

    @Override
    public void onCompleteUpdateDisplayName() {
        Utilisateur user = new Utilisateur();
        user.setIdUTI(mAuth.getCurrentUser().getUid());
        user.setEmailUTI(mEmail);
        user.setDisplayNameUTI(mDisplayName);
        user.setTelephoneUTI(mTelephone);

        UserService.updateFirebaseUser(mAuth, mFirebaseDatabase, getActivity(), user, this);
    }

    @Override
    public void onCompleteUpdateFirebase() {
        mIdUser = mAuth.getCurrentUser().getUid();
        CurrentUser.getInstance().setIdUTI(mIdUser);
        CurrentUser.getInstance().setEmailUTI(mEmail);
        CurrentUser.getInstance().setDisplayNameUTI(mDisplayName);
        CurrentUser.getInstance().setTelephoneUTI(mTelephone);
        getFragmentManager().popBackStackImmediate();
    }

    @Override
    public void onFailureUpdateEmail() {    }
    @Override
    public void onFailureUpdateDisplayName() {    }
    @Override
    public void onFailureUpdateFirebase() {    }
}
