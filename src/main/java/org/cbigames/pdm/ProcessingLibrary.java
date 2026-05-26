package org.cbigames.pdm;

import java.io.File;

public class ProcessingLibrary {

    public ProcessingLibrary(String[] file, int start, int length) {
        boolean foundName = false;
        for (int i = start; i < start + length; i++) {
            if (file[i].startsWith("name ")) {
                foundName = true;
                name = file[i].substring("name ".length());
            }
        }
        if (!foundName) {
            throw new RuntimeException("Syntax Error. Expected processing lib name in plib block starting on line: " + start);
        }
    }

    private String name;
    private String downloadLink;

    public String getName() {
        return name;
    }

    public void setDownloadLink(String link){
        downloadLink = link;
    }

    public boolean resolved(){
        return downloadLink != null;
    }

    public String getDownloadLink(){
        return downloadLink;
    }
}
