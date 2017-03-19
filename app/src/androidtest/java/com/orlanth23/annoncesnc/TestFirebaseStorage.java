package com.orlanth23.annoncesnc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TestFirebaseStorage {

    private StorageReference mStorageRef;
    private Context mContext;

    @Before
    public void precondition() {
        mContext = InstrumentationRegistry.getTargetContext();
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

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
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
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
    }
}
