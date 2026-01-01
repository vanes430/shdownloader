package me.vanes430.shdownloader.common;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.function.Consumer;

public class FileDownloader {

    public static void download(String fileUrl, Path destination, Consumer<String> logger, Runnable onSuccess, Consumer<Exception> onError) {
        new Thread(() -> {
            try {
                logger.accept("Starting download from: " + fileUrl);
                URL url = new URL(fileUrl);
                HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setRequestProperty("User-Agent", "Mozilla/5.0");
                
                int responseCode = httpConn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedInputStream in = new BufferedInputStream(httpConn.getInputStream());
                         FileOutputStream fileOutputStream = new FileOutputStream(destination.toFile())) {
                        byte[] dataBuffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                            fileOutputStream.write(dataBuffer, 0, bytesRead);
                        }
                    }
                    onSuccess.run();
                } else {
                    onError.accept(new IOException("Server returned HTTP code: " + responseCode));
                }
                httpConn.disconnect();
            } catch (Exception e) {
                onError.accept(e);
            }
        }).start();
    }
}
