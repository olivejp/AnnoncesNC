package com.orlanth23.annoncesnc.service;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.orlanth23.annoncesnc.dto.CurrentUser;
import com.orlanth23.annoncesnc.dto.Utilisateur;
import com.orlanth23.annoncesnc.interfaces.CustomChangePasswordCallback;
import com.orlanth23.annoncesnc.interfaces.CustomSignFirebaseUserCallback;

public class UserService {
    private static ProgressDialog prgDialog;

    public static void updatePassword(FirebaseAuth mAuth, final Activity activity, String password, final CustomChangePasswordCallback customChangePasswordCallback) {

        // Si pas d'utilisateur connecté, inutile de continuer.
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
            return;

        final ProgressDialog prgDialog = new ProgressDialog(activity);
        prgDialog.setMessage("Mise à jour du mot de passe.");
        prgDialog.show();

        OnCompleteListener<Void> onCompleteListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                prgDialog.hide();
                if (task.isSuccessful()) {
                    Toast.makeText(activity, "Le mot de passe a été correctement changé.", Toast.LENGTH_LONG).show();
                    customChangePasswordCallback.methodOnComplete();
                }
            }
        };

        OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                prgDialog.hide();
                Toast.makeText(activity, "Echec de la mise à jour du mot de passe.", Toast.LENGTH_LONG).show();
                customChangePasswordCallback.methodOnFailure();
            }
        };

        mAuth.getCurrentUser().updatePassword(password)
                .addOnCompleteListener(onCompleteListener)
                .addOnFailureListener(onFailureListener);
    }

    public static void sign(final FirebaseAuth auth, final FirebaseDatabase database, final Activity activity, String email, String password, final CustomSignFirebaseUserCallback customSignFirebaseUserCallback) {
        prgDialog = new ProgressDialog(activity);
        prgDialog.setMessage("Authentification en cours.");
        prgDialog.show();

        OnCompleteListener<AuthResult> onCompleteListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final FirebaseUser mFirebaseUser = auth.getCurrentUser();
                    DatabaseReference userRef = database.getReference("users/" + mFirebaseUser.getUid());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            prgDialog.hide();
                            Utilisateur user = dataSnapshot.getValue(Utilisateur.class);
                            CurrentUser.getInstance().setUser(user);
                            Toast.makeText(activity, "Connecté avec le compte " + mFirebaseUser.getDisplayName() + " !", Toast.LENGTH_LONG).show();
                            customSignFirebaseUserCallback.methodOnComplete();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            prgDialog.hide();
                            Toast.makeText(activity, "Un problème est survenue pendant votre authentification.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };

        OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                prgDialog.hide();
                try {
                    throw e;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                customSignFirebaseUserCallback.methodOnFailure();
            }
        };

        // On tente de se connecter au serveur Firebase.
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(onCompleteListener)
                .addOnFailureListener(onFailureListener);
    }
}
