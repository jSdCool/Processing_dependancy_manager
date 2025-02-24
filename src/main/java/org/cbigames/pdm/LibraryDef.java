package org.cbigames.pdm;

import java.util.ArrayList;

public class LibraryDef {
    private String libPackage;
    private String libJar;

    private String libVersion;

    private ArrayList<String> libNatives = new ArrayList<>();
    private ArrayList<String>  libDependsJars = new ArrayList<>();


    public LibraryDef(String[] file, int start,int length){
        for(int i=start;i<start+length;i++){
            if(file[i].startsWith("package ")){
                libPackage = file[i].split(" ")[1];
                continue;
            }
            if(file[i].startsWith("jar ")){
                libJar = file[i].split(" ")[1];
                continue;
            }
            if(file[i].startsWith("version ")){
                libVersion = file[i].split(" ")[1];
                continue;
            }
            if(file[i].startsWith("native ")){
                libNatives.add(file[i].split(" ")[1]);
                continue;
            }
            if(file[i].startsWith("dependant ")){
                libDependsJars.add(file[i].split(" ")[1]);
                continue;
            }
        }
    }

}
