package com.orlanth23.annoncesnc.sync;

import android.accounts.Account;
import android.app.NotificationManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.dto.AnnonceFirebase;
import com.orlanth23.annoncesnc.dto.PhotoFirebase;
import com.orlanth23.annoncesnc.dto.StatutAnnonce;
import com.orlanth23.annoncesnc.dto.StatutPhoto;
import com.orlanth23.annoncesnc.list.ListeCategories;
import com.orlanth23.annoncesnc.list.ListeStats;
import com.orlanth23.annoncesnc.provider.ProviderContract;
import com.orlanth23.annoncesnc.provider.contract.AnnonceContract;
import com.orlanth23.annoncesnc.provider.contract.InfosServerContract;
import com.orlanth23.annoncesnc.provider.contract.PhotoContract;
import com.orlanth23.annoncesnc.utility.Utility;
import com.orlanth23.annoncesnc.webservice.InfoServer;
import com.orlanth23.annoncesnc.webservice.Proprietes;
import com.orlanth23.annoncesnc.webservice.ServiceAnnonce;
import com.orlanth23.annoncesnc.webservice.ServiceRest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.orlanth23.annoncesnc.provider.AnnoncesProvider.sSelectionAnnoncesByStatut;
import static com.orlanth23.annoncesnc.provider.AnnoncesProvider.sSelectionPhotosByStatut;
import static com.orlanth23.annoncesnc.provider.ProviderContract.AnnonceEntry;
import static com.orlanth23.annoncesnc.provider.ProviderContract.PhotoEntry;

