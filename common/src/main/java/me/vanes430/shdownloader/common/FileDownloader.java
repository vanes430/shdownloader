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
                String currentUrl = fileUrl;
                int redirectCount = 0;
                HttpURLConnection httpConn = null;
                
                while (redirectCount < 10) {
                    logger.accept("Connecting to: " + currentUrl);
                    URL url = new URL(currentUrl);
                    httpConn = (HttpURLConnection) url.openConnection();
                    httpConn.setInstanceFollowRedirects(false); // We handle redirects manually
                    httpConn.setRequestProperty("User-Agent", "Mozilla/5.0");
                    
                    int responseCode = httpConn.getResponseCode();
                    
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Success, break loop and download
                        break;
                    } else if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || 
                               responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                               responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                               responseCode == 307 || 
                               responseCode == 308) {
                        String newUrl = httpConn.getHeaderField("Location");
                        httpConn.disconnect();
                        
                        if (newUrl == null) {
                            onError.accept(new IOException("Redirected without Location header"));
                            return;
                        }
                        
                        // Handle relative redirects
                        if (!newUrl.startsWith("http")) {
                            URL previousUrl = new URL(currentUrl);
                            newUrl = new URL(previousUrl, newUrl).toString();
                        }
                        
                        currentUrl = newUrl;
                        redirectCount++;
                        logger.accept("Redirecting to (" + redirectCount + "/10): " + currentUrl);
                    } else {
                        httpConn.disconnect();
                        onError.accept(new IOException("Server returned HTTP code: " + responseCode));
                        return;
                    }
                }

                if (httpConn != null && httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
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
                    onError.accept(new IOException("Too many redirects or failed to connect."));
                }
                
                if (httpConn != null) httpConn.disconnect();
                
            } catch (Exception e) {
                onError.accept(e);
            }
        }).start();
    }
}
