package org.cbigames.pdm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFile {
  static void download(String link, String fileName,long[] progress) throws IOException {

    URL url = new URL(link);
    URLConnection c = url.openConnection();
    c.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");
    if(progress!=null && progress.length>=2){
      progress[0] = c.getContentLengthLong();
    }

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
    output.close();
  }
}