public class AnnoncesSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = AnnoncesSyncAdapter.class.getName();
    private static final Gson gson = new Gson();
    private static final ServiceRest serviceRest = new Retrofit.Builder().baseUrl(Proprietes.getServerEndpoint()).addConverterFactory(GsonConverterFactory.create()).build().create(ServiceRest.class);
    private static final ServiceAnnonce serviceAnnonce = new Retrofit.Builder().baseUrl(Proprietes.getServerEndpoint()).addConverterFactory(GsonConverterFactory.create()).build().create(ServiceAnnonce.class);
    private static List<String> exceptionMessage = new ArrayList<>();
    private ContentResolver mContentResolver;
    private Context mContext;
    private int nbAnnoncesDeleted;
    private int nbAnnoncesSend;
    private int nbPhotosSend;
    private int nbMessageSend;
    private int nbPhotosDeleted;
    private StorageReference mStorageRef;
    private FirebaseDatabase mDatabase;

    private String photoLocalPath;
    private InfoServer infoServer = new InfoServer();

    public AnnoncesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        mContext = context;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance();
    }

    public AnnoncesSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        mContext = context;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        if (Utility.checkWifiAndMobileData(mContext)) {
            // Suppression des annonces en attente de suppression
            deleteAnnonceEnAttente();

            // On envoie les annonces qui sont en attente
            postAnnonceEnAttente();

            // On envoie les photos
            postPhotoEnAttente();

            // Suppression des photos
            deletePhotoEnAttente();

            // Récupération du nombre d'utilisateur
            getInfoServer();

            // Envoie d'une notification si quelque chose a été fait
            if (nbAnnoncesDeleted + nbAnnoncesSend + nbPhotosSend + nbMessageSend + nbPhotosDeleted > 0) {
                sendNotification();
            }
        }
    }

    private void sendNotification() {
        String textToSend = "";

        if (nbAnnoncesDeleted > 0) {
            textToSend = textToSend.concat("Nombre d'annonce(s) supprimée(s) : ").concat(String.valueOf(nbAnnoncesDeleted));
        }

        if (nbPhotosDeleted > 0) {
            textToSend = textToSend.concat("\n").concat("Nombre de photo(s) supprimée(s) : ").concat(String.valueOf(nbPhotosDeleted));
        }

        if (nbAnnoncesSend > 0) {
            textToSend = textToSend.concat("\n").concat("Nombre d'annonce(s) postée(s) : ").concat(String.valueOf(nbAnnoncesSend));
        }

        if (nbPhotosSend > 0) {
            textToSend = textToSend.concat("\n").concat("Nombre de photo(s) envoyée(s) : ").concat(String.valueOf(nbPhotosSend));
        }

        if (nbMessageSend > 0) {
            textToSend = textToSend.concat("\n").concat("Nombre de message(s) envoyé(s) : ").concat(String.valueOf(nbMessageSend));
        }

        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_annonces)
                .setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(textToSend);

        // Sets an ID for the notification
        int mNotificationId = 001;

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
            (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    private void getInfoServer() {
        mDatabase.getReference("annonces").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                infoServer.setNbAnnonce(dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mDatabase.getReference("utilisateurs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                infoServer.setNbUtilisateur(dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        // Mise à jour des informations dans le ContentProvider
        ContentValues contentValues = new ContentValues();
        contentValues.put(InfosServerContract.COL_NB_ANNONCE, infoServer.getNbAnnonce());
        contentValues.put(InfosServerContract.COL_NB_UTILISATEUR, infoServer.getNbUtilisateur());
        String where = InfosServerContract._ID + "=?";
        String[] args = new String[]{"1"};
        int rowUpdated = mContentResolver.update(ProviderContract.InfosServerEntry.CONTENT_URI, contentValues, where, args);
        if (rowUpdated == 1) {
            // Mise à jour réussie
            ListeStats listeStats = ListeStats.getInstance(mContext);
            ListeStats.getDataFromProvider(mContext);
        } else {
            exceptionMessage.add("getInfoServer:Mise à jour du Provider échouée");
        }

        // Mise à jour de la liste des catégories avec le nombre reçu par le WS
        ListeCategories.setNbAnnonceFromHashMap(mContext, infoServer.getNbAnnonceByCategorie());
    }

    private void deletePhotoEnAttente() {
        nbPhotosDeleted = 0;

        // Lecture des photos supprimées Hors Connexion qui sont maintenant à supprimer sur le serveur
        Cursor cursorPhotosToDelete;
        String[] selectionArgs = new String[]{StatutPhoto.ToDelete.valeur()};
        cursorPhotosToDelete = mContentResolver.query(PhotoEntry.CONTENT_URI, null, sSelectionPhotosByStatut, selectionArgs, null);

        if (cursorPhotosToDelete != null) {
            while (cursorPhotosToDelete.moveToNext()) {
                PhotoFirebase photoFirebase = getPhotoFromCursor(cursorPhotosToDelete);
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference dRef = firebaseDatabase.getReference("photos/" + photoFirebase.getIdPhoto());
                dRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            exceptionMessage.add("Suppression de la photo échouée.");
                        }
                    }
                });

            }
            cursorPhotosToDelete.close();
        }
    }

    private void deleteAnnonceEnAttente() {
        nbAnnoncesDeleted = 0;

        // Lecture des annonces supprimées Hors Connexion qui sont maintenant à supprimer sur le serveur
        Cursor cursorAnnoncesToDelete;
        String[] selectionArgs = new String[]{StatutAnnonce.ToDelete.valeur()};
        cursorAnnoncesToDelete = mContentResolver.query(AnnonceEntry.CONTENT_URI, null, sSelectionAnnoncesByStatut, selectionArgs, null);

        if (cursorAnnoncesToDelete != null) {
            while (cursorAnnoncesToDelete.moveToNext()) {
                AnnonceFirebase annonceWs = getAnnonceFromCursor(cursorAnnoncesToDelete);
            }
            cursorAnnoncesToDelete.close();
        }
    }

    private void sendPhotoToFirebaseDatabase(UUID idPhoto, PhotoFirebase photo) {
        // Insertion de notre photo dans notre FirebaseDatabase
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference photoDatabaseRef = mDatabase.getReference("photos/" + idPhoto);
        photoDatabaseRef.setValue(photo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    exceptionMessage.add("postPhotoEnAttente:Insertion dans Firebase échouée.");
                }
            }
        });
    }

    private void sendPhotoToFirebaseStorage(UUID UUIDPhoto, Uri fileImage, @Nullable Integer idAnnonce) {
        // Récupération de la référence
        StorageReference photoRef = mStorageRef.child("photos/" + UUIDPhoto + ".png");

        // On initialise la metadata, s'il y a quelque chose à mettre dedans
        StorageMetadata metadata = null;
        if (idAnnonce != null) {
            // On attache des metadata au fichier
            metadata = new StorageMetadata.Builder()
                .setContentType("image/png")
                .setCustomMetadata("Id annonce", String.valueOf(idAnnonce))
                .build();

            // On met à jour les métadata
            photoRef.updateMetadata(metadata);
        }

        // On envoie le fichier
        photoRef.putFile(fileImage)
            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    nbPhotosSend++;
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    exceptionMessage.add("postPhotoEnAttente:Impossible d'envoyer la photo " + photoLocalPath);
                }
            });
    }

    /**
     * Cette méthode va lire toutes les photos présentes dans le content provider avec le statut ToSend.
     * Pour chaque enregistrement, on va envoyer la photo sur Firebase Storage
     * On va ensuite créer un enregistrement dans Firebase Database pour l'archiver.
     */
    private void postPhotoEnAttente() {

        // Recherche dans le ContentProvider de toutes les photos à envoyer
        Cursor cursorPhotoToSend;
        String[] selectionArgs = new String[]{StatutPhoto.ToSend.valeur()};
        cursorPhotoToSend = mContentResolver.query(PhotoEntry.CONTENT_URI, null, sSelectionPhotosByStatut, selectionArgs, null);

        if (cursorPhotoToSend != null) {
            while (cursorPhotoToSend.moveToNext()) {
                // Récupération de la photo présente dans le curseur
                PhotoFirebase photoFirebase = getPhotoFromCursor(cursorPhotoToSend);

                int columnIndex = cursorPhotoToSend.getColumnIndex(PhotoContract.COL_CHEMIN_LOCAL_PHOTO);
                photoLocalPath = cursorPhotoToSend.getString(columnIndex);
                Uri file = Uri.fromFile(new File(photoLocalPath));

                // On trouve une ID pour la photo
                UUID UUIDPhoto = UUID.randomUUID();

                // Récupération de l'id de l'annonce pour la mettre dans la metadata de la photo
                Integer idAnnonce = cursorPhotoToSend.getInt(cursorPhotoToSend.getColumnIndex(PhotoContract.COL_UUID_ANNONCE));

                // Upload du fichier de l'image sur FirebaseStorage
                sendPhotoToFirebaseStorage(UUIDPhoto, file, idAnnonce);

                // Enregistrement de la photo dans la FirebaseDatabase
                sendPhotoToFirebaseDatabase(UUIDPhoto, photoFirebase);

            }
            cursorPhotoToSend.close();
        }
    }

    private void postAnnonceEnAttente() {
        nbAnnoncesSend = 0;

        // Lecture des annonces postées Hors Connexion qui sont maintenant à envoyer
        final Cursor cursorAnnoncesToSend;
        String[] selectionArgs = new String[]{StatutAnnonce.ToPost.valeur()};
        cursorAnnoncesToSend = mContentResolver.query(AnnonceEntry.CONTENT_URI, null, sSelectionAnnoncesByStatut, selectionArgs, null);

        if (cursorAnnoncesToSend != null) {
            while (cursorAnnoncesToSend.moveToNext()) {
                // On récupère les données du curseur pour les insérer dans un POJO afin de pouvoir l'envoyer sur
                // La base de données Firebase.
                final AnnonceFirebase annonceFirebase = getAnnonceFromCursor(cursorAnnoncesToSend);

                // Création d'une nouvelle id pour l'annonce
                String idAnnonce = String.valueOf(UUID.randomUUID());
                annonceFirebase.setIdAnnonce(idAnnonce);

                // Insertion de notre annonce dans notre FirebaseDatabase
                FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
                DatabaseReference annonceRef = mDatabase.getReference("annonces/" + annonceFirebase.getIdAnnonce());
                annonceRef.setValue(annonceFirebase).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            exceptionMessage.add("postAnnonceEnAttente:Insertion dans Firebase échouée.");
                        } else {
                            // Mise à jour de l'annonce dans le content Provider
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(AnnonceContract.COL_STATUT_ANNONCE, StatutAnnonce.Valid.valeur());

                            String where = AnnonceContract._ID + "=?";
                            String[] args = new String[]{String.valueOf(annonceFirebase.getIdLocal())};

                            int rowUpdated = mContentResolver.update(ProviderContract.AnnonceEntry.CONTENT_URI, contentValues, where, args);
                            if (rowUpdated != 1) {
                                exceptionMessage.add("getInfoServer:Mise à jour du Provider échouée");
                            }
                        }
                    }
                });
            }
            cursorAnnoncesToSend.close();
        }
    }

    private PhotoFirebase getPhotoFromCursor(Cursor cursor) {
        PhotoFirebase photoForWs = new PhotoFirebase();
        int indexColIdLocal = cursor.getColumnIndex(PhotoContract._ID);
        int indexColIdAnnonce = cursor.getColumnIndex(PhotoContract.COL_UUID_ANNONCE);
        int indexColNomPhoto = cursor.getColumnIndex(PhotoContract.COL_CHEMIN_LOCAL_PHOTO);
        int indexColIdPhoto = cursor.getColumnIndex(PhotoContract.COL_UUID_PHOTO);

        photoForWs.setIdLocal(cursor.getInt(indexColIdLocal));
        photoForWs.setIdAnnonce(cursor.getInt(indexColIdAnnonce));
        photoForWs.setNomPhoto(cursor.getString(indexColNomPhoto));
        photoForWs.setIdPhoto(cursor.getInt(indexColIdPhoto));

        return photoForWs;
    }

    private AnnonceFirebase getAnnonceFromCursor(Cursor cursor) {
        AnnonceFirebase annonceFirebase = new AnnonceFirebase();
        int indexColIdLocal = cursor.getColumnIndex(AnnonceContract._ID);
        int indexColIdAnnonce = cursor.getColumnIndex(AnnonceContract.COL_UUID_ANNONCE);
        int indexColIdCategory = cursor.getColumnIndex(AnnonceContract.COL_ID_CATEGORY);
        int indexColIdUtilisateur = cursor.getColumnIndex(AnnonceContract.COL_ID_UTILISATEUR);
        int indexColTitreAnnonce = cursor.getColumnIndex(AnnonceContract.COL_TITRE_ANNONCE);
        int indexColDescriptionAnnonce = cursor.getColumnIndex(AnnonceContract.COL_DESCRIPTION_ANNONCE);
        int indexColPrixAnnonce = cursor.getColumnIndex(AnnonceContract.COL_PRIX_ANNONCE);

        annonceFirebase.setIdAnnonce(String.valueOf(cursor.getInt(indexColIdAnnonce)));
        annonceFirebase.setIdCategory(cursor.getInt(indexColIdCategory));
        annonceFirebase.setIdUtilisateur(cursor.getString(indexColIdUtilisateur));
        annonceFirebase.setTitreAnnonce(cursor.getString(indexColTitreAnnonce));
        annonceFirebase.setDescriptionAnnonce(cursor.getString(indexColDescriptionAnnonce));
        annonceFirebase.setPrixAnnonce(cursor.getInt(indexColPrixAnnonce));
        annonceFirebase.setIdLocal(cursor.getInt(indexColIdLocal));

        return annonceFirebase;
    }

    private class PhotoForWs {

    }

    private class MessageForWs {
        Integer idMessage;
        Integer idSender;
        Integer idReceiver;
        String message;
        Integer idLocal;
    }
}
