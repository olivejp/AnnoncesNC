package com.orlanth23.annoncesNC.utility;

import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by orlanth23 on 27/02/2016.
 */
public abstract class UploadFileToServer  extends AsyncTask<Void, Integer, String>{
    // Constructor
    public UploadFileToServer() {
    }

    /**
     * @return
     *     // urlDirUpload = "http://annonces.noip.me/AnnoncesNC/uploads"
     *     // urlPageUpload = "http://annonces.noip.me/AnnoncesNC/fileUpload.php"
     */
    public static String postHttpRequest(String sourceString, String urlPageUpload, String urlDirUpload) {

        String response = "";

        if (!sourceString.isEmpty()) {

            String boundary = "*****";
            String twoHyphens = "--";
            String lineEnd = "\r\n";
            String line;
            int maxBufferSize = 1024 * 1024;
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;

            try {
                // Récupération du fichier image
                File sourceFile = new File(sourceString);
                FileInputStream fileInputStream = new FileInputStream(sourceFile);

                // Création d'une URL et d'une HttpURLConnection
                URL url = new URL(urlPageUpload);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Paramétrage de la connection HTTP
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file", sourceString);

                // Récupération du outputstream pour pouvoir écrire des données qui seront envoyées à l'extérieur
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);

                //premier paramètre - filepath
                dos.writeBytes(twoHyphens + boundary + lineEnd);

                String filepath = urlDirUpload + sourceString;

                dos.writeBytes("Content-Disposition: form-data; name=\"filepath\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(filepath);
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);

                //Adding Parameter media file(audio,video and image)
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + sourceString + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                // create a buffer of maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();


                //----------------------------------------------------------------------------------------
                // Gestion de la réception
                //----------------------------------------------------------------------------------------

                // Gestion de l'input pour récupérer les données renvoyer par le serveur après l'envoi
                InputStream responseStream = new BufferedInputStream(connection.getInputStream());
                BufferedReader responseStreamReader =
                        new BufferedReader(new InputStreamReader(responseStream));

                StringBuilder stringBuilder = new StringBuilder();

                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                responseStreamReader.close();

                response = stringBuilder.toString();

                responseStream.close();

                // On se déconnecte
                connection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

}

