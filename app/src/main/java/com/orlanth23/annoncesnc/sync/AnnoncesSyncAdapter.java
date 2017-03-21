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
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.orlanth23.annoncesnc.R;
import com.orlanth23.annoncesnc.dto.StatutAnnonce;
import com.orlanth23.annoncesnc.dto.StatutPhoto;
import com.orlanth23.annoncesnc.list.ListeCategories;
import com.orlanth23.annoncesnc.list.ListeStats;
import com.orlanth23.annoncesnc.provider.contract.AnnonceContract;
import com.orlanth23.annoncesnc.provider.contract.PhotoContract;
import com.orlanth23.annoncesnc.webservice.InfoServer;
import com.orlanth23.annoncesnc.webservice.Proprietes;
import com.orlanth23.annoncesnc.webservice.RetrofitService;
import com.orlanth23.annoncesnc.webservice.ReturnWS;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
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
    private static final RetrofitService retrofitService = new Retrofit.Builder().baseUrl(Proprietes.getServerEndpoint()).addConverterFactory(GsonConverterFactory.create()).build().create(RetrofitService.class);
    private ContentResolver mContentResolver;
    private Context mContext;
    private int nbAnnoncesDeleted;
    private int nbAnnoncesSend;
    private int nbPhotosSend;
    private int nbMessageSend;
    private String textToSend;
    private List<String> exceptionMessage;

    public AnnoncesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        mContext = context;
    }

    public AnnoncesSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        // Suppression des annonces en attente de suppression
        deleteAnnonceEnAttente();

        // On envoie les annonces qui sont en attente
        postAnnonceEnAttente();

        // Suppression des photos
        deletePhotoEnAttente();

        // Récupération du nombre d'utilisateur
        getInfoServer();

        // Envoie d'une notification si on a envoyé quelque chose.
        sendNotification(textToSend);
    }

    private void sendNotification(String textToSend) {
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
        Call<ReturnWS> callGetInfoServer = retrofitService.infoServer();
        try {
            Response<ReturnWS> response = callGetInfoServer.execute();
            if (response.isSuccessful()) {
                ReturnWS rs = response.body();
                if (rs.statusValid()) {
                    InfoServer infoServer = gson.fromJson(rs.getMsg(), InfoServer.class);
                    ListeStats.setNbUsers(infoServer.getNbUtilisateur());
                    ListeStats.setNbAnnonces(infoServer.getNbAnnonce());
                    ListeCategories.setNbAnnonceFromHashMap(mContext, infoServer.getNbAnnonceByCategorie());
                } else {
                    exceptionMessage.add("Les infos serveur n'ont pas pu être récupérées. Retour du WS incorrect.");
                }
            } else {
                exceptionMessage.add("Les infos serveur n'ont pas pu être récupérées. Réponse incorrecte du serveur.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PhotoForWs getPhotoFromCursor(Cursor cursor) {
        PhotoForWs photoForWs = new PhotoForWs();
        int indexColIdLocal = cursor.getColumnIndex(PhotoContract._ID);
        int indexColIdAnnonce = cursor.getColumnIndex(PhotoContract.COL_ID_ANNONCE);
        int indexColNomPhoto = cursor.getColumnIndex(PhotoContract.COL_NOM_PHOTO);
        int indexColIdPhoto = cursor.getColumnIndex(PhotoContract.COL_ID_PHOTO_SERVER);

        photoForWs.idLocal = cursor.getInt(indexColIdLocal);
        photoForWs.idAnnonce = cursor.getInt(indexColIdAnnonce);
        photoForWs.nomPhoto = cursor.getString(indexColNomPhoto);
        photoForWs.idPhoto = cursor.getInt(indexColIdPhoto);

        return photoForWs;
    }

    private AnnonceForWs getAnnonceFromCursor(Cursor cursor) {
        AnnonceForWs annonceForWs = new AnnonceForWs();
        int indexColIdLocal = cursor.getColumnIndex(AnnonceContract._ID);
        int indexColIdAnnonce = cursor.getColumnIndex(AnnonceContract.COL_ID_ANNONCE_SERVER);
        int indexColIdCategory = cursor.getColumnIndex(AnnonceContract.COL_ID_CATEGORY);
        int indexColIdUtilisateur = cursor.getColumnIndex(AnnonceContract.COL_ID_UTILISATEUR);
        int indexColTitreAnnonce = cursor.getColumnIndex(AnnonceContract.COL_TITRE_ANNONCE);
        int indexColDescriptionAnnonce = cursor.getColumnIndex(AnnonceContract.COL_DESCRIPTION_ANNONCE);
        int indexColPrixAnnonce = cursor.getColumnIndex(AnnonceContract.COL_PRIX_ANNONCE);

        annonceForWs.idAnnonce = cursor.getInt(indexColIdAnnonce);
        annonceForWs.idCategory = cursor.getInt(indexColIdCategory);
        annonceForWs.idUtilisateur = cursor.getInt(indexColIdUtilisateur);
        annonceForWs.titreAnnonce = cursor.getString(indexColTitreAnnonce);
        annonceForWs.descriptionAnnonce = cursor.getString(indexColDescriptionAnnonce);
        annonceForWs.prixAnnonce = cursor.getInt(indexColPrixAnnonce);
        annonceForWs.idLocal = cursor.getInt(indexColIdLocal);

        return annonceForWs;
    }

    private void deletePhotoEnAttente() {
        // Lecture des photos supprimées Hors Connexion qui sont maintenant à supprimer sur le serveur
        Cursor cursorPhotosToDelete;
        String[] selectionArgs = new String[]{StatutPhoto.ToDelete.valeur()};
        cursorPhotosToDelete = mContentResolver.query(PhotoEntry.CONTENT_URI, null, sSelectionPhotosByStatut, selectionArgs, null);

        if (cursorPhotosToDelete != null) {
            while (cursorPhotosToDelete.moveToNext()) {
                PhotoForWs photoForWs = getPhotoFromCursor(cursorPhotosToDelete);
                try {
                    retrofitService.deletePhoto(photoForWs.idAnnonce, photoForWs.idPhoto).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            cursorPhotosToDelete.close();
        }
    }

    private void deleteAnnonceEnAttente() {
        // Lecture des annonces supprimées Hors Connexion qui sont maintenant à supprimer sur le serveur
        Cursor cursorAnnoncesToDelete;
        String[] selectionArgs = new String[]{StatutAnnonce.ToDelete.valeur()};
        cursorAnnoncesToDelete = mContentResolver.query(AnnonceEntry.CONTENT_URI, null, sSelectionAnnoncesByStatut, selectionArgs, null);

        if (cursorAnnoncesToDelete != null) {
            while (cursorAnnoncesToDelete.moveToNext()) {
                AnnonceForWs annonceWs = getAnnonceFromCursor(cursorAnnoncesToDelete);
                try {
                    // Appel de mon WS
                    Response<ReturnWS> response = retrofitService.deleteAnnonce(annonceWs.idAnnonce).execute();
                    if (response.isSuccessful()) {
                        ReturnWS rs = response.body();
                        if (rs.statusValid()) {
                            // Si la suppression à bien eu lieu sur le serveur, on va supprimer l'annonce dans notre ContentProvider
                            Integer idLocal = rs.getIdLocal();
                            String where = AnnonceContract._ID + " = ?";
                            String[] whereArgs = new String[]{String.valueOf(idLocal)};
                            mContentResolver.delete(AnnonceEntry.CONTENT_URI, where, whereArgs);
                            nbAnnoncesDeleted++;
                        } else {
                            exceptionMessage.add(rs.getMsg());
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    exceptionMessage.add(e.getMessage());
                }
            }
            cursorAnnoncesToDelete.close();
        }
    }

    private void postAnnonceEnAttente() {
        // Lecture des annonces postées Hors Connexion qui sont maintenant à envoyer
        Cursor cursorAnnoncesToSend;
        String[] selectionArgs = new String[]{StatutAnnonce.ToPost.valeur()};
        cursorAnnoncesToSend = mContentResolver.query(AnnonceEntry.CONTENT_URI, null, sSelectionAnnoncesByStatut, selectionArgs, null);

        if (cursorAnnoncesToSend != null) {
            while (cursorAnnoncesToSend.moveToNext()) {
                AnnonceForWs annonceWs = getAnnonceFromCursor(cursorAnnoncesToSend);
                try {
                    Response<ReturnWS> response = retrofitService.postAnnonce(annonceWs.idCategory,
                        annonceWs.idUtilisateur,
                        annonceWs.titreAnnonce,
                        annonceWs.descriptionAnnonce,
                        annonceWs.prixAnnonce,
                        annonceWs.idLocal).execute();

                    if (response.isSuccessful()) {
                        ReturnWS rs = response.body();
                        if (rs.statusValid()) {
                            // Si la mise à jour à bien eu lieu sur le serveur, on va mettre à jour l'annonce dans notre ContentProvider
                            Integer idLocal = rs.getIdLocal();
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(AnnonceContract.COL_STATUT_ANNONCE, StatutAnnonce.Valid.valeur());
                            String where = AnnonceContract._ID + " = ?";
                            String[] whereArgs = new String[]{String.valueOf(idLocal)};
                            mContentResolver.update(AnnonceEntry.CONTENT_URI, contentValues, where, whereArgs);
                            nbAnnoncesSend++;
                        } else {
                            exceptionMessage.add("Les infos serveur n'ont pas pu être récupérées. Retour du WS incorrect.");
                        }
                    } else {
                        exceptionMessage.add("Les infos serveur n'ont pas pu être récupérées. Réponse incorrecte du serveur.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    exceptionMessage.add(e.getMessage());
                }


            }
            cursorAnnoncesToSend.close();
        }
    }

    // Création des classes privées pour récupérer des infos à partir des curseurs
    private class AnnonceForWs {
        Integer idAnnonce;
        Integer idCategory;
        Integer idUtilisateur;
        String titreAnnonce;
        String descriptionAnnonce;
        Integer prixAnnonce;
        Integer idLocal;
    }

    private class PhotoForWs {
        Integer idAnnonce;
        Integer idLocal;
        Integer idPhoto;
        String nomPhoto;
    }

    private class MessageForWs {
        Integer idMessage;
        Integer idLocal;
        Integer idSender;
        Integer idReceiver;
        String message;
    }
}
