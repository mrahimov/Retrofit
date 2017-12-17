package nyc.jrod.c4q.ac44.retrofit.before;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class Utils {
  public static String downloadUrl(URL url) throws IOException {
    InputStream stream = null;
    HttpsURLConnection connection = null;
    String result = null;
    try {
      connection = (HttpsURLConnection) url.openConnection();
      connection.setReadTimeout(3000);
      connection.setConnectTimeout(3000);
      connection.setRequestMethod("GET");
      connection.setDoInput(true);
      connection.connect();

      int responseCode = connection.getResponseCode();
      if (responseCode != HttpsURLConnection.HTTP_OK) {
        throw new IOException("HTTP error code: " + responseCode);
      }

      stream = connection.getInputStream();
      if (stream != null) {
        // Converts Stream to String with max size of 1MB.
        result = readStream(stream, 1 << 20);
      }
    } finally {
      if (stream != null) {
        stream.close();
      }
      if (connection != null) {
        connection.disconnect();
      }
    }
    return result;
  }

  private static String readStream(InputStream stream, int maxReadSize) throws IOException {
    Reader reader = new InputStreamReader(stream, "UTF-8");
    char[] rawBuffer = new char[maxReadSize];
    int readSize;
    StringBuilder builder = new StringBuilder();
    while (((readSize = reader.read(rawBuffer)) != -1) && maxReadSize > 0) {
      if (readSize > maxReadSize) {
        readSize = maxReadSize;
      }
      builder.append(rawBuffer, 0, readSize);
      maxReadSize -= readSize;
    }
    return builder.toString();
  }
}
