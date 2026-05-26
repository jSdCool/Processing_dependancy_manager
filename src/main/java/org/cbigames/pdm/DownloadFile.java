package org.cbigames.pdm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class DownloadFile {
  static void download(String link, String fileName,long[] progress) throws IOException {

    URL url = new URL(link);
    URLConnection c = url.openConnection();
    c.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");
    Map<String, List<String>> headers = c.getHeaderFields();//follow link redirects
    for (String header : headers.get(null)) {
      if (header.contains(" 302 ") || header.contains(" 301 ")) {
        link = headers.get("Location").get(0);
        url = new URL(link);
        c = url.openConnection();
        headers = c.getHeaderFields();
      }
    }
    if(progress!=null && progress.length>=2){
      progress[0] = c.getContentLengthLong();
    }System.err.println(c.getContentLengthLong());

    InputStream input;
    input = c.getInputStream();
    byte[] buffer = new byte[4096];
    int n = -1;

    OutputStream output = new FileOutputStream(fileName);
    while ((n = input.read(buffer)) != -1) {
      if (n > 0) {
        output.write(buffer, 0, n);
        if(progress!=null && progress.length>=2){
          progress[1]+=n;
        }
      }
    }
    input.close();
    output.close();
  }

  public static String downloadToString(String link) throws IOException {
    URL url = new URL(link);
    URLConnection c = url.openConnection();
    c.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");

    InputStream input;
    input = c.getInputStream();

    byte[] buffer = new byte[4096];
    int n = -1;
    StringBuilder output = new StringBuilder();
    while ((n = input.read(buffer)) != -1) {
      if (n > 0) {
        output.append(new String(buffer,0,n));
      }
    }
    input.close();
    return output.toString();
  }
}
