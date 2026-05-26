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

    public String getLibFolderName(){
        File zipFile = new File(downloadLink);
        String name = zipFile.getName();
        //remove the .zip
        name = name.substring(0,name.length()-4);
        return name;
    }

    public String getDownloadLink(){
        return downloadLink;
    }
}
