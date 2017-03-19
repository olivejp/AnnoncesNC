package com.orlanth23.annoncesnc;

import android.content.Context;
<<<<<<< HEAD
=======
import android.content.ContextWrapper;
>>>>>>> origin/Branch_SyncAdapter
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
<<<<<<< HEAD
import com.google.firebase.storage.FileDownloadTask;
=======
>>>>>>> origin/Branch_SyncAdapter
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
<<<<<<< HEAD
import java.io.IOException;

import static junit.framework.Assert.assertTrue;
=======
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
>>>>>>> origin/Branch_SyncAdapter

@RunWith(AndroidJUnit4.class)
public class TestFirebaseStorage {

<<<<<<< HEAD
    private StorageReference mStorageRef;
    private Context mContext;
=======
    private static final String IMAGE_NAME = "profile.jpg";
    private static final String IMAGE_DIR = "imageDir";
    private Context mContext;
    private StorageReference mStorageRef;
>>>>>>> origin/Branch_SyncAdapter

    @Before
    public void precondition() {
        mContext = InstrumentationRegistry.getTargetContext();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

<<<<<<< HEAD
    @Test
    public void downloadFileFromFirebase() {
        File localFile = null;
        try {
            localFile = File.createTempFile("images", "jpg");
            StorageReference riversRef = mStorageRef.child("images/portrait_jp.jpg");
            riversRef.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            // Successfully downloaded data to local file
                            // ...
                            assertTrue(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failed download
                    // ...
                    assertTrue(false);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void sendImageToFirebase() {

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_annonces);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] byteArray = baos.toByteArray();

        StorageReference riversRef = mStorageRef.child("images/rivers.png");

        riversRef.putBytes(byteArray)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
=======
    private String saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(mContext);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, IMAGE_NAME);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath.toString();
    }

    private Bitmap loadImageFromStorage(String path) {
        try {
            File f = new File(path, IMAGE_NAME);
            return BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Test
    public void imageStorage() {
        // Création d'une image
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_action_accept);

        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            String imageName = "images/" + UUID.randomUUID() + ".png";

            StorageReference storageRef = mStorageRef.child(imageName);

            storageRef.putBytes(data).
                addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
>>>>>>> origin/Branch_SyncAdapter
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
<<<<<<< HEAD
                        assertTrue(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        assertTrue(false);
                    }
                });
=======
                        assertTrue("onSuccess", true);
                    }
                }).
                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        assertTrue("FAIL : onFailure", false);
                    }
                });
        } else {
            assertTrue("FAIL : fileImage n'existe pas", false);
        }

>>>>>>> origin/Branch_SyncAdapter
    }
}
