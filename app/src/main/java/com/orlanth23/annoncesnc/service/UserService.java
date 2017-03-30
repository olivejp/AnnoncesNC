package com.orlanth23.annoncesnc.service;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.dto.Utilisateur;
import com.orlanth23.annoncesnc.interfaces.CallbackUpdateDisplayName;
import com.orlanth23.annoncesnc.interfaces.CallbackUpdateFirebaseUser;
import com.orlanth23.annoncesnc.interfaces.CustomChangePasswordCallback;
import com.orlanth23.annoncesnc.interfaces.CustomLostPasswordCallback;
import com.orlanth23.annoncesnc.interfaces.CustomUpdateEmailCallback;
import com.orlanth23.annoncesnc.interfaces.CustomUserSignCallback;

public class UserService {
    private static String ROOT_USERS_REF = "users/";

    public static void updateFirebaseUser(FirebaseDatabase mDatabase, final Activity activity, Utilisateur user, final CallbackUpdateFirebaseUser callbackUpdateFirebaseUser) {
        OnCompleteListener<Void> onFirebaseCompleteListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(activity, activity.getString(R.string.dialog_update_user_succeed), Toast.LENGTH_LONG).show();
                callbackUpdateFirebaseUser.onCompleteUpdateFirebase();
            }
        };

        OnFailureListener onFirebaseFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity, "Appel à la base Firebase échoué.", Toast.LENGTH_LONG).show();
                callbackUpdateFirebaseUser.onFailureUpdateFirebase();
            }
        };

        // Enregistrement de cet utilisateur dans la RealTimeDatabase de Firebase
        DatabaseReference userRef = mDatabase.getReference(ROOT_USERS_REF + user.getIdUTI());

        userRef.setValue(user)
            .addOnCompleteListener(onFirebaseCompleteListener)
            .addOnFailureListener(onFirebaseFailureListener);
    }

    public static void updateDisplayName(FirebaseAuth mAuth, final Activity activity, String displayName, final CallbackUpdateDisplayName callbackUpdateDisplayName) {
        // Mise à jour du nom d'affichage


        OnCompleteListener<Void> onUpdateProfileListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                callbackUpdateDisplayName.onCompleteUpdateDisplayName();
            }
        };

        OnFailureListener onUpdateProfileFListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity, "Mise à jour des données du profil échouée.", Toast.LENGTH_LONG).show();
                callbackUpdateDisplayName.onFailureUpdateDisplayName();
            }
        };

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build();

        mAuth.getCurrentUser().updateProfile(profileUpdates)
            .addOnCompleteListener(onUpdateProfileListener)
            .addOnFailureListener(onUpdateProfileFListener);
    }

    public static void updateEmailUser(FirebaseAuth mAuth, final Activity activity, String email, final CustomUpdateEmailCallback customUpdateEmailCallback) {
        // Affichage d'un message de mise à jour
        OnCompleteListener<Void> onUpdateProfileCompleteListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                customUpdateEmailCallback.onCompleteUpdateEmail();
            }
        };

        OnFailureListener onUpdateProfileFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity, "Mise à jour du profil échouée.", Toast.LENGTH_LONG).show();
                customUpdateEmailCallback.onFailureUpdateEmail();
            }
        };

        // Tentative de mise à jour de l'email la Firebase Auth
        mAuth.getCurrentUser().updateEmail(email)
            .addOnCompleteListener(onUpdateProfileCompleteListener)
            .addOnFailureListener(onUpdateProfileFailureListener);

    }

    public static void lostPassword(FirebaseAuth mAuth, final Activity activity, String email, final CustomLostPasswordCallback customLostPasswordCallback) {

        OnCompleteListener<Void> onCompleteListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(activity, activity.getString(R.string.dialog_password_send), Toast.LENGTH_LONG).show();
                    customLostPasswordCallback.onCompleteLostPassword();
                }
            }
        };

        OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                customLostPasswordCallback.onFailureLostPassword();
            }
        };

        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(onCompleteListener)
            .addOnFailureListener(onFailureListener);
    }

    public static void updatePassword(FirebaseAuth mAuth, final Activity activity, String password, final CustomChangePasswordCallback customChangePasswordCallback) {

        // Si pas d'utilisateur connecté, inutile de continuer.
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            return;

        OnCompleteListener<Void> onCompleteListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(activity, "Le mot de passe a été correctement changé.", Toast.LENGTH_LONG).show();
                customChangePasswordCallback.onCompleteChangePassword();

            }
        };

        OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity, "Echec de la mise à jour du mot de passe.", Toast.LENGTH_LONG).show();
                customChangePasswordCallback.onFailureChangePassword();
            }
        };

        mAuth.getCurrentUser().updatePassword(password)
            .addOnCompleteListener(onCompleteListener)
            .addOnFailureListener(onFailureListener);
    }

    public static void sign(final FirebaseAuth auth, final FirebaseDatabase database, final Activity activity, String email, String password, final CustomUserSignCallback customUserSignCallback) {
        OnCompleteListener<AuthResult> onCompleteListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final FirebaseUser mFirebaseUser = auth.getCurrentUser();
                    DatabaseReference userRef = database.getReference(ROOT_USERS_REF + mFirebaseUser.getUid());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Utilisateur user = dataSnapshot.getValue(Utilisateur.class);
                            Toast.makeText(activity, "Connecté avec le compte " + mFirebaseUser.getDisplayName() + ".", Toast.LENGTH_LONG).show();
                            customUserSignCallback.onCompleteUserSign(user);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(activity, "Un problème est survenue pendant votre authentification.", Toast.LENGTH_LONG).show();
                            customUserSignCallback.onCancelledUserSign();
                        }
                    });
                }else{
                    Toast.makeText(activity, "Un problème est survenue pendant votre authentification.", Toast.LENGTH_LONG).show();
                    customUserSignCallback.onFailureUserSign();
                }
            }
        };

        OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                try {
                    throw e;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                customUserSignCallback.onFailureUserSign();
            }
        };

        // On tente de se connecter au serveur Firebase.
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(onCompleteListener)
            .addOnFailureListener(onFailureListener);
    }
}
